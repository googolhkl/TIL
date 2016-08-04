# 하이브 설치
##### 하이브는 아키텍처만큼이나 설치 방법도 매우 간단하다. [하이브 공식홈페이지](http://hive.apache.org/downloads.html)에서 원하는 패키지 파일을 내려받으면 된다. 여기서는 1.2.1 버전을 설치하겠다.
##### 하이브 설치 파일은 하둡 클라이언트가 구성돼 있는 PC에 설치하면 된다. 우리는 네임노드가 설치돼 있는 마스터 서버에 설치하겠다.
##### 아래 명령어로 다운로드를 하자.

```
$ wget mirror.navercorp.com/apache/hive/stable/apache-hive-1.2.1-bin.tar.gz
$ tar xvfz apache-hive-1.2.1-bin.tar.gz
```

##### 압축을 풀면 `apache-hive-1.2.1-bin` 디렉토리가 생성되고 조회하면 다음과 같이 출력된다.
##### `examples` 디렉토리는 하이브QL을 이용하는 다양한 예제 파일이 들어있다.

```
hkl@googolhkl1:~$ ls -l apache-hive-1.2.1-bin/
합계 476
-rw-rw-r-- 1 hkl hkl  24754  4월 30  2015 LICENSE
-rw-rw-r-- 1 hkl hkl    397  6월 19  2015 NOTICE
-rw-rw-r-- 1 hkl hkl   4366  6월 19  2015 README.txt
-rw-rw-r-- 1 hkl hkl 421129  6월 19  2015 RELEASE_NOTES.txt
drwxrwxr-x 3 hkl hkl   4096  8월  4 12:30 bin
drwxrwxr-x 2 hkl hkl   4096  8월  4 12:30 conf
drwxrwxr-x 4 hkl hkl   4096  8월  4 12:30 examples
drwxrwxr-x 7 hkl hkl   4096  8월  4 12:30 hcatalog
drwxrwxr-x 4 hkl hkl   4096  8월  4 12:30 lib
drwxrwxr-x 3 hkl hkl   4096  8월  4 12:30 scripts
```

##### 하이브는 `conf` 디렉토리에 있는 `hive-env.sh` 파일에 기본 환경설정을 한다. 처음 하이브를 설치했을 경우, `conf` 디렉토리에는 템플릿 파일만 있으므로 다음과 같이 새로운 `hive-env.sh`파일을 만든다.

```
hkl@googolhkl1:~/apache-hive-1.2.1-bin$ mv conf/hive-env.sh.template conf/hive-env.sh
```
<br />

##### 하이브는 하둡 환경설정 정보를 이용해 하이브 질의를 수행한다. `hive-env.sh`파일에 다음과 같이 하둡 홈 디렉토리를 설정한다.

```
# Set HADOOP_HOME to point to a specific hadoop install directory
# HADOOP_HOME=${bin}/../../hadoop
HADOOP_HOME=/home/hkl/hadoop-2.6.0
```

#####하이브 서비스의 설정을 세부적으로 수정하고 싶을 때는 `conf` 디렉토리에 `hive-site.xml` 파일을 정의해야 한다. 하이브는 기본적으로 `conf` 디렉토리에 있는 `hive-default.xml`파일을 이용해 환경설정을 진행하는데, `hive-site.xml`에 `hive-default.xml`과 같은 속성이 있다면 `hive-default.xml`에 있는 속성을 무시하고 `hive-site.xml`에 정의된 속성을 사용한다. `hive-default.xml`에는 매우 많은 속성 값이 있으며, 그 중에서 중요한 속성을 아래 표에 정리했다. 참고로 다른 속성값에 대한 설명은 `conf`디렉토리에 있는 `hive-default.xml.template`파일을 확인하면 된다.

