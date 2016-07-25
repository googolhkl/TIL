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

####주키퍼 실행 계정 생성
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

#### 주키퍼 다운로드
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

##### zoo.cfg파일을  [zoo.cfg](https://github.com/googolhkl/TIL/blob/master/hadoop2/installation/zoo.cfg) 파일로 수정해준다.
##### dataDir항목의 디렉터리를 만들고 myid파일도 생성한다. googolhkl1에는 1을 적어준다.

```
$ cd
$ mkdir data
$ cd data
$ vi myid # 이 파일에는 1을 적어준다.
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

### 6. 하둡2 설치
##### googolhkl1의 서버에 zookeeper계정이 아닌 hkl계정으로 로그인해서 다운로드를 받고 압축을 푼다.

```
$ cd ~ 
$ wget http://mirror.apache-kr.org/hadoop/common/hadoop-2.6.0/hadoop-2.6.0.tar.gz
$ tar xvfz hadoop-2.6.0.tar.gz
$ ln -s hadoop-2.6.0 hadoop2
$ cd hadoop2
```
#### 하둡2 환경 파일 수정
##### ~/hadoop2.6.0/etc/hadoop/ 디렉터리에 환경 파일이 있다.
##### hadoop-env.sh파일을 [hadoop-env.sh](https://github.com/googolhkl/TIL/blob/master/hadoop2/installation/hadoop-env.sh) 파일로 수정해준다.
##### slaves파일을  [slaves](https://github.com/googolhkl/TIL/blob/master/hadoop2/installation/slaves) 파일로 수정해준다.
##### core-site.xml파일을 [core-site.xml](https://github.com/googolhkl/TIL/blob/master/hadoop2/installation/core-site.xml) 파일로 수정해준다.
##### hdfs-site.xml파일을 [hdfs-site.xml](https://github.com/googolhkl/TIL/blob/master/hadoop2/installation/hdfs-site.xml) 파일로 수정해준다.
##### mapred-site.xml파일을 [mapred-site.xml](https://github.com/googolhkl/TIL/blob/master/hadoop2/installation/mapred-site.xml.template) 파일로 수정해준다.
##### yarn-env.sh파일을 [yarn-env.sh](https://github.com/googolhkl/TIL/blob/master/hadoop2/installation/yarn-env.sh) 파일로 수정해준다.
##### yarn-site.xml파일을 [yarn-site.xml](https://github.com/googolhkl/TIL/blob/master/hadoop2/installation/yarn-site.xml) 파일로 수정해준다.

#### 하둡2 배포
##### googolhkl1에 설정된 하둡2 설치 파일을 전체 서버에 배포한다.

```
$ cd ~
$ tar zcvf hadoop.tar.gz hadoop-2.6.0/
$ scp hadoop.tar.gz hkl@googolhkl2:/home/hkl
$ scp hadoop.tar.gz hkl@googolhkl3:/home/hkl
$ scp hadoop.tar.gz hkl@googolhkl4:/home/hkl
$ ssh hkl@googolhkl2 "cd ~; tar xvfz hadoop.tar.gz"
$ ssh hkl@googolhkl3 "cd ~; tar xvfz hadoop.tar.gz"
$ ssh hkl@googolhkl4 "cd ~; tar xvfz hadoop.tar.gz"
```

#### 하둡2 실행
##### ZKFC를 실행하기 전에 반드시 아래와 같이 주키퍼를 초기화해야 한다. (주키퍼 서버를 실행한 상태여야 한다)

```
hkl@googolhkl1 hadoop-2.6.0$ ./bin/hdfs zkfc -formatZK
```

##### HDFS에 파일을 저장하려면 네임노드를 미리 포맷해야 한다. 네임노드 HA에서는 네임노드를 포맷하기 전에 반드시 저널 노드를 실행해야 한다.

```
hkl@googolhkl1 hadoop-2.6.0$ ./sbin/hadoop-daemon.sh start journalnode
hkl@googolhkl2 hadoop-2.6.0$ ./sbin/hadoop-daemon.sh start journalnode
hkl@googolhkl3 hadoop-2.6.0$ ./sbin/hadoop-daemon.sh start journalnode
```

##### 저널 노드가 실행됐으니 네임노드를 포맷해 준다.

```
ogolhkl1 hadoop-2.6.0$ ./bin/hdfs namenode -format
```

##### 네임노드가 포맷되고 나면 다음과 같이 액티브 네임노드를 실행한다.

```
hkl@googolhkl1 hadoop-2.6.0$ ./sbin/hadoop-daemon.sh start namenode
```

##### 이어서 주키퍼 장애 컨트롤러를 실행한다. 

```
hkl@googolhkl1 hadoop-2.6.0$ ./sbin/hadoop-daemon.sh start zkfc
```

##### 전체 데이터노드를 실행한다.

```
hkl@googolhkl1 hadoop-2.6.0$ ./sbin/hadoop-daemons.sh start datanode
```

##### 이제 스탠드바이 네임노드를 설정한다. 우선 스탠드바이 네임노드를 실행할 서버(googolhkl2)에 접속한 후 하둡 디렉터리로 이동해 다음과 같이 액티브 네임노드의 메타데이터를 복사한다.
##### bootstrapStandby 명령어를 실행하면 스탠드바이 네임노드를 포맷하고 액티브 네임노드의 메타데이터가 스탠드바이 네임노드로 복사된다.

```
hkl@googolhkl2 hadoop-2.6.0$ ./bin/hdfs namenode -bootstrapStandby
```

##### 메타데이터 복사가 완료되면 다음과 같이 스탠드바이 네임노드를 실행한다

```
hkl@googolhkl2 hadoop-2.6.0$ ./sbin/hadoop-daemon.sh start namenode
```

##### 다음으로 스탠드바이 네임노드용 주키퍼 장애 컨트롤러를 실행한다,

```
hkl@googolhkl2 hadoop-2.6.0$ ./sbin/hadoop-daemon.sh start zkfc
```

##### 이제 모든 HDFS 설치가 완료 됐으니 테스트 해보자.
##### 실행은 어느 서버에서 실행하든 상관 없다.

```
hkl@googolhkl1 hadoop-2.6.0$ ./bin/hdfs dfs -ls /
hkl@googolhkl1 hadoop-2.6.0$ ./bin/hdfs dfs -mkdir /user
hkl@googolhkl1 hadoop-2.6.0$ ./bin/hdfs dfs -mkdir/user/hkl
hkl@googolhkl1 hadoop-2.6.0$ ./bin/hdfs dfs -mkdir/user/hkl/conf
```

##### 디렉터리를 생성했고, 이제 파일을 업로드 하자. 업로드할 파일은 하둡 환경설정 파일이다.

```
hkl@googolhkl1 hadoop-2.6.0$ ./bin/hdfs dfs -put etc/hadoop/hadoop-env.sh /user/hkl/conf

