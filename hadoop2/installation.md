# 하둡 설치하기
### 설치 환경은 우분투 16.04 LTS로 가정한다.
### 서버는 googolhkl1, googolhkl2, googolhkl3, googolhkl4로 가정한다.
### 서버의 모든 사용자는 hkl로 가정한다.

### 1. 호스트 파일 수정

##### 하둡은 서버 간에 ssh를 이용하는데 ssh는 다른 서버로 접근할 때 IP혹은 호스트명으로 접속한다.
##### 호스트 파일을 수정해준다.

`$ ifconfig`

##### 여기서 나온 아이피를 아래에 적어준다.
 
```
$ sudo vi /etc/hosts
127.0.0.1 		localhost
111.111.22.101	googolhkl1
111.111.22.102	googolhkl2
111.111.22.103	googolhkl3
111.111.22.104	googolhkl4
```

##### 127.0.1.1은 삭제한다. 
##### 이렇게 모든 서버에 반복해준다.

### 2. JAVA 설치

```
$ sudo add-apt-repository ppa:webupd8team/java
$ sudo apt-get update
$ sudo apt-get install oracle-java8-installer
$ java -version
```

### 3. SSH 설정

##### 가장 먼저 ssh를 설치해주고 실행시킨다. (4개의 서버에서 모두 다 실행한다)

```
$ sudo apt-get install ssh
$ service ssh start
```

##### 다음으로 googolhkl1으로 와서 공개키 생성과 다른 서버에 복사를 실행한다.

```
$ ssh-keygen -t rsa
$ ssh-copy-id -i /home/hkl/.ssh/id_rsa.pub hkl@googolhkl2
```

##### 성공적으로 완료하면 다음 명령어로 비밀번호 없이 다른 서버에 접속이 가능하다.

```
$ ssh googolhkl2
```

##### 이 작업을 나머지 세개의 서버에서도 해준다.

### 4. 프로토콜 버퍼 설치

##### 하둡2에서 프로토콜 버퍼는 2.5.0을 사용하기 때문에 2.5.0을 설치 해줘야 한다.(다른 버전은 호환이 안됀다)
##### 프로토콜 버퍼를 설치하기 위해 여러가지 툴이 필요하다. (autoconf, automake, libtool, curl, make, g++, unzip)

```
$ sudo apt-get install autoconf automake libtool curl make g++ unzip
```

##### root 비밀번호가 설정 되어 있지 않다면 다음 명령어로 설정 해준다.

```
$ sudo passwd root
```

##### 프로토콜 버퍼를 설치한다.

```
$ cd /usr/local 
$ wget http://protobuf.googlecode.com/files/protobuf-2.5.0.tar.gz
$ tar xvfz protobuf-2.5.0
$ cd protobuf-2.5.0
$ ./autogen.sh
$ ./configure
$ make
$ make check
$ make install
$ ldconfig
$ protoc --version
```

### 5. 주키퍼 설치

##### 네임노드 HA를 구성하려면 먼저 주키퍼를 설치해야 한다.

##주키퍼 실행 계정 생성
##### root 계정으로 로그인한 후, zookeeper계정을 생성한다.

```
$ adduser zookeeper
$ passwd zookeeper
```

##### 이렇게 googolhkl1, googolhkl2, googolhkl3 서버에 zookeeper계정을 만든다.
##### googolhkl1, googolhkl2에서 다음과 같은 작업을 해준다.

```
$ ssh-copy-id -i /home/hkl/.ssh/id_rsa.pub zookeeper@googolhkl1
$ ssh-copy-id -i /home/hkl/.ssh/id_rsa.pub zookeeper@googolhkl2
$ ssh-copy-id -i /home/hkl/.ssh/id_rsa.pub zookeeper@googolhkl3
```

## 주키퍼 다운로드
##### googolhkl1의 zookeeper계정으로 로그인 해준다.

```
$ ssh zookeeper@googolhkl1
```

##### 주키퍼를 다운로드 받고 설정을 해준다.

```
$ wget http://mirror.apache-kr.org/zookeeper/zookeeper-3.4.6/zookeeper-3.4.6.tar.gz
$ tar xvfz zookeeper-3.4.6.tar.gz
$ cd /home/zookeeper/zookeeper-3.4.6
$ cp conf/zoo_sample.cfg conf/zoo.cfg
$ vi conf/zoo.cfg
```

##### [zoo.cfg](www.naver.com) 파일을 수정해준다.
##### dataDir항목의 디렉터리를 만들고 myid파일도 생성한다. googolhkl1에는 1을 적어준다.

```
$ cd
$ mkdir data
$ cd data
$ vi myid # 이 파일에는 1을 적어줍니다.
```
##### 이렇게 googolhkl1에 zookeeper계정 설정을 했는데, googolhkl2, googolhkl3에도 해야한다.
##### scp로 복사를 진행하겠다. googolhkl1의 zookeeper계정의 공개키를 googolhkl2, googolhkl3의 zookeeper계정에 복사를 해주고 진행한다.

```
$tar zcvf zookeeper.tar.gz zookeeper-3.4.6/
$scp zookeeper.tar.gz zookeeper@googolhkl2:/home/zookeeper
$scp zookeeper.tar.gz zookeeper@googolhkl3:/home/zookeeper
$ssh googolhkl2 "cd /home/zookeeper; tar xvfz zookeeper.tar.gz; mkdir data"
$ssh googolhkl3 "cd /home/zookeeper; tar xvfz zookeeper.tar.gz; mkdir data"
```

##### googolhkl2, googolhkl3의 계정으로 접속해 myid를 수정해준다.
##### googolhkl2 = 2
##### googolhkl3 = 3
##### 주키퍼 서버를 실행한다. 각 서버마다 zookeeper계정으로 로그인해서 실행해준다.

```
$ cd zookeeper-3.4.6
$ ./bin/zkServer.sh start
$ ./bin/zkServer.sh status
```

###### 오류가 발생할 때 환경변수를 추가해야한다.
###### 각서버마다 /etc/profile에 다음 내용을 추가한다.
###### export JAVA_HOME=/usr/lib/vim/java-8-oracle
###### export ZK_HOME=/home/zookeeper/zookeeper/
###### export PATH=$PATH:$ZK_HOME/bin
