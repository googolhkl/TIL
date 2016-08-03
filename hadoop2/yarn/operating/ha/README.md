# 리소스매니저 HA 구성
##### 얀 아키텍처에서 리소스매니저는 마스터 서버로서 다양한 역할을 수행한다. 하지만 리소스매니저가 다운될 경우 노드매니저가 살아 있더라도 얀 클러스터 전체를 이용할 수 없다. 왜냐하면 클라이언트의 요청을 처리할 서버가 없고, 실행 중이거나 실행 요청이 들어온 애플리케이션이 필요한 리소스를 스케줄링할 수 없기 때문이다. 그래서 리소스매니저는 SPOF(Single Point Of Failure)였으며, 하둡 커뮤리티도 이 문제점을 해결하기 위해 노력해왔다. 하둡 2.4.0버전부터 리소스매니저의 HA가 적용되어 리소스매니저에 대한 장애 복구 부담을 덜 수 있게 됐다. 리소스매니저의 HA 구성은 네임노드의 HA 구성과 유사한 방식으로 설계돼 있다. 아래 그림은 리소스매니저의 HA 아키텍처를 나타낸다.

![리소스매니저 HA아키텍처](https://github.com/googolhkl/TIL/blob/master/hadoop2/yarn/operating/resources/ResourceManagerHA.png)
##### 리소스매니저의 HA 아키텍처는 다음과 같은 세 개의 컴포넌트로 구성된다.
- 주키퍼 : 리소스매니저의 공통 저장소다.
- ZKRMStateStore(org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore) : 리소스매니저의 상태 정보를 주키퍼에 저장하는 역할을 담당한다.
- EmbeddedElectorService(org.apache.hadoop.yarn.server.resourcemanager.EmbeddedElectorService) : 리소스매니저에 내장돼 있는 HA 상태 관리 컴포넌트다. 이 컴포넌트는 주키퍼 정보를 모니터링해서 자신을 실행한 리소스매니저의 HA 상태를 갱신한다. 예를 들어, 액티브 리소스매니저가 실행된 상태에서 스탠드바이 리소스매니저를 실행하면 EmbeddedElectorService는 해당 리소스매니저의 HA상태를 스탠드바이로 갱신한다. 그리고 주키퍼 정보를 모니터링하는 중 액티브 리소스매니저가 주키퍼 정보를 갱신하지 못하고 있을 경우 이를 장애 상태로 인지하고 스탠드바이 리소스매니저를 액티브 리소스매니저로 변경한다.

##### 네임노드의 HA가 ZKFC라는 별도의 데몬을 실행하는 것과 달리 리소스매니저의 HA는 내장돼 있는 EmbeddedElectorService로 HA 상태를 갱신할 수 있다. 다만 네임노드의 HA가 stop-dfs.sh, start-dfs.sh로 한 번에 실행되는 것과는 달리 스탠드바이 리소스매니저를 해당 호스트에 접속해 `yarn-daemon.sh`로 실행해야한다는 번거로움이 있다. 대신 커맨드 라인에서 상태정보를 인위적으로 변경하거나 장애 발생 시 자동으로 복구되는 기능은 정상적으로 진행 된다.

## 리소스매니저 HA 환경설정
##### 리소스매니저 HA의 환경설정은 yarn-site.xml에 설정해야만 한다. 이때 설정 가능한 속성값은 네임노드 HA의 속성값고 매우 유사하다. 아래 표는 yarn-site.xml에서 설정 가능한 속성값을 나타낸다.

| 속성 | 내용|
| --- | --- |
| yarn.resourcemanager.ha.enabled | 리소스매니저 HA 기능의 사용 여부를 설정한다. 기본값은 false로 되어있다. |
| yarn.resourcemanager.ha.rm-ids | 액티브 리소스매니저, 스탠드바이 리소스매니저 아이디를 등록한다. 콤마 단위로 구분하며, 가상의 아이디를 부한다. 네임노드 HA에서 각 네임노드에 아이디를 부여하는 것과 동일한 방식이다. | 
| yarn.resourcemanager.ha.id | 현재 실행되는 리소스매니저의 아이디다. 예를 들어, yarn.resourcemanager.ha.rm-ids에 rm1, rm2를 등록하고, 현재 rm1을 실행했다면 이 속성에는 반드시 rm1을 설정해야 한다. |
| yarn.resourcemanager.address.<rm-id> | 액티브/스탠드바이 리소스매니저의 주소다. <rm-id>에는 yarn.resourcemanager.ha.rm-ids에서 등록한 아이디를 사용한다. |
| yarn.resourcemanager.scheduler.address.<rm-id> | 액티브/스탠드바이 리소스매니저의 스케줄러 주소다. <rm-id>에는 yarn.resourcemanager.ha.rm-ids에서 등록한 아이디를 사용한다. |
| yarn.resourcemanager.admin.address.<rm-id> | 액티브/스탠드바이 리소스매니저의 RM Admin 주소다. <rm-id>에는 yarn.resourcemanager.ha.rm-ids에서 등록한 아이디를 사용한다. |
| yarn.resourcemanager.resource-tracker.address.<rm-id> | 액티브/스탠드바이 리소스매니저의 리소스트래커 주소다. <rm-id>에는 yarn.resourcemanager.ha.rm-ids에서 등록한 아이디를 사용한다. |
| yarn.resourcemanager.webapp.address.<rm-id> | 액티브/스탠드바이 리소스매니저의 웹 주소이다. <rm-id>에는 yarn.resourcemanager.ha.rm-ids에서 등록한 아이디를 사용한다. |
| yarn.resourcemanager.recovery.enabled | 리소스매니저의 자동 잡 복구 기능의 사용 여부다. 기본값으로 false로 설정돼있다. |
| yarn.resourcemanager.zk-address | HA 상태 정보를 저장하는 주키퍼 서버의 정보다. 주키퍼 호스트와 포트를 등록한다. 각 주키퍼 서버는 콤마로 구분한다. |
| yarn.resourcemanager.ha.automatic-failover.enabled | 리소스매니저 HA의 자동 장애복구 기능의 사용 여부다. 기본값은 true다. |
| yarn.resourcemanager.ha.automatic-failover.embedded | 리소스매니저 HA의 자동 장애복구 시 EmbeddedElectorService 사용 여부를 나타낸다. 기본값은 true다. |
| yarn.resourcemanager.cluster-id | 액티브 리소스매니저와 스탠드바이 리소스매니저를 묶어서 가상의 클러스터 아이디를 부여한다. 네임노드 HA에서 클러스터 아이디를 부여하는 것과 동일한 방식이다. | 
<br />

## 리소스매니저 HA 테스트
