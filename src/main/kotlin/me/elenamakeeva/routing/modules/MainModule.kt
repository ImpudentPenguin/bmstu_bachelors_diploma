package me.elenamakeeva.routing.modules

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.TrayState
import kotlinx.coroutines.*
import me.elenamakeeva.routing.models.*
import me.elenamakeeva.routing.modules.VRPModule.Companion.DEPOT
import me.elenamakeeva.routing.utils.*
import me.elenamakeeva.routing.utils.Constants.DEBUGGABLE
import me.elenamakeeva.routing.utils.Constants.FORMATTER
import java.util.logging.Filter
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.random.Random

@OptIn(DelicateCoroutinesApi::class)
class MainModule(private val state: TrayState? = null) {

    companion object {
        private const val ANSI_RED = "\u001B[31m"

        private val LOGGER = Logger.getGlobal().also { logger ->
            logger.filter = Filter { record ->
                when (record.level) {
                    Level.WARNING -> println(ANSI_RED + "${record.level}: ${record.message}")
                    else -> println("${record.level}: ${record.message}")
                }

                false
            }
        }
    }

    var futureRoutes = mutableStateOf<List<Route>>(emptyList())
    var currentRoutes = mutableStateOf<List<Route>>(emptyList())
    var finishedRoutes = mutableStateOf<List<Route>>(emptyList())
    var oldRoutes = mutableStateOf<List<Route>>(emptyList())
    var isGeneratedRequests = mutableStateOf(false)

    private val dbModule = DBModule()
    private var expressRequests: MutableList<Request>? = null
    private var requests: List<Node> = emptyList()
    private var jobs = mutableListOf<Job>()

    private var requestsOffset = 0
    private var expressRequestsOffset = 0
    private var result: Result? = null

    private lateinit var vrpModule: VRPModule

    init {
        updateReports()
    }

    suspend fun generateRoutes(loaderFinish: () -> Unit) {
        clearStates()
        startVRP(loaderFinish)
    }

    suspend fun getRequests(requestsCount: Int, expressRequestsCount: Int) {
        launch(
            onStart = {
                LOGGER.info("Запрошены заявки из базы данных")

                requests = (dbModule.getRequests(limit = requestsCount, offset = requestsOffset) as MutableList<Node>)
                    .also { it.returnToDepot(0) }
                expressRequests = dbModule.getExpressRequests(
                    requestsSize = requests.size,
                    limit = expressRequestsCount,
                    offset = expressRequestsOffset
                )

                requestsOffset += requests.size
                expressRequestsOffset += expressRequests?.size ?: 0
            },
            onComplete = { time ->
                LOGGER.info("Заяки успешно получены [milliseconds: $time]")
                LOGGER.info("Количество заявок ${requests.size - 1}. Количество экспресс заявок ${expressRequests?.size ?: 0}")
            }
        )
    }

    private suspend fun startVRP(loaderFinish: () -> Unit) {
        launch(
            onStart = {
                LOGGER.info("Запущен поиск маршрутов")
                val start = System.currentTimeMillis()
                val cars = dbModule.getCars()
                vrpModule = VRPModule(requests, cars)

                val result = vrpModule.findPath()
                val time = System.currentTimeMillis() - start
                LOGGER.info("Поиск маршрутов завершен [milliseconds: $time]")

                this@MainModule.result = result
                loaderFinish.invoke()
                updateStates()
            }
        )
    }

    private suspend fun updateStates() {
        if (result?.routes.isNullOrEmpty()) {
            LOGGER.warning("Маршруты отсутствуют")
            return
        }

        launch(
            onStart = {
                LOGGER.info("Запущено обновление состояний")

                modeling()
                val expressRequestsJob = generateExpress()

                do {
                    futureRoutes.value = result?.routes?.filter { !it.path.first().isVisited } ?: emptyList()
                    currentRoutes.value = result?.routes?.filter {
                        it.path.first().isVisited && it.path.any { node -> !node.isVisited }
                    } ?: emptyList()
                    finishedRoutes.value = result?.routes?.filter {
                        it.path.all { node -> node.isVisited }
                    } ?: emptyList()
                } while (futureRoutes.value.isNotEmpty() || currentRoutes.value.isNotEmpty()
                    || finishedRoutes.value.size != result?.routes?.size || !expressRequestsJob.isCompleted
                )
            },
            onComplete = { time ->
                dbModule.saveReports(finishedRoutes.value)
                finishedRoutes.value = emptyList()
                updateReports()
                LOGGER.info("Обновление состояний завершено [milliseconds: $time]")
            }
        )
    }

    private fun modeling() {
        GlobalScope.launch {
            result?.routes?.forEachIndexed { routeIndex, _ ->
                val job = routeWork(routeIndex)
                jobs.add(job)
            }
        }
    }

