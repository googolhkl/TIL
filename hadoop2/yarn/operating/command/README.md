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

## 1. 사용자 명령어
### jar
##### 이 명령어는 jar 파일에 포함된 애플리케이션을 실행할 때 사용한다. 이 명령은 다음과 같은 형태로 사용한다.
##### `bin/yarn jar jar_파일_이름 [메인클래스] 추가 옵션

 ```
[hkl@googolhkl1 hadoop-2.6.0] ./bin/yarn jar share/hadoop/yarn/lib/yarn-examples-1.0-SNAPSHOT.jar com.hkl.hadoop.yarn.examples.Myclient -jar share/hadoop/yarn/lib/yarn-examples-1.0-SNAPSHOT.jar -num_containers=1
 ```

### application
##### 이 명령어는 애플리케이션 상태를 출력하거나 특정한 애플리케이션을 종료할 때 사용한다. 

| 옵션 | 내용 | 예시 |
| --- | --- | --- |
| -list | 얀 클러스터에서 실행 중인 애플리케이션 목록을 출력한다. | bin/yarn application -list |
| -appStates <상태> | -list 옵션과 함께 사용 가능한 옵션이다. 애플리케이션 목록을 출력할 때 특정 상태인 애플리케이션 목록만을 출력하게 한다. 사용가능한 상태는 다음과 같다.
- ALL, NEW, NEW_SAVING, SUBMITTED, ACCEPTED, RUNNING, FINISHED, FAILED, KILLED 
참고로 콤마 구분자로 해서 상태를 여러 개 지정할 수 있다. | bin/yarn application -list -appStates ACCEPTED,FAILED