```

##### 성공적으로 업로드 됐는지 확인하자.

```
hkl@googolhkl1 hadoop-2.6.0$ ./bin/hdfs dfs -ls conf
```

##### 이제 얀 클러스터를 실행한다.

```
hkl@googolhkl1 hadoop-2.6.0$ ./sbin/start-yarn.sh
```

##### 맵리듀스 잡을 위한 히스토리 서버도 함께 실행한다.
```
hkl@googolhkl1 hadoop-2.6.0$ ./sbin/mr-jobhistory-daemon.sh start historyserver
```

##### 이제 웹에 액티브 네임노드인 http://googolhkl1:50070 에 접속하면 웹 인터페이스가 나온다.
##### wordcount 예제를 실행해 보자

```
hkl@googolhkl1 hadoop-2.6.0$ ./bin/yarn jar share/hadoop/mapreduce/hadoop-mapreduce-examples-2.6.0.jar wordcount conf output
```

##### 이번에는 네임노드 HA가 제대로 동작하는지 확인해 보자
##### jps명령으로 NameNode의 pid를 확인 한 후  

```
$ jps
```

##### kill명령어로 제거해준다.

```
$ kill -9 네임노드_PID
```

##### 그럼 http://googolhkl1:50070 에 접속이 안되고 http://googolhkl2:50070 으로 접속이 된다.
##### 다시 네임노드르 실행해주자.

```
 $ ./bin/hdfs haadmin -getServiceState nn2
```

### 두번째 실행부터 계속 반복하기 귀찮으니 스크립트로 만들어 버리자.
##### [hadoopStart.sh](https://github.com/googolhkl/TIL/blob/master/hadoop2/installation/hadoopStart.sh) 파일을 실행하면 된다.