| 속성 | 내용 |
| --- | --- |
| hive.metastore.warehouse.dir | 데이터 웨어하우스를 저장하는 기본 디렉토리다. 기본값은 `/user/hive/warehouse`이다. |
| hive.exec.scratchdir | 하이브 잡이 수행될 때 생성되는 데이터를 저장할 HDFS 디렉토리다. 기본 경로는 `"tmp/hive-계정명"`이다. |
| hive.metastore.local | 원격 서버에 설치된 메타스토어 데이터베이스에 접속할 것인지, 로컬에 아파치 더비를 이용할 것인지 설정한다. 기본값으로 true로 설정돼 있어서 로컬의 아파치 더비를 사용한다. 여러 사용자가 함께 사용한다면 false로 설정한 후 JDBC 설정 속성을 함께 등록해야 한다. | 
| javax.jdo.option.ConnectionDriverName | 메타스토어 데이터베이스에 접근할 때 사용할 JDBC 드라이버다. 기본 드라이버는 `org.apache.derby.jdbc.EmbeddedDriver`다. |
| javax.jdo.option.ConnectionURL | 메타스토어 데이터베이스에 접속하기 위한 커넥션 스트링 값이다. 기본값은 jdbc:derby:;databaseName=metastore_db;create=true 다. |
| javax.jdo.option.ConnectionUserName | 메타스토어용 데이터베이스에 로그인하는 사용자명이다. 기본값은 APP이다. |
| javax.jdo.option.ConnectionPassword | JDBC 메타스토어용 데이터베이스에 로그인하는 비밀번호이며, 기본값은 mine으로 설정돼 있다. | 
<br />

##### 하이브는 기본적으로 아파치 더비디비를 메타스토어로 사용하며, 다른 DBMS를 메타스토어로 사용하려면 `hive-site.xml`을 수정해야 한다.
##### 아래 예제는 MySQL을 메타스토어로 사용하는 `hive-site.xml`을 나타낸다. 이처럼 DBMS를 메타스토어로 사용할 경우 반드시 해당 DBMS의 JDBC드라이버를 하이브의 `lib` 디렉토리에 복사해야 한다. 하이브는 JDBC를 이용해 각 DMBS에 접속하기 때문에 해당 드라이버를 찾지 못할 경우 접속 자체가 불가능하다.

```xml
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>
	<property>
		<name>hive.metastore.local</name>
		<value>false</value>
	</property>

	<property>
		<name>javax.jdo.option.ConnectionURL</name>
		<value>jdbc:mysql://MySQL서버IP:MySQL서버포트/hive?createDatabaseIfNotExist=true</value>
	</property>

	<property>
		<name>javax.jdo.option.ConnectionDriverName</name>
		<value>com.mysql.jdbc.Driver</value>
	</property>

	<property>
		<name>javax.jdo.option.ConnectionUserName</name>
		<value>계정명</value>
	</property>

	<property>
		<name>javax.jdo.option.ConnectionPassword</name>
		<value>암호</value>
	</property>
</configuration>
```
<br />

##### 여기서는 위와 같이 MySQL을 메타스토어로 사용하지 않고 아래와 같이 아파치 더비를 사용했다.
##### 기본 디렉토리는 `/user/hive/warehouse` 로 설정했으며, 하이브 커맨드 라인 툴에서 칼럼명이 출력되도록 설정했다.
##### 마찬가지로 `hive-site.xml` 파일에 작성하면 된다.

```xml
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>

<configuration>
	<property>
		<name>hive.metastore.warehouse.dir</name>
		<value>/user/hive/warehouse/</value>
	</property>

	<property>
		<name>hive.cli.print.header</name>
		<value>true</value>
	</property>
</configuration>
```
<br />

##### 이제 하이브에서 업로드하는 데이터는 HDFS의 `/user/hive/warehouse`에 저장된다.  그리고 하이브에서 실행하는 잡의 여유 공간으로 HDFS의 `"/tmp/hive-유저명"` 디렉토리를 사용한다. 이 두 개의 디렉토리를 다음과 같이 미리 생성한 후 실행 권한도 함께 설정해야 한다.

