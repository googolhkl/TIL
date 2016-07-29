# 얀 아키텍처의 이해
## 얀의 전체적인 작업흐름
##### 얀의 전체적인 흐름을 설명하고 단계적 동작 방식을 설명한다.
![얀 작업흐름](https://github.com/googolhkl/TIL/blob/master/hadoop2/yarn/architecture/YarnWorkflow.png)

##### 1. 클라이언트는 애플리케이션 실행을 요청한다. 애플리케이션은 얀 API를 구현한 프로그램이면 어떤 애플리케이션이든 실행 가능하다. 이때 리소스매니저는 실행 요청이 유효할 경우 클라이언트에게 신규 애플리케이션ID를 할당한다.
##### 2. 리소스매니저는 노드매니저에게 애플리케이션마스터 실행을 요청한다.
##### 3. 노드매니저는 리소스매니저에게 요청을 받은 후 컨테이너에서 애플리케이션마스터를 실행한다. 이때 컨테이너는 새로운 JVM을 생성해 애플리케이션마스터를 실행한다.
##### 4. 애플리케이션마스터는 리소스매니저에게 애플리케이션을 실행하기 위한 리소스를 요청한다. 이때 리소스는 필요한 호스트와 랙 정보, 메모리, CPU, 네트워크 정보, 그리고 컨테이너 개수 등으로 구성된다. 리소스매니저는 전체 클러스터의 리소스 상태를 확인한 후 애플리케이션마스터에게 노드매니저 목록을 전송한다.
##### 5. 애플리케이션마스터는 리소스매니저에게 할당받은 노드매니저들에게 컨테이너 실행을 요청한다.
##### 6. 노드매니저들은 컨테이너에 새로운 JVM을 생성한 후 해당 애플리케이션을 실행한다. 애플리케이션이 종료되면 해당 애플리케이션마스터가 종료된다. 마지막으로 리소스매니저는 종료된 애플리케이션마스터에게 할당했던 리소스를 해제한다.

## 얀 단계별 동작 방식
##### 위에서 얀의 전체적인 작업 흐름을 보았다. 하지만 실제로 각 컴포넌트 간에는 더 복잡한 상호작용이 발생한다.  
### 1. 애플리케이션 실행 요청 (MyClient.java)
##### 아래 그림은 YarnClient를 구현한 클래스인 클라이언트가 얀 클러스터에 애플리케이션 실행을 요청했을 때 진행되는 과정을 나타낸다. ClientRMService는 리소스매니저의 내부 컴포넌트로 클라이언트가 호출하는 모든 RPC 호출을 처리한다.
##### 이 부분은 얀의 전체적인 작업흐름에서 1. 애플리케이션 실행요청에 해당한다.
![애플리케이션 실행 요청](https://github.com/googolhkl/TIL/blob/master/hadoop2/yarn/architecture/ApplicationExecutingRequest.png)

#####  1. 클라이언트가 애플리케이션을 얀 클러스터에서 실행하려면 얀 클러스터에서 신규 애플리케이션ID를 발급받아야 한다. 클라이언트는 ClientRMService의 createNewApplication 메소드를 호출해 애플리케이션ID 발급을 요청한다. 이 부분은 [Myclient.java](https://github.com/googolhkl/TIL/blob/master/hadoop2/yarn/application/com/hkl/hadoop/yarn/examples/MyClient.java#L185)에 다음과 같이 작성되어 있다.

```java
YarnClientApplication app = yarnClient.createApplication();
```

##### 2. ClientRMService는 클라이언트의 요청에 신규 애플리케이션 ID와 얀 클러스터에서 최대로 할당할 수 있는 리소스 정보가 설정돼 있는 GetNewApplicationResponse 객체를 반환한다. 이 부분은 [Myclient.java](https://github.com/googolhkl/TIL/blob/master/hadoop2/yarn/application/com/hkl/hadoop/yarn/examples/MyClient.java#L186)에 다음과 같이 작성되어 있다.

```java
GetNewApplicationResponse appResponse = app.getNewApplicationResponse();
```

##### 3. 클라이언트는 애플리케이션 ID가 정상적으로 반환됐는지 확인한 후 ClientRmService의 submitApplication 메소드를 호출한다. 이때 파라미터로 ApplicationSubmissionContext를 전달한다. ApplicationSubmissionContext는 리소스매니저가 해당 애플리케이션의 애플리케이션마스터를 실행하기 위한 다음과 같은 정보를 포함하고 있다.
##### - 애플리케이션 ID
##### - 애플리케이션 이름
##### - 애플리케이션이 사용할 큐(Queue)의 이름
##### - 애플리케이션의 우선순위
##### - 애플리케이션이 필요한 리소스
##### - 애플리케이션마스터를 실행할 컨테이너 정보가 담긴 ContainerLaunchContext 객체
##### 참고로 애플리케이션은 얀 클러스터의 스케줄러에 설정돼 있는 큐만 사용할 수 있다.
##### 위의 정보를 설정하고 submitApplication을 실행한다. 이 부분은 [Myclient.java](https://github.com/googolhkl/TIL/blob/master/hadoop2/yarn/application/com/hkl/hadoop/yarn/examples/MyClient.java#L234)에 다음과 같이 작성되어 있다.

```java
yarnClient.submitApplication(appContext);
```


##### 4. 클라이언트는 submitApplication을 호출한 후 리소스매니저에게 ApplicationReport를 요청한다. 이때 클라이언트는 ClientRMService의 getApplicationReport를 호출한다. 이 부분은 [Myclient.java](https://github.com/googolhkl/TIL/blob/master/hadoop2/yarn/application/com/hkl/hadoop/yarn/examples/MyClient.java#L379)에 monitorApplication메소드에 다음과 같이 작성되어 있다.

```java
ApplicationReport report = yarnClient.getApplicationReport(appId);
```

##### 5. 리소스매니저는 애플리케이션이 정상적으로 등록됐을 경우 ApplicationReport를 반환하다. ApplicationReport는 얀 클러스터에서 실행되는 애플리케이션의 통계 정보를 제공하며, 애플리케이션 ID, 이름, 사용자, 큐, 애플리케이션마스터 정보, 애플리케이션 구동 시간 등의 정보를 포함하고 있다. 반환된 ApplicationReport를 가지고 오류를 판단하는 부분은 [Myclient.java](https://github.com/googolhkl/TIL/blob/master/hadoop2/yarn/application/com/hkl/hadoop/yarn/examples/MyClient.java#L382) 부터 정의 되어있다.


### 2. 애플리케이션마스터 실행 요청 및 fork
##### 애플리케이션이 실행되려면 애플리케이션의 라이프 사이클을 관리하는 애플리케이션마스터가 실행돼야 한다.
##### 이 부분은 얀의 전체적인 작업흐름에서 2.애플리케이션마스터 실행요청에 해당한다.
![애플리케이션마스터 실행요청](https://github.com/googolhkl/TIL/blob/master/hadoop2/yarn/architecture/ApplicationMasterExecutingRequest.png)


##### 1. 애플리케이션 목록을 관리하는 컴포넌트인 RMAppManager는 리소스매니저의 내부 스케줄러에게 애플리케이션 등록 및 애플리케이션마스터를 실행하기 위한 컨테이너를 요청한다. 
##### 참고로 리소스매니저에서 실행되는 애플리케이션은 시도하는 횟수만큼 ApplicationAttemptId가 생성된다. 그리고 내부 스케줄러에 애플리케이션을 등록할 때 ApplicationAttemptId를 사용하게 된다.

##### 2. ApplicationAttemptId를 애플리케이션이 사용하는 큐에 등록한다. 그리고 RMAppManager가 스케줄 등록 결과를 알 수 있게 RMAppAttemptEvennType.ATTEMPT_ADDED 이벤트를 발생시킨다.

##### 3. RMAppManager는 스케줄러에게 ApplicationAttemptId에 대한 컨테이너 할당을 요청한다. 
##### 4. 스케줄러는 ApplicationAttemptId에게 컨테이너를 할당한 후 RMAppManager가 애플리케이션마스터를 실행할 수 있게 RMContainerEventType.START를 발생시킨다.
##### 5. 스케줄러의 응답을 받은 RMAppManager는 애플리케이션마스터를 실행하는 컴포넌트인 ApplicationMasterLauncher를 실행한다.
##### 6. ApplicationMasterLauncher는 AMLauncher를 실행해 애플리케이션마스터를 실행한다. 
##### 7. AMLauncher는 컨테이너 정보를 설정한 후 노드매니저에게 애플리케이션마스터 실행을 요청한다. 이때 파라미터로 ContainerLaunchContext를 이용한다. 얀 클러스터에서 실행되는 모든 애플리케이션은 컨테이너에서 실행되며, ContainerLaunchContext에 노드매니저가 컨테이너를 실행하는데 필요한 다음과 같은 정보가 저장돼 있다.
##### - 컨테이너 ID
##### - 컨테이너에 할당된 리소스 정보
##### - 컨테이너 사용자 정보
##### - 보안 정보
##### - 컨테이너를 실행하는데 필요한 바이너리 파일, JAR 파일, XML 파일과 같은 로컬 파일 정보
##### - 환경설정 정보
##### - 컨테이너를 실행할 커맨드 라인
##### 참고로 얀에서 맵리듀스를 실행할 경우 AMLauncher는 ContainerLaunchContext의 커맨드 라인을 다음과 같은 형태로 설정한다. 여기서 MRAppMaster는 맵리듀스 애플리케이션의 애플리케이션마스터 역할을 하는 클래스다.
```
$ JAVA_HOME/bin/java -Dlog4j.configuration=container-log4j.properties -Dyarn.app.container.log.dir=<LOG_DIR>
-Dyarn.app.container.log.filesize=0
-Dhadoop.root.logger=INFO,CLA
-Xmx1024m
org.apache.hadoop.mapreduce.v2.app.MRappMaster 1>
<LOG_DIR>/stdout2><LOG_DIR>/stderr
```

##### 8. 노드매니저는 AMLauncher가 요청한 컨테이너를 실행한 후 결과가 저장돼 있는 StartContainerResponse를 반환한다. 이 부분은 얀의 전체적인 작업흐름에서 3번(fork) 과정에 속한다.

### 3. 애플리케이션마스터 등록 (MyApplicationMaster.java)
##### 노드매니저가 애플리케이션을 정상적으로 실행했을 경우 해당 애플리케이션마스터가 리소스매니저에게 등록돼야 한다. 왜냐하면 리소스매니저는 클러스터 내에서 실행되는 여러 개의 애플리케이션마스터에게 자원을 할당하고, 상태를 모니터링해야 하기 때문이다. 아래 그림은 애플리케이션마스터가 리소스매니저에 등록되는 과정을 보여준다.
![애플리케이션마스터 등록](https://github.com/googolhkl/TIL/blob/master/hadoop2/yarn/architecture/ApplicationMasterRegistering.png)

##### 1. ApplicationMasterProtocol은 리소스매니저와 애플리케이션마스터 간에 필요한 다음과 같은 인터페이스가 정의돼있다. 참고로 얀은 ApplicationMasterProtocol을 구현한 기본 클라이언트인 AMRMClient와 AMRMClientAsync를 제공한다. 또는 기본 클라이언트를 사용하지 않고, 개발자가 직접 MasterProtocol 인터페이스를 구현할 수도 있다.
##### - 리소스매니저에게 애플리케이션마스터 등록(registerApplicationMaster)
##### - 리소스매니저에게 애플리케이션마스터 해제(finishApplicationMaster)
##### - 리소스매니저에게 리소스 할당 요청(allocate)
##### 애플리케이션마스터의 클라이언트는 registerApplicationMaster 메소드를 호출해 애플리케이션마스터 등록을 요청한다. 이때 registerApplicationMaster의 파라미터에는 애플리케이션마스터의 호스트명, 포트, 애플리케이션마스터의 상태를 모니터링하기 위한 URL이 포함된다. 이 부분은 [MyApplicationMaster.java](https://github.com/googolhkl/TIL/blob/a7290b5fde0d1c809c95ae47c32647dad2afb2fa/hadoop2/yarn/application/com/hkl/hadoop/yarn/examples/MyApplicationMaster.java#L172)에 다음과 같이 작성되어 있다.

```java
amRMClient.registerApplicationMaster("", 0, "");
```

##### 2. 리소스매니저의 애플리케이션마스터 목록을 관리하는 ApplicationMasterService는 자신이 관리하고 있는 애플리케이션마스터 목록에 해당 애플리케이션을 추가한 후 AllocateResponse 객체를 반환한다. 이 부분은 [MyApplicationMaster.java](https://github.com/googolhkl/TIL/blob/a7290b5fde0d1c809c95ae47c32647dad2afb2fa/hadoop2/yarn/application/com/hkl/hadoop/yarn/examples/MyApplicationMaster.java#L208)에 다음과 같이 작성되어 있다.

```java
AllocateResponse response = amRMClient.allocate(0);
```

##### 3. 이제 애플리케이션마스터는 allocate 메소드를 호출해 애플리케이션을 실행하는데 필요한 리소스 할당을 요청한다. 이때 파라미터는 AllocateRequest를 사용하며, 다음과 같은 정보가 저장된다.
##### - 응답ID: 응답 ID가 중복됐는지 확인하는데 사용
##### - 애플리케이션마스터가 실행 중인 애플리케이션의 진행 상태
##### - 애플리케이션이 요청하는 리소스 목록
##### - 애플리케이션 실행이 완료된 컨테이너 목록
##### - 블랙 리소스 리스트: 애플리케이션마스터가 사용해서는 안되는 리소스 목록
##### - allocate 호출 횟수
##### allocate 메소드는 애플리케이션마스터의 클라이언트가 리소스매니저에게 자신의 상태를 알려주기 위한 하트비트용도로 사용된다 리소스매니저는 AllocateRequest에 포함돼 있는 애플리케이션의 진행 상태나 하트비트 전송 주기를 체크해 애플리케이션마스터의 상태를 확인할 수 있다.
##### 또한 애플리케이션마스터는 필요한 컨테이너를 한번에 할당잗지 못하더라도 최종적으로는 필요한 모든 컨테이너를 할당받을 수 있다. 왜냐하면 allocate 메소드가 주기적으로 호출되기 때문이다. 참가로 AVRMClient와 AMRMCLientAsync는 1초에 한번씩 allocate메소드를 호출한다.

##### 4. ApplicationMasterService는 컨테이너 할당 요청을 스케줄러에게 위임한다. 스케줄러는 해당 컨테이너가 가용한지 여부를 응답한다. 그리고 ApplicationMasterService가 스케줄러의 응답 결과를 AllocateResponse에 설정한 후 애플리케이션 마스터에게 반환한다.