    private suspend fun generateExpress(): Job {
        if (expressRequests.isNullOrEmpty()) {
            return GlobalScope.launch { LOGGER.warning("Экспресс заявки на доставку отсутствуют") }
        }

        return GlobalScope.launch {
            val randomCarsCount = getRandomInt(1, result?.routes?.size ?: 0)
            val used = mutableListOf<Int>()
            var start = 0L

            launch {
                start = System.currentTimeMillis()
                LOGGER.info("Запущено получение экспресс заявок на доставку")

                if (DEBUGGABLE) delay(3000)
                else wait(3000, (result?.routes?.size ?: 1) * 2000L)

                while (used.size < randomCarsCount) {
                    val randomCar = getRandomInt(0, result?.routes?.size ?: -1)

                    launch(
                        onStart = LaunchCatching@{
                            if (!used.contains(randomCar)) {
                                val route = result?.routes?.get(randomCar)
                                    ?: return@LaunchCatching
                                val expressIndex = getRandomInt(0, expressRequests?.size ?: 0)
                                val express = expressRequests?.removeAt(expressIndex)
                                    ?: run {
                                        return@LaunchCatching
                                    }

                                if (DEBUGGABLE) delay(2000)
                                else wait(2000, (route.path.size - 1) / 2 * 2000L)

                                jobs[randomCar].cancel()

                                state?.sendNotification(
                                    Notification(
                                        "Внимание",
                                        "Появилась срочная заявка №${express.requestId} у ТС ${route.carId}"
                                    )
                                )

                                val path = route.path.toMutableList()
                                val currentIndex = path.getLastVisitedNode()
                                val nextIndex = currentIndex + 1
                                val newPath = path.drop(nextIndex.coerceAtLeast(1))
                                    .also {
                                        if (currentIndex != path.size)
                                            it.dropLast(1)
                                    }
                                val mainPath = path - newPath.toSet()
                                val isRebuildPath = vrpModule.checkDistance(newPath, express)

                                if (isRebuildPath) {
                                    val pathWithoutDepot = newPath.mapNotNull { if (it is Request) it else null }
                                    val car = Car(
                                        id = route.carId,
                                        model = route.model,
                                        startTime = route.startTime,
                                        currentTime = route.date,
                                        path = mutableListOf()
                                    )

                                    val rebuildPath = vrpModule.rebuildPath(car, express, pathWithoutDepot).let { updatedPath ->
                                        if (currentIndex == 0) updatedPath.removeAt(0)
                                        updatedPath
                                    }
                                    val updatedPath = mainPath + rebuildPath

                                    result?.routes?.get(randomCar)?.path = updatedPath.toMutableList()
                                    LOGGER.info("Обновлен маршрут ТС ${route.carId} с динамическим перестроением")
                                } else {
                                    if (nextIndex != 0 && nextIndex != path.size && path[nextIndex - 1] != DEPOT) {
                                        path.returnToDepot(nextIndex)
                                        path.add(nextIndex + 1, express)
                                    } else {
                                        path.add(nextIndex.coerceAtLeast(1), express)
                                        if (nextIndex == path.size - 1)
                                            path.returnToDepot(nextIndex + 1)
                                    }

                                    result?.routes?.get(randomCar)?.path = path
                                    LOGGER.info("Обновлен маршрут ТС ${route.carId} без перестроения")
                                }

                                result?.routes?.get(randomCar)?.hasExpress = true
                                used.add(randomCar)
                                jobs[randomCar] = routeWork(randomCar)
                            }
                        }
                    )
                }
            }.invokeOnCompletion {
                val time = System.currentTimeMillis() - start
                LOGGER.info("Получение экспресс заявок завершено [milliseconds: $time]")
            }
        }
    }

    private fun CoroutineScope.routeWork(routeIndex: Int): Job {
        val route = result?.routes?.get(routeIndex)
        return launch {
            if (route == null) return@launch

            if (DEBUGGABLE) delay(1000)
            else wait(1000, 15000)

            val distances = vrpModule.calculateDistances(route.path).first
            var index = route.path.indexOfFirst { !it.isVisited }.coerceAtLeast(0)

            while (!route.path.all { it.isVisited } && index < route.path.size) {
                if (DEBUGGABLE) delay(2000)
                else wait(2000, 20000)

                val node = route.path[index]

                if (index + 1 < distances.size) {
                    val minutes = distances[index][index + 1].kmToMinutes()
                    route.date = route.date.plusMinutes(minutes).plusMinutes(10)
                }

                node.isVisit.value = true
                node.isVisited = true
                wait(100, 101)
                node.isVisit.value = false
                index++
            }
        }.also { job ->
            job.invokeOnCompletion { error ->
                route?.let {
                    if (error == null)
                        LOGGER.info("Время ТС ${route.carId} ${route.date.format(FORMATTER)}")
                    else
                        LOGGER.info("Маршрут ТС ${route.carId} приостановлен, так как появилась срочная заявка")
                }
            }
        }
    }

    private fun MutableList<Node>.getLastVisitedNode(): Int {
        val current = this.indexOfLast { it.isVisited }
        current.takeIf { it > -1 }?.let { this[current].isVisit.value = false }

        return current
    }

    private suspend fun wait(from: Long, until: Long) {
        delay(Random.nextLong(from, until))
    }

    private fun updateReports() {
        oldRoutes.value = dbModule.getReports()
    }

    private fun getRandomInt(from: Int, until: Int) = Random.nextInt(from, until)

    private fun clearStates() {
        futureRoutes.value = emptyList()
        currentRoutes.value = emptyList()
        finishedRoutes.value = emptyList()
        jobs = mutableListOf()
        result = null
    }

    private fun MutableList<Node>.returnToDepot(index: Int) {
        add(index, DEPOT.copy(isVisited = false, isVisit = mutableStateOf(false)))
    }
}