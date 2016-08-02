# 얀 명령어
##### 얀은 커맨드 라인에서 얀 클러스터를 제어할 수 있게 다양한 명령어를 제공한다.
##### 얀 명령어는 하둡 디텍토리의 bin 디렉터리에 yarn이라는 실행파일로 제공된다.
##### 이 명령어를 실행하면 다음과 같은 사용법이 출력된다.

```
[hkl@googolhkl1 hadoop-2.6.0] ./bin/yarn
 Usage : yarn [--config confdir] COMMAND
 where COMMAND is one of:
 resourcemanager -format-state-store 	deletes the RMStateStore
 resourcemanager			run the ResourceManager
 nodemanager				run a nodemanager on each slave
 timelineserver				run the timeline server
 rmadmin				admin tools
 version				print the version
 jar <jar>				run a jar file
 application				prints application(s)
					report/kill application
 applicationattempt			prints applicationattempt(s)
 					report
 container				prints container(s) report
 node					prints node report(s)
 queue					prints queue information
 logs					dump container logs
 classpath				prints the class path needed to
 					get the Hadoop jar and the
					requred libraries
 daemonlog				get/set the log level for each
 					daemon
or
 CLASSNAME				run the class named CLASSNAME
Most commands print help when invoked w/o parameters
```

##### 출력된 COMMAND는 얀 명령어 뒤에 사용할 수 있는 옵션이다. 예를들어 리소스매니저를 실행할 경우 다음과 같이 입력하면 된다.

```
[hkl@googolhkl1 hadoop-2.6.0] ./bin/yarn resourcemanager
```

##### 얀 명령어는 크게 두가지로 분류 된다.

 - 사용자 명령어
 - 관리자 명령어

##### 사용자 명령어의 경우 계정에 상관 없이 실행할 수 있다.
##### 관리자 명령어의 경우 하둡을 실행한 계정만 실행할 수 있다.
<br />

## 1. 사용자 명령어
### jar
##### 이 명령어는 jar 파일에 포함된 애플리케이션을 실행할 때 사용한다. 이 명령은 다음과 같은 형태로 사용한다.
##### `bin/yarn jar jar_파일_이름 [메인클래스] 추가 옵션

```
[hkl@googolhkl1 hadoop-2.6.0] ./bin/yarn jar share/hadoop/yarn/lib/yarn-examples-1.0-SNAPSHOT.jar com.hkl.hadoop.yarn.examples.Myclient -jar share/hadoop/yarn/lib/yarn-examples-1.0-SNAPSHOT.jar -num_containers=1
```
<br />

### application
##### 이 명령어는 애플리케이션 상태를 출력하거나 특정한 애플리케이션을 종료할 때 사용한다. 

| 옵션 | 내용 | 예시 |
| --- | --- | --- |
| -list | 얀 클러스터에서 실행 중인 애플리케이션 목록을 출력한다. | bin/yarn application -list |
| -appStates <상태> | -list 옵션과 함께 사용 가능한 옵션이다. 애플리케이션 목록을 출력할 때 특정 상태인 애플리케이션 목록만을 출력하게 한다. 사용가능한 상태는 다음과 같다.<br /> ALL, NEW, NEW_SAVING, SUBMITTED, ACCEPTED, RUNNING< FINISHED, FAILED, KILLED<br /> 참고로 콤마 구분자로 상태를 여러개 지정할 수 있다. | bin/yarn application -list -appStates ACCEPTED,FAILED
| -appTypes <타입> | -list 옵션과 함께 사용 가능한 옵션. 애플리케이션 목록을 출력할 때 특정 애플리케이션 타입에 해당하는 애플리케이션을 출력한다. 타입에는 출력하고자 하는 애플리케이션의 타입을 설정하면 된다. 참고로 콤마 구분자로 타입을 여러 개 지정할 수 있다. | bin/yarn application -list -appTypes MAPREDUCE<br /> bin/yarn application -list -appTypes YARN
| -kill <애플리케이션ID> | 특정 애플리케이션을 강제로 종료한다. 애플리케이션ID는 얀 웹화면이나 application -list에서 확인한다. | bin/yarn application -kill application_1409667559273_0004 |
| -movetoqueue <애플리케이션ID> | 리소스매니저의 스케줄러는 여러 개의 큐로 구성할 수 있다. 이 옵션은 특정 애플리케이션을 스케줄러의 다른 큐로 이동시킬 수 있다. | bin/yarn application -movetoqueue application_1409667559273_0004  -queue anotherqueue |
| -queue <큐 이름> | movetoqueue 옵션과 함께 사용한다. 이동시킬 큐의 이름을 설정한다. | 위와 동일 |
| -status <애플리케이션ID> | 특정 애플리케이션의 상태를 출력한다. | bin/yarn application -status application_1409667559273_0004  
<br />