```
hkl@googolhkl1:~$ hdfs dfs -mkdir /tmp
hkl@googolhkl1:~$ hdfs dfs -mkdir /user/hive
hkl@googolhkl1:~$ hdfs dfs -mkdir /user/hive/warehouse
hkl@googolhkl1:~$ hdfs dfs -chmod g+w /tmp
hkl@googolhkl1:~$ hdfs dfs -chmod g+w /user/hive/warehouse
```

##### 하둡 권한 설정이 완료되면 하이브르 설칠한 디렉토리에서 다음과 같이 하이브 명령어를 실행한다.

```
hkl@googolhkl1:~/apache-hive-1.2.1-bin$ ./bin/hive
```

##### 여기서 오류가 발생했다. 오류 내용은 다음과 같다. 

```
[ERROR] Terminal initialization failed; falling back to unsupported
java.lang.IncompatibleClassChangeError: Found class jline.Terminal, but interface was expected
    at jline.TerminalFactory.create(TerminalFactory.java:101)
    at jline.TerminalFactory.get(TerminalFactory.java:158)
    at jline.console.ConsoleReader.<init>(ConsoleReader.java:229)
    at jline.console.ConsoleReader.<init>(ConsoleReader.java:221)
    at jline.console.ConsoleReader.<init>(ConsoleReader.java:209)
    at org.apache.hadoop.hive.cli.CliDriver.getConsoleReader(CliDriver.java:773)
    at org.apache.hadoop.hive.cli.CliDriver.executeDriver(CliDriver.java:715)
    at org.apache.hadoop.hive.cli.CliDriver.run(CliDriver.java:675)
    at org.apache.hadoop.hive.cli.CliDriver.main(CliDriver.java:615)
    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
    at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
    at java.lang.reflect.Method.invoke(Method.java:606)
    at org.apache.hadoop.util.RunJar.run(RunJar.java:221)
    at org.apache.hadoop.util.RunJar.main(RunJar.java:136)

Exception in thread "main" java.lang.IncompatibleClassChangeError: Found class jline.Terminal, but interface was expected
    at jline.console.ConsoleReader.<init>(ConsoleReader.java:230)
    at jline.console.ConsoleReader.<init>(ConsoleReader.java:221)
    at jline.console.ConsoleReader.<init>(ConsoleReader.java:209)
    at org.apache.hadoop.hive.cli.CliDriver.getConsoleReader(CliDriver.java:773)
    at org.apache.hadoop.hive.cli.CliDriver.executeDriver(CliDriver.java:715)
    at org.apache.hadoop.hive.cli.CliDriver.run(CliDriver.java:675)
    at org.apache.hadoop.hive.cli.CliDriver.main(CliDriver.java:615)
    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
    at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
    at java.lang.reflect.Method.invoke(Method.java:606)
    at org.apache.hadoop.util.RunJar.run(RunJar.java:221)
    at org.apache.hadoop.util.RunJar.main(RunJar.java:136)
```

##### 이 오류는 다음과 같이 [해결](http://stackoverflow.com/questions/28997441/hive-startup-error-terminal-initialization-failed-falling-back-to-unsupporte)할 수 있다.

```
hkl@googolhkl1:~/apache-hive-1.2.1-bin$ vi ~/.bash_profile 
export HADOOP_USER_CLASSPATH_FIRST=true
hkl@googolhkl1:~/apache-hive-1.2.1-bin$ source ~/.bash_profile 
```

##### 만약 권한의 문제가 생기면 다음과 같이 HDFS의 /tmp/hive 디렉토리의 권한을 777로 설정한다.

```
hkl@googolhkl1:~/hadoop-2.6.0$ ./bin/hdfs dfs -chmod 777 /tmp/hive
```

##### 문제가 모두 해결하고, 다시 하이브를 실행하면 다음과 같이 정상적으로 실행된다.

```
hkl@googolhkl1:~/apache-hive-1.2.1-bin$ ./bin/hive

Logging initialized using configuration in jar:file:/home/hkl/apache-hive-1.2.1-bin/lib/hive-common-1.2.1.jar!/hive-log4j.properties
hive>
```
