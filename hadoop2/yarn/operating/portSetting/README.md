# 포트 설정
##### 얀의 주요 데몬들이 사용하는 포트를 알아보자. 얀은 크게 두가지 포트로 구분된다.
- HTTPP 포트
- IPC 포트

## HTTP 포트
##### HTTP 포트는 사용자가 웹에서 접근할 때 사용하는 포트다. 다음표는 HTTP 포트를 나타낸다. 참고로 두 번째 열의 옵션값은 yarn-site.xml에서 설정 가능한 옵션이다.

| 데몬 | 옵션이름 | 기본포트|
| --- | --- | --- |
| 리소스매니저 | yarn.resourcemanager.webapp.address | 8088 |
| 잡히스토리 서버 | yarn.log.server.url | 19888 |
<br />

## IPC 포트
##### IPC 포트는 InterProcess Communication의 약자로, 내부 컴포넌트들이 통신할 때 사용하는 포트다. 예를 들어, 리소스매니저는 노드매니저나 클라이언트가 접근할 수 있도록 8050 포트를 사용한다. 다음 표는 얀의 주요 IPC 포트를 나타낸다. yarn-site-xml에서 설정할 수있다. 참고로 포트가 0으로 설정될 경우 사용 가능한 포트를 무작위로 조회해서 사용하게 된다.

| 컴포넌트 | 옵션 이름 | 기본 포트 |
| --- | --- | --- |
| 리소스매니저 | yarn.resourcemanager.address | 8032 |
| 리소스매니저 내부 리소스트래커 | yarn.resourcemanager.resource-tracker.address | 8031 |
| RM APIs webapp | yarn.resourcemanager.webapp.https.address | 8090 |
| RM 스케줄러 | yarn.resourcemanager.scheduler.address | 8030 |
| 노드매니저 | yarn.nodemanager.address | 0 |
| RM Admin | yarn.resourcemanager.admin.address | 8033 |
<br />
