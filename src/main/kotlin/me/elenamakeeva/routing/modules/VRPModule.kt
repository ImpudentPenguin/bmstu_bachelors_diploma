package me.elenamakeeva.routing.modules

import me.elenamakeeva.routing.models.*
import me.elenamakeeva.routing.utils.*
import java.time.LocalDateTime
import java.util.function.Function
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.streams.toList

class VRPModule(private var requests: List<Node>, private val cars: List<CAR_INFO>) {

    companion object {
        val DEPOT = Depot()
        const val MAX_TRUCK_CAPACITY = 500.0 // кг
        private const val ALPHA = 1
        private const val BETA = 2
        private const val q0 = 0.75
        private const val MAX_ITERATION = 500
        private const val TAU0 = 10e-6
        private const val T0 = 100.0
        private const val THETA = 0.999
        private const val Q = 2
        private const val P = 0.1
        private const val WORK_DAY_HOURS = 9L
    }

    private var t = T0
    private var bestResult = Result(total = Double.MAX_VALUE)
    private var tempPath: MutableList<ID> = mutableListOf()
    private var pheromones = MutableList(requests.size) { MutableList(requests.size) { TAU0 } }
    private var distance: List<List<Double>> = emptyList()
    private var inverse: List<List<Double>> = emptyList()

    init {
        calculateDistances(requests).also { (dist, inv) ->
            distance = dist
            inverse = inv
        }
    }

    fun findPath(): Result {
        tempPath.clear()

        val earliestStartTime = requests.getEarliestStartTime()

        for (i in 0 until MAX_ITERATION) {
            val result = Result()
            tempPath = mutableListOf(DEPOT.id)
            requests.forEach { it.isAdded = false }
            var countCar = 0

            while (countCar < Constants.MAX_CARS && tempPath.size < requests.size - 1) {
                val (carId, model) = cars[countCar]
                val carModel = Car(
                    id = carId,
                    model = model,
                    startTime = earliestStartTime,
                    currentTime = earliestStartTime,
                    path = mutableListOf(Depot())
                )
                val car = buildRoute(carModel)

                if (car.path.size > 2) { // проверка на то, что у ТС нет заявок
                    result.routes.add(
                        Route(
                            carId = car.id,
                            model = car.model,
                            date = car.startTime,
                            path = car.path,
                            distance = car.distanceTravelled
                        )
                    )
                    result.total += car.distanceTravelled
                }

                countCar++
            }

            updatePheromonesGlobal(result)

            if (result.total < bestResult.total) {
                bestResult = result
            }
        }

        return bestResult
    }

    fun rebuildPath(car: Car, expressRequest: Request, requests: List<Request>): MutableList<Node> {
        val nodes = (requests.toMutableList() as MutableList<Node>).also {
            it.addAll(0, listOf(DEPOT, expressRequest))

            it.forEachIndexed { index, node ->
                node.id = index
            }
        }

        calculateDistances(nodes).let { (dist, inv) ->
            distance = dist
            inverse = inv
        }

        this.requests = nodes

        tempPath.clear()
        pheromones = MutableList(nodes.size) { MutableList(nodes.size) { TAU0 } }
        var best = Result(total = Double.MAX_VALUE)

        for (i in 0 until MAX_ITERATION) {
            val result = Result()
            tempPath = mutableListOf(nodes.first().id, nodes.component2().id)
            car.path = mutableListOf(nodes.first(), nodes.component2())

            nodes.forEach { it.isAdded = false }

            var isStop = false

            while (tempPath.size < nodes.size - 1 && !isStop) {
                val route = buildRoute(car)

                if (route.path.size > 2) { // проверка на то, что у ТС нет заявок
                    result.routes.add(
                        Route(
                            carId = route.id,
                            model = route.model,
                            date = route.startTime,
                            path = route.path
                        )
                    )
                    result.total += route.distanceTravelled
                    isStop = true
                }
            }

            updatePheromonesGlobal(result)

            if (result.total < best.total) {
                best = result
            }
        }

        val lost = nodes
            .filter { node -> best.routes.firstOrNull()?.path?.firstOrNull { it.id == node.id } == null }
            .toMutableList()

        while ((best.routes.firstOrNull()?.path?.size ?: 0) < nodes.size + 1 && lost.isNotEmpty()) {
            val lostNode = lost.removeFirstOrNull() ?: continue
            var min = Long.MAX_VALUE
            var newIndex = -1

            best.routes.first().path.forEachIndexed { index, node ->
                val currentMin = distance[lostNode.id][node.id].kmToMinutes()

                if (currentMin < min) {
                    newIndex = index
                    min = currentMin
                }
            }

            if (newIndex != -1) {
                best.routes.first().path.add(newIndex + 1, lostNode)
            }
        }

        return best.routes.first().path
    }

