package com.example.kakaomapproject

import app.cash.turbine.test
import com.example.data.RouteRepository
import com.example.data.response.ApiException
import com.example.data.response.DistanceTime
import com.example.data.response.LocationsResponse
import com.example.data.response.OriginDestination
import com.example.data.response.RouteResponse
import com.example.kakaomapproject.model.Route
import com.example.kakaomapproject.model.RouteError
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private lateinit var viewModel: MainViewModel
    private lateinit var routeRepository: RouteRepository

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        routeRepository = mockk()

        viewModel = MainViewModel(routeRepository)

        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchLocations 성공적으로 호출시 mainViewState 상태가 업데이트 된다`() = runTest {
        val mockedLocations = LocationsResponse(
            locations = listOf(
                OriginDestination("서울역", "판교역"),
                OriginDestination("대전역", "성심당"),
                OriginDestination("강남역", "알파돔타워"),
                OriginDestination("수서역", "마곡역"),
            )
        )
        coEvery { routeRepository.getLocations() } returns Result.success(mockedLocations)

        viewModel.fetchLocations()
        advanceUntilIdle()

        val viewState = viewModel.mainViewState.first()
        assert(viewState is MainViewState.ListView)
        assertEquals((viewState as MainViewState.ListView).locations, mockedLocations.locations)
    }

    @Test
    fun `fetchRoute가 ApiException 발생 시 에러 상태로 전환된다`() = runTest {
        val location = OriginDestination("서울역", "판교역")
        val exception = ApiException(4041, "not_found")

        coEvery { routeRepository.getRoute(location.origin, location.destination) }
            .returns(Result.failure(exception))

        viewModel.fetchRoute(location)
        advanceUntilIdle()

        viewModel.errorViewState.test {
            val errorState = awaitItem()
            assertEquals(
                RouteError(
                    RouteError.getRouteErrorPath(location),
                    exception.code,
                    exception.errorMessage
                ),
                errorState
            )
        }
    }

    @Test
    fun `fetchRoute 호출 시 fetchDistanceTime이 성공적으로 호출되고 ViewState가 업데이트된다`() = runTest {
        val location = OriginDestination("서울역", "판교역")
        val mockedRoutesResponse = listOf(
            RouteResponse(
                points = "126.97227318174524,37.55595957732287 126.97216162420102,37.55584147066708",
                trafficState = "UNKNOWN"
            ),
            RouteResponse(
                points = "126.97216162420102,37.55584147066708 126.97206311621702,37.55559733155492",
                trafficState = "UNKNOWN"
            )
        )
        val mockedRoutes = mockedRoutesResponse.map { Route.fromRouteResponse(it) }

        val mockedDistanceTime = DistanceTime(24427, 2652)

        coEvery { routeRepository.getRoute(location.origin, location.destination) }
            .returns(Result.success(mockedRoutesResponse))

        coEvery { routeRepository.getDistanceTime(location.origin, location.destination) }
            .returns(Result.success(mockedDistanceTime))

        viewModel.fetchRoute(location)

        viewModel.mainViewState.test {
            val viewState = awaitItem()
            assert(viewState is MainViewState.MapView)
            assertEquals(mockedRoutes, (viewState as MainViewState.MapView).routes)
            assertEquals(mockedDistanceTime, viewState.distanceTime)
        }
    }
}