# 메이븐
### 메이븐 설치
##### 빌드관리 도구로 메이븐을 이용하겠다.
##### [메이븐 다운로드 홈페이지](http://maven.apache.org/download.cgi) 에서 메이븐 3.2.3을 설치하거나 다음 명령어로 설치하면 된다.

```
$ wget https://archive.apache.org/dist/maven/maven-3/3.2.3/binaries/apache-maven-3.2.3-bin.tar.gz
```

##### 받은 파일을 /usr/local 디렉터리로 이동시켜 주고 압축을 풀어주자.

```
$ sudo mv apache-maven-3.2.3-bin.tar.gz /usr/local
$ cd /usr/local
$ sudo tar xvfz apache-maven-3.2.3-bin.tar.gz
```

##### 압축이 풀린 디렉터리를 리눅스 프로파일(/etc/profile)에 추가한다.

> export PATH=$PATH:/usr/local/apache-maven-3.2.3/bin

##### 로그아웃을 하고 다시 로그인을 하고 다음 명령어로 제대로 설치 됐는지 확인해 보자.

```
$ mvn
```

### 이클립스와 연동
##### 이클립스를 실행시키고 File - New - Other... - Maven - Maven Project - Next > 를 눌러준다.
##### 다음과 같이 체크박스에 체크를 해준다.

- [x] Create a simple project (skip archetype selection)
- [x] Use default Workspace location
- [] Add project(s) to working set

##### Location : /home/hkl/source/yarn-examples
##### Next > 를 누르고 Group Id와 Artifact Id, Version을 다음과 같이 적어준다.

> Group Id : com.hkl.hadoop
> Artifact Id : yarn-examples
> 1.0-SNAPSHOT

##### Finish 버튼을 누르면 메이븐 프로젝트 생성을 위한 설정이 완료된다.
##### 이제 pom.xml 파일에 하둡관련 설정을 추가하겠다.
##### /home/hkl/source/yarn-examples/pom.xml 파일을 [pom.xml](https://github.com/googolhkl/TIL/blob/master/hadoop2/yarn/pom.xml) 파일로 바꿔준다.
##### 소스파일을 /home/hkl/source/yarn-examples/src/main/java/ 에 넣어둔다.
##### /home/hkl/source/yarn-examples 에서 다음과 같은 명령어를 실행한다.

```
$ mvn clean install
```

##### target 디렉터리가 생성된 것을 확인할 수 있다.
##### target 디렉터리 안에 yarn-examples-1.0.SNAPSHOT.jar 파일이 생성된 것을 확인할 수 있다.
