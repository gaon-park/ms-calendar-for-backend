# ms-calendar-for-backend
- 메이플스토리 확률형 아이템 기록 검증기, 공유 캘린더 (유틸 사이트 백엔드 서버)
- [큐브 사용 결과 API](https://developers.nexon.com/Maplestory/api/15/47) 사용
- 확률형 아이템의 기록을 검색하고 공시 확률의 정확도를 검증
- 파티플레이를 위한 공유 캘린더 기능
- 유저 간 팔로우 시스템 구현

## 목차
1. 기술 스택
2. 운영 관련
3. 기능 사항
    1. 상세 API 확인 (swagger)
    2. 유저 관련
    3. 일정 관련
    4. 큐브 이력 관련

## 기술 스택

* [frontend repository](https://github.com/gaon-park/ms-calendar-for-frontend)

* backend
<table>
    <thead>
        <tr>
            <th>분류</th>
            <th>기술</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Language</td>
            <td>Kotlin</td>
        </tr>
        <tr>
            <td>Framework</td>
            <td>SprintBoot</td>
        </tr>
        <tr>
            <td>DB</td>
            <td>MySQL</td>
        </tr>
        <tr>
            <td rowspan="4">GCP</td>
            <td>Compute Engine</td>
        </tr>
        <tr>
            <td>IAM</td>
        </tr>
        <tr>
            <td>Cloud Storage</td>
        </tr>
        <tr>
            <td>VPC 네트워크</td>
        </tr>
        <tr>
            <td>Server</td>
            <td>Nginx (Reverse Proxy)</td>
        </tr>
    </tbody>
</table>


## 운영 관련 (메이플 inven 이용)
- [테스트 도움!! 큐브 기록 검색 사이트](https://www.inven.co.kr/board/maple/5974/940125)
- [나의 등업 확률을 확인하자! 기능 설명편](https://www.inven.co.kr/board/maple/5974/989320)
- [서버 내립니다😥](https://www.inven.co.kr/board/maple/5974/1400582)

## 기능 사항
### 상세 API 확인 (swagger)
- json 형식: https://github.com/gaon-park/ms-calendar-for-backend/blob/master/api-docs.json
- yaml 형식: https://github.com/gaon-park/ms-calendar-for-backend/blob/master/api-docs.yml
- 로컬 서버 기동시: http://127.0.0.1/api-docs/
- 서비스 서버: https://ms-hero.kr/api-docs/
    ** 관리자 ID/PW 필요

### 유저 관련
0. Google OAuth를 통해 신규 가입/로그인합니다.
1. 계정을 비공개로 설정하는 경우, 팔로워가 아닌 유저에게는 '팔로워', '팔로우' 리스트를 제공하지 않습니다.
    - 계정의 다양한 정보를 설정할 수 있습니다.

   ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/37a7d7df-8553-490b-a104-8509ab578d29)

2. 팔로우 요청을 하거나, 팔로우를 취소할 수 있습니다.
    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/c5de095b-b812-4b62-b191-2faf5800ec4e)

    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/3a69d396-b76d-4ce4-8468-4a28d7859d44)


4. 팔로잉, 팔로워을 관리할 수 있습니다.
    - 팔로우 요청을 승인
    - 나의 팔로워에서 삭제(팔로우 요청을 거절)
  
    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/22d59b52-09cc-4176-809a-04259b55a673)


5. 검색은 한 번에 최대 1000건 까지 가능합니다. 결과값이 1000건 이상으로 중복될 때 키워드나, 필터를 사용해 검색 범위를 줄여주세요.

    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/deea0fd4-e487-4ecd-a15f-4128fe299c4b)

7. 큐브 데이터를 확인하기 위한 API KEY 등록이 필요합니다.
    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/ec9aedfd-6caf-4a78-8784-3282b1f7206d)

8. 알림을 통해 실시간으로 팔로우 요청, 스케줄 초대, 상대방의 팔로우/스케줄 수락 정보를 받을 수 있습니다.

    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/a4751050-b684-4dc2-882e-c8c479291391)

### 일정 관련
0. 메이플 공식 이벤트와 나의 일정, 계정 상태가 공개이거나 팔로우 중인 유저의 공개 일정을 확인할 수 있습니다.

    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/dbb70a1d-beb2-4058-82ab-0a7f1686a530)

1. 일정을 생성할 때 다양한 설정을 할 수 있습니다.
    - 계정의 공개, 비공개 상태에 관계없이 '참석자만'을 설정한 일정이면, 일정의 멤버만 보거나 수정할 수 있습니다.
    - 계정이 비공개 상태라면, '모두에게' 일정은 일정의 멤버, 혹은 팔로워만 볼 수 있습니다.
    - 일정 추가, 수정 시 멤버 초대는 팔로워이거나 계정이 공개 상태인 유저에게만 보낼 수 있습니다.
    - 반복 일정은 종료일 미설정 시, 최대 1년 후까지 반복됩니다.

    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/45690357-27c7-4136-afa2-487b52b903c9)

### 큐브 이력 관련
0. [메이플스토리 공식 사이트의 공시 확률](https://maplestory.nexon.com/Guide/OtherProbability/cube/red)과 유저의 실제 큐브 이벤트를 비교/검증합니다.
    - 수상한 큐브
    - 수상한 에디셔널 큐브
    - 장인의 큐브
    - 명장의 큐브
    - 이벤트링 전용 장인의 큐브
    - 이벤트링 전용 명장의 큐브
    - 레드 큐브
    - 블랙 큐브
    - 에디셔널 큐브

   ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/93117bbb-c188-4198-b1ec-c39afb7d96b1)

2. 전체 등록 유저와 나의 큐브 사용 이력을 확인할 수 있습니다.
    - 최근 한 달 데이터
    - 최근 두 달 데이터
    - 최근 세 달 데이터
    - 전체 누적 데이터
   
    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/974c69d5-ef7f-46aa-8ac2-063f98ceb209)

    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/c74414b0-3c74-4122-8de2-a2be84b1a6b1)

3. 나의 이력을 확인할 때, 아이템 별 확률 차이를 추가로 확인할 수 있습니다.
    - ex
    - `트와일라이트 마크` 아이템에 대해서 운이 상당히 나빴지만
   
    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/1d766f05-3e20-4497-a80f-a84a8a932bc8)

    - `데이브레이크 펜던트` 아이템에 대해서 운이 상당히 좋았다는 걸 알 수 있습니다.

    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/82d064e2-4a72-45c5-825b-2e9b6c8b1ead)

    
5. 나의 상세 이력을 확인할 수 있습니다. 
    - export 버튼을 통해 csv 형식으로 저장도 가능합니다.

    ![image](https://github.com/gaon-park/ms-calendar-for-backend/assets/52269983/6ffdf851-4729-4ec6-9695-902457836da9)


