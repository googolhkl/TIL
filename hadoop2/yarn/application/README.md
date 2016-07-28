# 얀 애플리케이션 개발
## 예제 애플리케이션
##### 예제는 애플리케이션 실행을 요청하는 클라이언트, 애플리케이션을 관리하는 애플리케이션마스터, 노드매니저에서 실행되는 애플리케이션으로 구성된다.


## 1. 클라이언트 구현
##### 애플리케이션 실행을 요청하는 클라이언트를 구현해보자.
##### 소스코드는 [MyClient.java](https://github.com/googolhkl/TIL/blob/master/hadoop2/yarn/application/com/hkl/hadoop/yarn/examples/MyClient.java)에서 확인할 수 있다.

## 2. 애플리케이션 마스터 구현
##### 애플리케이션을 관리하는 애플리케이션 마스터를 구현해보자.
##### 소스코드는 [MyApplicationMaster.java](https://github.com/googolhkl/TIL/blob/master/hadoop2/yarn/application/com/hkl/hadoop/yarn/examples/MyApplicationMaster.java)에서 확인할 수 있다.

## 3. 애플리케이션 구현
##### 노드매니저에서 실행되는 사용자 애플리케이션을 구현해보자.
##### 소스코드는 [HelloYarn.java](https://github.com/googolhkl/TIL/blob/master/hadoop2/yarn/application/com/hkl/hadoop/yarn/examples/HelloYarn.java)에서 확인할 수 있다.

## 4. 애플리케이션 실행
##### 지금까지 개발한 애플리케이션을 실행해 보자.
##### [빌드하기](https://github.com/googolhkl/TIL/blob/master/hadoop2/yarn/README.md)에 빌드하기를 보고 빌드를 한다.
##### 프로젝트를 생성한 경로에 target 디렉터리(/home/hkl/IdeaProjects/yarnExample/target)로 가자.

```
$ cd /home/hkl/IdeaProjects/yarnExample/target
```

##### yarn-examples-1.0-SNAPSHOT.jar 파일이 생성된 것을 확인할 수 있다.
##### 위에서 생성된 파일을 다음 명령어로 복사해 준다.

```
$ cp ./yarn-examples-1.0-SNAPSHOT.jar /home/hkl/hadoop-2.6.0/share/hadoop/yarn/lib/
```

##### 복사가 완료되면 하둡 디렉터리를 이동하고 MyClient를 실행한다. (아무런 파라미터를 설정하지 않아서 안내문만 뜬다)

```
$ cd /home/hkl/hadoop-2.6.0
$ yarn jar share/hadoop/yarn/lib/yarn-examples-1.0-SNAPSHOT.jar com.hkl.hadoop.yarn.examples.MyClient
```

##### 이제는 파라미터로 컨테이너 개수를 설정해서 실행해 보겠다. 메모리와 CPU는 설정하지 않아도 코드상에 구현된 기본값을 사용한다.

```
# yarn jar /share/hadoop/yarn/lib/yarn-examples-1.0-SNAPSHOT.jar com.hkl.hadoop.yarn.examples.Myclient -jar share/hadoop/yarn/lib/yarn-examples-1.0-SNAPSHOT.jar -num_containers=1
```

##### 정상적으로 실행됐으면 "Application completed successfully" 라는 메시지를 확인할 수 있을 것이다.
##### 그럼 애플리케이션 마스터인 MyApplicationMaster와 애플리케이션인 HelloYarn도 정상적으로 실행됐는지 확인해 보자.
##### 웹 브라우저에 http://호스트(googolhkl1):8088에 접속하면 HelloYarn이 완료된 것을 확인할 수 있을 것이다.
##### 화면에 출력된 애플리케이션 ID를 클릭해서 상세 화면으로 들어가자.
##### 이제 애플리케이션마스터의 로그가 정상적으로 출력됐는지 확인해보자.
##### 우측 하단에 logs 링크를 클릭해서 애플리케이션마스터가 출력한 로그를 확인하자.
##### AppMaster.stderr를 클릭하면 MyApplicationMaster의 로그 메시지를 확인할 수 있다.
##### 메시지중 Opening proxy : 호스트:포트 가 나오고 그 밑에 컨테이너 ID가 나온다.
##### 위에서 확인한 호스트로 로그인하고 로그 디렉터리로 가자.

```
$ ssh 호스트
$ cd /home/hkl/hadoop-2.6.0/logs/userlogs/컨테이너_ID
```

##### 디렉터리의 내용을 출력하면 하나의 디렉터리가 더있다. 들어가서 확인하면 stderr, stdout 두개의 파일이 있다.
##### stderr를 출력해 보면 HelloYarn에서 구현된 결과가 저장되어 있다.