### node
##### 노드매니저 상태를 출력하는 명령어다.

| 옵션 | 내용 | 예시 |
| --- | --- | --- |
| -list | 현재 실행 중인 노드 매니저 목록을 출력한다. | bin/yarn node -list |
| -all | -list 옵션과 함께 사용한다. 실행 중인 노드매니저 외에 강제로 제거되거나 장애가 발생한 노드매니저도 함께 출력한다. | bin/yarn node -list -all |
| -states <상태> | -list 옵션과 함께 사용한다. 특정 상태의 노드매니저만을 출력하게 한다.설정 가능한 상태는 다음과 같다.<br /> NEW, RUNNING, UNHEALTHY, ECOMMISSIONED, LOST, REBOOTED<br /> 참고로 콤마 구분자로 상태를 여러 개 지정할 수 있다. | bin/yarn node -list -states RUNNING,UNHEALTHY |
| -status <노드매니저ID> | 특정 노드매니저의 상태를 출력한다. 노드매니저ID는 -list에서 출력된 node-Id를 사용한다. | bin/yarn node -status 127.0.0.1:63432 |
<br />

### logs
##### 애플리케이션의 로그 덤프를 생성한다. 이 명령어는 다음과 같이 애플리케이션ID를 지정해 실행한다. 단, 이 명령어를 수행하려면 yarn-site.xml에 있는 yarn.log-aggregation-enable 옵션이 true로 설정돼 있어야 한다. yarn.log-aggregation-enable 옵션은 노드매니저의 로그의 병합여부를 나타내며, 기본값은 false다.

##### [hkl@googolhkl1 hadoop-2.6.0] ./bin/yarn logs -applicationId 애플리케이션Id 추가옵션

```
[hkl@googolhkl1 hadoop-2.6.0] ./bin/yarn logs -applicationId application_1409667559273_0008  
```

##### logs 명령어는 다음 표와 같은 옵션을 설정할 수 있다. -applicationId 옵션과 함께 사용해야 한다.

| 옵션 | 내용 | 예시 |
| --- | --- | --- |
| -appOwner <ApplicationOwner> | 애플리케이션의 소유자를 설정한다. | bin/yarn logs -applicationId application_1409667559273_0007 -appOwner hkl |
| -containerId <Container ID> | 특정 컨테이너의 로그 덤프를 생성한다. | bin/yarn logs -applicationId application_1409667559273_0007 -containerId container_1409667559273_0007_01_000009 |
| -nodeAddress <NodeAddress> | 특정 노드매니저의 로그 덤프를 생성한다. | bin/yarn logs -applicationId application_1409667559273_0007 -nodeAddress 127.0.0.1:63432 |
<br />

### classpath
##### 얀 클러스터의 클래스패스를 출력한다.

```
[hkl@googolhkl1 hadoop-2.6.0] ./bin/yarn classpath
```
<br />

### version
##### 현재 하둡 버전을 출력한다.

```
[hkl@googolhkl1 hadoop-2.6.0] ./bin/yarn version
```
<br />

