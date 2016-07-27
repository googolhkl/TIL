# 메이븐
## 메이븐 설치
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
# IDE연동
## 1. 인텔리제이와 연동
##### 인텔리제이를 실행시키고 File - New - Project... - Maven - Project SDK선택 - Next
##### GroupId와 ArtifactId, Version은 다음과 같이 입력해 준다.

> ##### GroupId : com.hkl.hadoop
> ##### ArtifactId : yarn-examples
> ##### 1.0-SNAPSHOT

##### Next를 누르고 Project name과 Project location을 지정해 준다.

> ##### Project name: yarn-examples
> ##### Project location: ~/IdeaProjects/yarn-examples

##### Finish를 누르면 프로젝트 생성이 끝난다.
##### 프로젝트 뷰를 보이게 해보자.
##### View - Tool Windows - Project 를 눌러준다.
##### View - Tool Windows - Maven Projects 를 눌러준다.
##### 이제 pom.xml 파일에 하둡관련 설정을 추가하겠다.
##### /home/hkl/IdeaProjects/yarn-examples/pom.xml 파일을 [pom.xml](https://github.com/googolhkl/TIL/blob/master/hadoop2/yarn/pom.xml) 파일로 바꿔준다.
##### 소스파일은 /home/hkl/IdeaProjects/yarn-examples/src/main/java 에 넣어둔다.
##### 라이브러리도 추가해 보자.
##### File - Project Structure...
##### 왼쪽에 Modules 탭을 클릭한다.
##### Name 밑쪽에 Dependencies 탭에 들어가 오른쪽에 초록색 [+] 버튼을 클릭한다.
##### JARs or directories... 를 클릭한다.
##### 경로에 /home/hkl/hadoop-2.6.0/share/hadoop 을 적고 [OK] 버튼을 누른다


## 2. 이클립스와 연동
##### 이클립스를 실행시키고 File - New - Other... - Maven - Maven Project - Next > 를 눌러준다.
##### 다음과 같이 체크박스에 체크를 해준다.

- [x] Create a simple project (skip archetype selection)
- [x] Use default Workspace location
- [ ] Add project(s) to working set

##### Location : /home/hkl/source/yarn-examples
##### Next > 를 누르고 Group Id와 Artifact Id, Version을 다음과 같이 적어준다.

> ###### GroupId : com.hkl.hadoop
> ###### ArtifactId : yarn-examples
> ###### 1.0-SNAPSHOT

##### Finish 버튼을 누르면 메이븐 프로젝트 생성을 위한 설정이 완료된다.
##### 이제 pom.xml 파일에 하둡관련 설정을 추가하겠다.
##### /home/hkl/source/yarn-examples/pom.xml 파일을 [pom.xml](https://github.com/googolhkl/TIL/blob/master/hadoop2/yarn/pom.xml) 파일로 바꿔준다.
##### 소스파일은 /home/hkl/source/yarn-examples/src/main/java/ 에 넣어둔다.

# 빌드하기
## 1. 인텔리제이로 빌드하기
##### View - Tool Windows - Maven Projects를 클릭한다.
##### 오른쪽에 새로운 뷰가 보인다. 이 뷰의 상단에 [Execute Maven Goal]을 클릭한다. (m모양의 아이콘이다)
##### Command line에 다음과 같이 입력한다.

> clean install

##### [Execute] 버튼을 누르면 빌드가 시작된다.



## 2. 이클립스로 빌드하기
##### Run - Run Configuration... - Maven Build 으로 들어간다.
##### Base Directory는 다음처럼 바꿔준다.

> /home/hkl/source/yarn-examples

##### Goals 에 다음과 같이 적어준다.
> Goals : clean install 

##### Run 버튼을 누르면 빌드가 된다.


## 3. 커맨드 라인으로 빌드하기
##### /home/hkl/source/yarn-examples 에서 다음과 같은 명령어를 실행한다.

```
$ mvn clean install
```

## 빌드완료 확인하기
### 1. 인텐리제이로 빌드하고 확인하기
##### /home/hkl/IdeaProjects/yarnExample/target 디렉터리가 생성된 것을 확인할 수 있다.
##### target 디렉터리 안에 yarn-examples-1.0.SNAPSHOT.jar 파일이 생성된 것을 확인할 수 있다.

### 2. 이클립스로 빌드하고 확인하기
##### /home/hkl/source/yarn-examples/target 디렉터리가 생성된 것을 확인할 수 있다.
##### target 디렉터리 안에 yarn-examples-1.0.SNAPSHOT.jar 파일이 생성된 것을 확인할 수 있다.

