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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private lateinit var viewModel: MainViewModel
    private lateinit var routeRepository: RouteRepository

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        //Mockito.reset(routeRepository)
        // RouteRepository를 모킹합니다.
        routeRepository = mock()

        // 테스트용 Dispatcher를 설정합니다.
        Dispatchers.setMain(testDispatcher)

        // 모킹된 repository를 사용하는 viewModel을 생성합니다.
        viewModel = MainViewModel(routeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        //viewModel = MainViewModel(routeRepository)
    }

    @Test
    fun `fetchLocations 성공적으로 호출시 mainViewState 상태가 업데이트 된다`() = runTest {
        // Given: Mocked repository returns a successful response with specific locations
        val mockedLocations = LocationsResponse(
            locations = listOf(
                OriginDestination("서울역", "판교역"),
                OriginDestination("대전역", "성심당"),
                OriginDestination("강남역", "알파돔타워"),
                OriginDestination("수서역", "마곡역"),
            )
        )
        `when`(routeRepository.getLocations()).thenReturn(Result.success(mockedLocations))

        // When: ViewModel이 생성되면 init 블록에서 자동으로 fetchLocations 호출됨
        viewModel = MainViewModel(routeRepository)

        // Then: ViewState가 업데이트 되었는지 확인
        viewModel.mainViewState.test {
            // 상태 변화가 발생하는지 확인
            val viewState = awaitItem()
            assert(viewState is MainViewState.ListView)
            val expectedLocations = mockedLocations.locations
            assertEquals(expectedLocations, (viewState as MainViewState.ListView).locations)
        }
    }

    @Test
    fun `fetchRoute가 ApiException 발생 시 에러 상태로 전환된다`() = runTest {
        // Given: Mocked repository returns an ApiException
        val location = OriginDestination("서울역", "판교역")
        val exception = ApiException(4041, "not_found")

        // mock the repository to return an ApiException
        `when`(routeRepository.getRoute(location.origin, location.destination))
            .thenReturn(Result.failure(exception))

        // When: ViewModel fetchRoute 호출
        viewModel.fetchRoute(location)

        // Then: ErrorViewState가 업데이트 되었는지 확인
        viewModel.errorViewState.test {
            val errorState = awaitItem()
            // 상태가 올바르게 업데이트되었는지 확인
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
        // Given: Mocked repository returns successful routes and distance time
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

        // Mock the repository to return the routes and distance time
        `when`(routeRepository.getRoute(location.origin, location.destination))
            .thenReturn(Result.success(mockedRoutesResponse)) // returning RouteResponse

        `when`(routeRepository.getDistanceTime(location.origin, location.destination))
            .thenReturn(Result.success(mockedDistanceTime))

        // When: fetchRoute 호출
        viewModel.fetchRoute(location)

        // Then: ViewState가 업데이트 되었는지 확인
        viewModel.mainViewState.test {
            val viewState = awaitItem()
            assert(viewState is MainViewState.MapView)
            assertEquals(mockedRoutes, (viewState as MainViewState.MapView).routes)
            assertEquals(mockedDistanceTime, viewState.distanceTime)
        }
    }
}