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
### 1. 애플리케이션 실행 요청
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


### 2. 애플리케이션마스터 실행 요청
##### 이 부분은 얀의 전체적인 작업흐름에서 2.애플리케이션마스터 실행요청에 해당한다.
![애플리케이션마스터 실행요청](https://github.com/googolhkl/TIL/blob/master/hadoop2/yarn/architecture/ApplicationMasterExecutingRequest.png)
