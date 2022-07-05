package me.elenamakeeva.routing.modules

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import me.elenamakeeva.routing.models.*
import me.elenamakeeva.routing.utils.CAR_INFO
import me.elenamakeeva.routing.utils.Coordinates
import me.elenamakeeva.routing.utils.timeFormat
import me.elenamakeeva.routing.utils.Constants
import java.sql.Connection
import java.sql.Date
import java.sql.DriverManager
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DBModule {

    companion object {
        private const val DB_URL = "jdbc:postgresql://localhost:5432/routingdb"
    }

    private var connection: Connection? = null

    init {
        connection = DriverManager.getConnection(DB_URL)
    }

    fun getReports(limit: Int = 500, offset: Int = 0): List<Route> {
        val query = "SELECT rts.\"carId\", crs.model, date, \"nodes\" FROM public.routes rts JOIN public.cars crs ON crs.\"carId\" = rts.\"carId\" LIMIT $limit OFFSET $offset"
        val pst = connection?.prepareStatement(query)
        val rs = pst?.executeQuery()

        val data = mutableListOf<Route>()

        while (rs?.next() == true) {
            var hasExpress = false
            val carId = rs.getString(1)
            val model = rs.getString(2)
            val date = LocalDateTime.of(rs.getDate(3).toLocalDate(), LocalTime.of(8, 0))
            val jsonNode = rs.getObject(4).toString().let { json ->
                "[${json.substring(2, json.length - 2)
                    .replace("\\", "")
                    .replace("}\",\"{", "}, {")}]"
            }

            val nodes = Json.decodeFromString<List<RequestSerializable>>(jsonNode)
                .mapIndexed { index, requestSerializable ->
                    if (requestSerializable.requestId == -1)
                        Depot()
                    else {
                        if (requestSerializable.isExpress && !hasExpress) {
                            hasExpress = true
                        }

                        Request(
                            id = index,
                            requestId = requestSerializable.requestId,
                            placeX = requestSerializable.placeX,
                            placeY = requestSerializable.placeY,
                            weight = requestSerializable.weight,
                            direction = Coordinates.getDirection(
                                requestSerializable.placeX,
                                requestSerializable.placeY
                            ),
                            isExpress = requestSerializable.isExpress,
                            timeWindow = TimeWindow(
                                startTime = LocalDateTime.of(
                                    date.toLocalDate(),
                                    LocalTime.parse(requestSerializable.timeWindow?.start)
                                ),
                                endTime = LocalDateTime.of(
                                    date.toLocalDate(),
                                    LocalTime.parse(requestSerializable.timeWindow?.end)
                                )
                            )
                        )
                    }
                }.toMutableList()

            data.add(Route(carId = carId, model = model, date = date, path = nodes, hasExpress = hasExpress))
        }

        return data
    }

    fun saveReports(data: List<Route>) {
        val query = "INSERT INTO public.routes(\"carId\", \"nodes\", date) VALUES (?, ?, ?)"
        val pst = connection?.prepareStatement(query)

        data.forEach { route ->
            val json = route.path.map {
                Json.encodeToString(
                    RequestSerializable(
                        requestId = it.requestId,
                        placeX = it.placeX,
                        placeY = it.placeY,
                        weight = it.weight,
                        timeWindow = TimeWindowSerializable(
                            it.timeWindow?.startTime.timeFormat(),
                            it.timeWindow?.endTime.timeFormat()
                        ),
                        isExpress = if (it is Request) it.isExpress else false
                    )
                )
            }
            val path = route.path.mapNotNull { if (it is Request && !it.isExpress) it.requestId else null }
            val expressRequests = route.path.mapNotNull { if (it is Request && it.isExpress) it.requestId else null }
            val array = connection?.createArrayOf("json", json.toTypedArray())

            pst?.setString(1, route.carId)
            pst?.setArray(2, array)
            pst?.setDate(3, Date.valueOf(route.date.toLocalDate()))
            pst?.executeUpdate()

            path.takeIf { it.isNotEmpty() }?.let { removeRequests(it) }
            expressRequests.takeIf { it.isNotEmpty() }?.let { removeExpressRequests(it) }
        }
    }

    fun getCars(limit: Int = Constants.MAX_CARS): List<CAR_INFO> {
        val query = "SELECT \"carId\", \"model\" FROM public.cars ORDER BY random() LIMIT $limit"
        val pst = connection?.prepareStatement(query)
        val rs = pst?.executeQuery()

        val data = mutableListOf<CAR_INFO>()

        while (rs?.next() == true) {
            val car = rs.getString(1) to rs.getString(2)
            data.add(car)
        }

        return data
    }

    fun getRequests(limit: Int = 500, offset: Int = 0): MutableList<Request> {
        val query = "SELECT \"requestId\", \"placeX\", \"placeY\", weight, \"startTime\", \"endTime\" " +
                "FROM public.requests LIMIT $limit OFFSET $offset"

        val pst = connection?.prepareStatement(query)
        val rs = pst?.executeQuery()

        val data = mutableListOf<Request>()
        var index = 1

        while (rs?.next() == true) {
            val requestId = rs.getInt(1)
            val placeX = rs.getDouble(2)
            val placeY = rs.getDouble(3)
            val weight = rs.getDouble(4)
            val start = rs.getString(5)
            val end = rs.getString(6)
            val direction = Coordinates.getDirection(placeX, placeY)

            val request = Request(
                id = index++,
                requestId = requestId,
                placeX = placeX,
                placeY = placeY,
                direction = direction,
                weight = weight,
                timeWindow = TimeWindow(
                    LocalDateTime.of(LocalDate.now(), LocalTime.parse(start)),
                    LocalDateTime.of(LocalDate.now(), LocalTime.parse(end))
                )
            )

            data.add(request)
        }

        return data
    }

    fun getExpressRequests(requestsSize: Int, limit: Int = 50, offset: Int = 0): MutableList<Request> {
        val query = "SELECT \"requestId\", \"placeX\", \"placeY\", weight, \"startTime\", \"endTime\", \"isExpress\" " +
                "FROM public.\"expressRequests\" LIMIT $limit OFFSET $offset"

        val pst = connection?.prepareStatement(query)
        val rs = pst?.executeQuery()

        val data = mutableListOf<Request>()
        var index = requestsSize

        while (rs?.next() == true) {
            val requestId = rs.getInt(1)
            val placeX = rs.getDouble(2)
            val placeY = rs.getDouble(3)
            val weight = rs.getDouble(4)
            val start = rs.getString(5)
            val end = rs.getString(6)
            val isExpress = rs.getBoolean(7)
            val direction = Coordinates.getDirection(placeX, placeY)

            val request = Request(
                id = index++,
                requestId = requestId,
                placeX = placeX,
                placeY = placeY,
                direction = direction,
                weight = weight,
                timeWindow = TimeWindow(
                    LocalDateTime.of(LocalDate.now(), LocalTime.parse(start)),
                    LocalDateTime.of(LocalDate.now(), LocalTime.parse(end))
                ),
                isExpress = isExpress
            )

            data.add(request)
        }

        return data
    }

    private fun removeRequests(path: List<Int>) {
        val indexes = path.joinToString(", ")
        val query = "DELETE FROM public.requests WHERE \"requestId\" IN ($indexes)"
        val pst = connection?.prepareStatement(query)

        pst?.executeUpdate()
    }

    private fun removeExpressRequests(path: List<Int>) {
        val indexes = path.joinToString(", ")
        val query = "DELETE FROM public.\"expressRequests\" WHERE \"requestId\" IN ($indexes)"
        val pst = connection?.prepareStatement(query)

        pst?.executeUpdate()
    }
}