# 과제 프로젝트
- 주어진 출발지 / 도착지에 따라 도로 상태 및 기타 정보를 보여주는 앱 만들기

### 뷰 요구사항
- 출발지 도착지 리스트 화면
    - API 주어짐
- 지도 화면 (출발 도착 리스트 클릭시)
    - 도로 선 표시(네이밍 변경)
    - 도로 선 색상 변경
    - 마커 표시
    - 시간, 거리표시 
    - 주의: 마커, 경로 화면진입시 한화면에 보여줘야함(화면 축적 조절)
- 에러 바텀시트
    - 경로가 없는경우
        - 에러코드 표시(서버 제공)
        - 메시지 표시(서버 제공)
    - 알수 없는 에러
        - API 에러인지 표시 -> (엔트포인트)

### 프로젝트 구조
프로젝트는 다음과 같이 구성되어 있습니다
```mermaid
graph TD;
    A[메인 화면(지도 화면)] --> B[출발지/도착지 목록 API 호출]
    B --> C[출발지/도착지 리스트 뷰 표시]
    C --> D[사용자 아이템 클릭]
    D --> E[지도 경로 API 호출]
    
    E --> |성공 시| F[거리/시간 API 호출]
    F --> G[지도에 경로, 거리, 시간 표시 및 출발지/도착지 리스트 뷰 숨김]
    
    E --> |실패 시| H[에러 바텀 시트 표시]
```

- MainActivity: 지도 초기화, 경로 표시 및 UI 업데이트 등의 주요 로직을 처리합니다.
- MainViewModel: API 요청을 관리하고 경로 및 위치 데이터를 처리한 후 StateFlow를 통해 액티비티에 데이터를 노출하는 역할을 합니다.
- LocationListAdapter: 출발지/도착지 목록을 표시하는 RecyclerView의 어댑터입니다.
- ErrorBottomSheetFragment: API 요청 실패 시 오류 메시지를 표시하는 바텀 시트입니다.

### 주요 기능
- 출발지/도착지 목록 표시
  - locations API를 사용하여 가능한 출발지 및 도착지 목록을 받아와 RecyclerView에 표시합니다.
  - 목록에서 출발지/도착지를 선택하면 해당 경로를 요청합니다. 
- 지도에 경로 표시
  - 사용자가 출발지/도착지 쌍을 선택하면 routes API를 통해 경로 데이터를 요청합니다.
- 경로 정보 표시
  - distance-time API를 사용하여 경로의 거리와 예상 소요 시간을 가져옵니다.
  - 지도 상단에 총 이동 시간과 거리를 표시합니다. 
- 오류 처리
  - API에서 에러가 반환될 경우(예: 경로 없음), 서버에서 받은 오류 코드와 메시지를 바텀 시트에 표시합니다.
  - 서버 메시지가 없을 경우에는 API 요청에서 발생한 오류의 종류를 간략하게 표시합니다. 
- 지도 기능
  - 경로의 시작점과 끝점에 마커를 추가합니다.
  - 경로가 로드되면 자동으로 카메라가 조정되어 경로가 화면에 맞게 표시됩니다.
- UI 애니메이션
  - 경로가 표시될 때 출발지/도착지 목록이 부드러운 페이드아웃 애니메이션으로 숨겨집니다.
- API 통합
이 앱은 3개의 API를 사용하여 동작합니다:

  - 위치 목록 API: 출발지 및 도착지 목록을 가져옵니다.
  - 경로 API: 선택한 출발지와 도착지 사이의 경로를 가져옵니다.
  - 거리 및 시간 API: 선택한 경로에 대한 거리 및 예상 시간을 가져옵니다.

각 API 호출은 MainViewModel에서 비동기적으로 처리되며, 결과는 StateFlow를 통해 UI에 노출됩니다.

#### 사용 방법
1. 앱을 실행하면 출발지 및 도착지 목록이 표시됩니다.
2. 출발지 및 도착지 쌍을 선택하면 해당 경로가 지도에 표시됩니다.
3. 경로와 함께 교통 상황, 거리, 소요 시간이 표시됩니다.
4. 오류가 발생하면 바텀 시트로 오류 메시지가 표시됩니다.
#### 오류 처리
- API 에러가 발생할 경우 사용자 친화적인 오류 메시지를 표시하여 문제를 알려줍니다.
- 오류 상태는 MainViewModel에서 관리되고 StateFlow를 통해 MainActivity에 전달됩니다.
#### 테스트
이 앱은 MainViewModel 클래스에 대한 단위 테스트를 포함하고 있으며, 다음 시나리오를 다룹니다:

- 위치 목록을 성공적으로 가져오고 UI 상태가 업데이트되는지 확인.
- 경로 조회 중 API 예외가 발생할 경우 오류 상태가 올바르게 처리되는지 확인.
- 경로 및 거리/시간 조회가 성공적으로 처리되고 뷰 모델의 상태가 올바르게 업데이트되는지 검증.

## 뷰 요구사항

- 출발지 도착지 리스트 화면 / 기획서 2p 1번 항목

    - API 주어짐

- 지도 화면 (출발 도착 리스트 클릭시) / 기획서 2P 2번 항목

    - 도로 선 표시(네이밍 변경)
    - 도로 선 색상 변경
    - 마커 표시
    - 시간, 거리표시

  주의: 마커, 경로 화면진입시 한화면에 보여줘야함(화면 축적 조절)

- 에러 바텀시트
    - 경로가 없는경우
        - 에러코드 표시(서버 제공)
        - 메시지 표시(서버 제공)
    - 알수 없는 에러
        - API 에러인지 표시 -> (해당 엔트포인트를 유저에게 보여주는은 부적절하다 판단, 이를 대처할 방법 강구)