    fun checkDistance(nodes: List<Node>, request: Request?): Boolean {
        if (request == null) return false

        var index = -1
        var min = Long.MAX_VALUE
        val updatedNodes = nodes.toMutableList().also { it.add(0, request) }
        val distances = calculateDistances(updatedNodes)

        for (i in 1 until updatedNodes.size) {
            val currentMin = distances.first[0][i].kmToMinutes()
            if (currentMin < min) {
                index = i
                min = currentMin
            }
        }

        return index != -1
    }

    private fun buildRoute(car: Car): Car {
        while (true) {
            val path = getAvailableRequest(car)

            if (path.isNotEmpty()) {
                val next = getNext(car, path)
                tempPath.add(next.id)
                localUpdate(car, next)
            } else {
                val nextNode = getNextAvailableNodeList(car)
                if (nextNode.isNotEmpty()) {
                    val next = getNext(car, nextNode)
                    tempPath.add(next.id)
                    localUpdate(car, next)
                } else {
                    break
                }
            }
        }

        returnToDepot(car)
        car.resetTime()
        return car
    }

    private fun getAvailableRequest(car: Car): List<Node> {
        val currentRequestIndex = car.getLastVisited()
        val isCheckWorkHours = requests.any { if (it is Request) it.isExpress else false }

        return requests.filter { node ->
            !tempPath.contains(node.id) && !node.isAdded
                    && (car.currentTime >= node.timeWindow?.startTime && car.currentTime <= node.timeWindow?.endTime)
                    && (car.currentTime.plusMinutes(distance[currentRequestIndex][node.id].kmToMinutes()) <= node.timeWindow?.endTime)
                    && (if (isCheckWorkHours) checkWorkHours(car, currentRequestIndex, node.id) else true)
        }
    }

    private fun getNextAvailableNodeList(car: Car): List<Node> {
        val currentRequestIndex = car.getLastVisited()
        val isCheckWorkHours = requests.any { if (it is Request) it.isExpress else false }

        return requests.filter { node ->
            !tempPath.contains(node.id) && !node.isAdded
                    && (car.currentTime.plusMinutes(distance[currentRequestIndex][node.id].kmToMinutes()) <= node.timeWindow?.endTime)
                    && node.timeWindow?.startTime?.isAfter(car.currentTime) ?: false
                    && car.usedCapacity + node.weight < MAX_TRUCK_CAPACITY
                    && (if (isCheckWorkHours) checkWorkHours(car, currentRequestIndex, node.id) else true)
        }
    }

    private fun checkWorkHours(car: Car, currentRequestIndex: Int, nodeId: Int): Boolean {
        return (car.startTime.plusHours(WORK_DAY_HOURS) > car.currentTime.plusMinutes(distance[currentRequestIndex][nodeId].kmToMinutes()))
    }

    private fun nextCustomerRule(lastVisited: Int): Function<Node, Double> {
        return Function<Node, Double> { node: Node ->
            calculateCost(lastVisited, node.id)
        }
    }