## 2. 관리자 명령어
##### 얀은 다음과 같은 관리자용 명령어를 제공한다.
<br />

### resourcemanager
##### 리소스매니저를 구동한다.

```
[hkl@googolhkl1 hadoop-2.6.0] ./bin/yarn resourcemanager
```
<br />

### nodemanager
##### 노드매니저를 구동한다.

```
[hkl@googolhkl1 hadoop-2.6.0] ./bin/yarn nodemanager
```
<br />

### proxyserver
##### 프록시 서버를 구동한다.

```
[hkl@googolhkl1 hadoop-2.6.0] ./bin/yarn proxyserver
```
<br />

### timelineserver
##### 하둡 2.6.0 버전부터 사용자에게 애플리케이션 실행 이력을 제공하기 위해 타임라인 서비스를 제공한다. 이 명령어는 타임라인 서비스를 위한 타임라인 서버를 실행하는 기능이다. 하지만 현재 버전에서는 타임라인 서버가 정상적으로 동작하지 않기 때문에(-_-) 향후 버전에서 운영하는 것이 좋다.

```
[hkl@googolhkl1 hadoop-2.6.0] ./bin/yarn timelineserver
```
<br />

### rmadmin
##### 리소스매니저를 관리하기 위한 다양한 옵션을 제공한다. 다음은 이 명령어가 지원하는 옵션이다.

| 옵션 | 내용 | 예시 |
| --- | --- | --- |
| -refreshQueues | mapred-queues.xml을 리프레시한다. 이 파일에는 맵리듀스용 큐의 정보가 설정돼 있다. | bin/yarn rmadmin -refreshQueues |
| -refreshNodes | 리소스매니저에서 노드매니저의 정보를 리프레시한다. | bin/yarn rmadmin -refreshNodes |
| -refreshSuperUserGroupsConfiguration | 슈퍼유저(관리자) 프록시그룹 매핑 정보를 리프레시한다. | bin/yarn rmadmin -refreshSuperUserGroupsConfiguration | 
| -refreshUserToGroupsMappings | 유저와 그룹 간의 매핑 정보를 리프레시한다. | bin/yarn rmadmin -refreshUserToGroupsMappings |
| -refreshAdminAcls | 리소스매니저의 관리자용 ACL을 리프레시한다. | bin/yarn rmadmin -refreshAdminAcls |
| -refreshServiceAcl | 서비스 레벨의 인증 정책 파일을 리프레시한다. | bin/yarn rmadmin -refreshServiceAcl |
| -getGroups [계정] | 특정 계정의 그룹 정보를 출력한다. | bin/yarn rmadmin -getGroups hkl |
<br />

### daemonlog
##### 노드매니저의 로그 출력 레벨을 관리한다. 다으은 이 명령어에서 사용 가능한 옵션이다.

| 옵션 | 내용 | 예시 |
| --- | --- | --- |
| -getlevel | 특정 노드매니저에서 특정 클래스의 로그 레벨을 출력한다. 이때 **노드매니저의 포트는 반드시 웹 포트를 설정해야 한다.**참고로 bin/yarn node -list에서 출력된 Node-Http-Address를 사용하면 된다. | bin/yarn daemonlog -getlevel [노드매니저호스트:노드매니저웹포트] [클래스명]<br />bin/yarn daemonlog -getlevel 127.0.0.1:8042 NodeManager<br />bin/yarn daemonlog -getlevel 127.0.0.1:8042 AMRMClient |
| -setlevel | 특정 노드매니저에서 특정 클래스의 로그 레벨을 강제로 설정한다. | bin/yarn daemonlog -getlevel [노드매니저호스트:노드매니저웹포트] [클래스명] [로그레벨]<br />bin/yarn daemonlog -getlevel 127.0.0.1:8042 NodeManager INFO<br />bin/yarn daemonlog -getlevel 127.0.0.1:8042 AMRMClient DEBUG |


