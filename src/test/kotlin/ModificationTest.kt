package me.elenamakeeva.routing.test

import me.elenamakeeva.routing.models.Node
import me.elenamakeeva.routing.modules.DBModule
import me.elenamakeeva.routing.modules.VRPModule
import me.elenamakeeva.routing.utils.Constants
import org.junit.Before
import org.junit.Test

@Suppress("UNCHECKED_CAST")
class ModificationTest {

    private val dbModule = DBModule()
    private lateinit var vrpModule: VRPModule

    @Before
    fun setup() {
        Constants.DEBUGGABLE = true
    }

    @Test
    fun `вызов вычисления маршрута с количеством заявок = 200 без модификации`() {
        Constants.WITH_MODIFICATION = false

        val requests =
            (dbModule.getRequests(limit = 200, offset = 0).toMutableList() as MutableList<Node>).let { nodes ->
                nodes.add(0, VRPModule.DEPOT)
                nodes
            }
        val cars = dbModule.getCars()
        vrpModule = VRPModule(requests, cars)

        val result = vrpModule.findPath()

        assert(result.routes.isNotEmpty())
    }

    @Test
    fun `вызов вычисления маршрута с количеством заявок = 500 без модификации`() {
        Constants.WITH_MODIFICATION = false

        val requests =
            (dbModule.getRequests(limit = 500, offset = 0).toMutableList() as MutableList<Node>).let { nodes ->
                nodes.add(0, VRPModule.DEPOT)
                nodes
            }
        val cars = dbModule.getCars()
        vrpModule = VRPModule(requests, cars)

        val result = vrpModule.findPath()

        assert(result.routes.isNotEmpty())
    }

    @Test
    fun `вызов вычисления маршрута с количеством заявок = 700 без модификации`() {
        Constants.WITH_MODIFICATION = false

        val requests =
            (dbModule.getRequests(limit = 700, offset = 0).toMutableList() as MutableList<Node>).let { nodes ->
                nodes.add(0, VRPModule.DEPOT)
                nodes
            }
        val cars = dbModule.getCars()
        vrpModule = VRPModule(requests, cars)

        val result = vrpModule.findPath()

        assert(result.routes.isNotEmpty())
    }

    @Test
    fun `вызов вычисления маршрута с количеством заявок = 1000 без модификации`() {
        Constants.WITH_MODIFICATION = false

        val requests =
            (dbModule.getRequests(limit = 1000, offset = 0).toMutableList() as MutableList<Node>).let { nodes ->
                nodes.add(0, VRPModule.DEPOT)
                nodes
            }
        val cars = dbModule.getCars()
        vrpModule = VRPModule(requests, cars)

        val result = vrpModule.findPath()

        assert(result.routes.isNotEmpty())
    }

    @Test
    fun `вызов вычисления маршрута с количеством заявок = 1200 без модификации`() {
        Constants.WITH_MODIFICATION = false

        val requests =
            (dbModule.getRequests(limit = 1200, offset = 0).toMutableList() as MutableList<Node>).let { nodes ->
                nodes.add(0, VRPModule.DEPOT)
                nodes
            }
        val cars = dbModule.getCars()
        vrpModule = VRPModule(requests, cars)

        val result = vrpModule.findPath()

        assert(result.routes.isNotEmpty())
    }

    @Test
    fun `вызов вычисления маршрута с количеством заявок = 1500 без модификации`() {
        Constants.WITH_MODIFICATION = false

        val requests =
            (dbModule.getRequests(limit = 1500, offset = 0).toMutableList() as MutableList<Node>).let { nodes ->
                nodes.add(0, VRPModule.DEPOT)
                nodes
            }
        val cars = dbModule.getCars()
        vrpModule = VRPModule(requests, cars)

        val result = vrpModule.findPath()

        assert(result.routes.isNotEmpty())
    }

    @Test
    fun `вызов вычисления маршрута с количеством заявок = 200 с модификацией`() {
        Constants.WITH_MODIFICATION = true

        val requests =
            (dbModule.getRequests(limit = 200, offset = 0).toMutableList() as MutableList<Node>).let { nodes ->
                nodes.add(0, VRPModule.DEPOT)
                nodes
            }
        val cars = dbModule.getCars()
        vrpModule = VRPModule(requests, cars)

        val result = vrpModule.findPath()

        assert(result.routes.isNotEmpty())
    }

    @Test
    fun `вызов вычисления маршрута с количеством заявок = 500 с модификацией`() {
        Constants.WITH_MODIFICATION = true

        val requests =
            (dbModule.getRequests(limit = 500, offset = 0).toMutableList() as MutableList<Node>).let { nodes ->
                nodes.add(0, VRPModule.DEPOT)
                nodes
            }
        val cars = dbModule.getCars()
        vrpModule = VRPModule(requests, cars)

        val result = vrpModule.findPath()

        assert(result.routes.isNotEmpty())
    }

    @Test
    fun `вызов вычисления маршрута с количеством заявок = 700 с модификацией`() {
        Constants.WITH_MODIFICATION = true

        val requests =
            (dbModule.getRequests(limit = 700, offset = 0).toMutableList() as MutableList<Node>).let { nodes ->
                nodes.add(0, VRPModule.DEPOT)
                nodes
            }
        val cars = dbModule.getCars()
        vrpModule = VRPModule(requests, cars)

        val result = vrpModule.findPath()

        assert(result.routes.isNotEmpty())
    }

    @Test
    fun `вызов вычисления маршрута с количеством заявок = 1000 с модификацией`() {
        Constants.WITH_MODIFICATION = true

        val requests =
            (dbModule.getRequests(limit = 1000, offset = 0).toMutableList() as MutableList<Node>).let { nodes ->
                nodes.add(0, VRPModule.DEPOT)
                nodes
            }
        val cars = dbModule.getCars()
        vrpModule = VRPModule(requests, cars)

        val result = vrpModule.findPath()

        assert(result.routes.isNotEmpty())
    }

    @Test
    fun `вызов вычисления маршрута с количеством заявок = 1200 с модификацией`() {
        Constants.WITH_MODIFICATION = true

        val requests =
            (dbModule.getRequests(limit = 1200, offset = 0).toMutableList() as MutableList<Node>).let { nodes ->
                nodes.add(0, VRPModule.DEPOT)
                nodes
            }
        val cars = dbModule.getCars()
        vrpModule = VRPModule(requests, cars)

        val result = vrpModule.findPath()

        assert(result.routes.isNotEmpty())
    }

    @Test
    fun `вызов вычисления маршрута с количеством заявок = 1500 с модификацией`() {
        Constants.WITH_MODIFICATION = true

        val requests =
            (dbModule.getRequests(limit = 1500, offset = 0).toMutableList() as MutableList<Node>).let { nodes ->
                nodes.add(0, VRPModule.DEPOT)
                nodes
            }
        val cars = dbModule.getCars()
        vrpModule = VRPModule(requests, cars)

        val result = vrpModule.findPath()

        assert(result.routes.isNotEmpty())
    }
}