    private fun getNext(car: Car, available: List<Node>): Node {
        val lastVisited = car.getLastVisited()

        val next = if (q0 <= Math.random()) {
            available.stream()
                .max(Comparator.comparing(nextCustomerRule(lastVisited))).get()
        } else {
            val rand = Math.random()
            val denominator = available.stream().mapToDouble { node ->
                calculateCost(lastVisited, node.id)
            }.sum()

            var tmp = available.stream().map { node -> node.id to (calculateCost(lastVisited, node.id) / denominator) }
                .sorted(Comparator.comparingDouble { node -> node.second })
                .toList()
            var result = tmp.last()
            tmp = tmp.stream().filter { node -> rand < node.second }.toList()
            if (tmp.isNotEmpty())
                result = tmp.first()

            available[available.indexOfFirst { it.id == result.first }]
        }

        requests[next.id].isAdded = true
        return next
    }

    private fun returnToDepot(car: Car) =
        localUpdate(car, DEPOT.copy(isVisited = false))

    private fun localUpdate(car: Car, node: Node) {
        val lastNodeIndex = car.getLastVisited()
        updateCarInfo(car, node, lastNodeIndex)
        updatePheromonesLocal(node, lastNodeIndex)
    }

    private fun updateCarInfo(car: Car, node: Node, lastNodeIndex: Int) {
        car.path.add(node)

        car.usedCapacity += node.weight
        car.distanceTravelled += distance[lastNodeIndex][node.id]
        car.currentTime = car.currentTime.plusMinutes(distance[lastNodeIndex][node.id].kmToMinutes())

        val time = node.timeWindow

        if (time != null && car.currentTime < time.startTime) {
            val waitingTime = time.startTime.minute - car.currentTime.minute
            car.currentTime = car.currentTime.plusMinutes(waitingTime.toLong())
        }

        car.currentTime = car.currentTime.plusMinutes(10)
    }

    private fun updatePheromonesLocal(node: Node, lastNodeIndex: Int) = updatePheromones(lastNodeIndex, node.id)

    private fun updatePheromonesGlobal(result: Result) {
        if (isGlobalUpdateAllowed(result)) {
            val delta = 1 / result.total
            result.routes.forEach { route ->
                for (i in 1 until route.path.size) {
                    val last = requests.first { it.requestId == route.path[i - 1].requestId }.id
                    val current = requests.first { it.requestId == route.path[i].requestId }.id
                    updatePheromones(last, current, delta)
                }
            }
        }

        updateTemperature()
    }

    private fun isGlobalUpdateAllowed(result: Result): Boolean {
        val delta = result.total - bestResult.total
        return delta < 0 || exp(-delta / t) > Math.random()
    }

    private fun updateTemperature() {
        t *= THETA
    }

    private fun calculateCost(lastVisited: Int, next: Int): Double {
        return pheromones[lastVisited][next].pow(ALPHA) + (inverse[lastVisited][next]).let { value ->
            if (Constants.WITH_MODIFICATION)
                value.pow(BETA)
            else value
        }
    }

    private fun updatePheromones(last: Int, next: Int, delta: Double = TAU0) {
        pheromones[last][next] = (1 - P) * pheromones[last][next] + (Q / delta)
    }

    private fun List<Node>.getEarliestStartTime(): LocalDateTime {
        return this.stream()
            .skip(1)
            .map { it.timeWindow?.startTime ?: LocalDateTime.MAX }
            .min(LocalDateTime::compareTo)
            .get()
    }

    fun calculateDistances(requests: List<Node>): Pair<DISTANCES, INVERSE_DISTANCES> {
        val distances = MutableList(requests.size) { MutableList(requests.size) { 0.0 } }
        val inverse = MutableList(requests.size) { MutableList(requests.size) { 0.0 } }
        requests.forEachIndexed { index, request ->
            for (j in index until requests.size) {
                if (index == j) {
                    distances[index][j] = 0.0
                    inverse[index][j] = Double.MAX_VALUE
                } else {
                    val request2 = requests[j]
                    val distance = sqrt(
                        (request.placeX - request2.placeX).pow(2) + (request.placeY - request2.placeY).pow(2)
                    )
                    distances[index][j] = distance
                    distances[j][index] = distance
                    val inverseDistance = (1 / distances[index][j]).let { value ->
                        if (Constants.WITH_MODIFICATION)
                            value.pow(BETA)
                        else value
                    }
                    inverse[index][j] = inverseDistance
                    inverse[j][index] = inverseDistance
                }
            }
        }
        return distances to inverse
    }
}