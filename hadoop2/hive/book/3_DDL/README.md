# 데이터 정의
##### HiveQL은 하이브 쿼리 언어다. 널리 사용 중인 모든 SQL 언어와 마찬가지로 HiveQL 또한 ANSI SQL 표준의 어떠한 버전에도 완벽하게 따르지는 않는다. MySQL과 가장 비슷한데 큰 차이점이 존재한다. 하이브는 로우 레벨의 삽입과 변경, 삭제를 지원하지 않는다. 트랜잭션 또한 지원하지 않는다. 대신 하둡이 지원하는 범위 안에서 보다 나은 성능을 위해 확장할 수 있는 기능을 제공하고 사용자가 정의한 확장과 외부 프로그램을 하이브와 연동할 수 있다.



## 1. 하이브에서의 데이터베이스
##### 하이브에서 데이터베이스 개념은 단지 `테이블의 카탈로그 또는 네임스페이스`다. 이는 큰 프로젝트에서 테이블명의 충돌을 막는 방법이다.

```
hive> CREATE DATABASE financials;
```
##### 만약 `financials`테이블이 이미 존재하면 하이브는 에러를 보내지만 아래 명령을 사용하면 에러를 막을 수 있다.
```
hive> CREATE DATABASE IF NOT EXIST financials;
```
<br />

##### `SHOW`로 존재하는 데이터베이스를 확인할 수 있다.
```
hive (default)> SHOW DATABASES;
OK
database_name
default
financials
```
##### 정규표현식을 이용하여 나열할 데이터베이스를 제한할 수 있다. 아래는 'd'로 시작하고 어떠한 문자로 끝날 수 있는 데이터베이스 이름만 나열한 것이다.
```
hive (default)> SHOW DATABASES LIKE "h*";
OK
database_name
default
```
<br />

##### 하이브는 각 데이터베이스마다 별도의 디렉토리를 생성하고 테이블을 그 하위 디렉토리에 저장한다. 디렉토리를 가지지 않는 기본 데이터베이스에 존재하는 테이블은 예외다.
##### 데이터베이스 디렉토리는 hive.metastore.warehouse.dir 속성으로 설정한 최상위 디렉토리 밑에 생성된다.
##### 아래는 새로운 데이터베이스의 기본 디렉토리 위치를 변경하는 예제다.
```
hive> CREATE DATABASE financials
    > LOCATION "/hello/world/directory";
```

##### `DESCRIBE DATABASE <database>` 명령어 실행 시 나타날 주석은 아래처럼 데이터베이스 생성시 입력할 수 있다. `DESCRIBE DATABASE`명령어는 데이터베이스 디렉토리의 위치도 보여준다.
```
hive> CREATE DATABASE financials
    > COMMENT "hello hive!";

hive (default)> DESCRIBE DATABASE financials;
OK
db_name	comment	location	owner_name	owner_type	parameters
financials	hello hive!	hdfs://googolhkls-cluster/user/hive/warehouse/financials.db	hkl	USER	
Time taken: 0.024 seconds, Fetched: 1 row(s)
```
<br />

##### 데이터베이스 키-값 속성을 지정할 수 있다. 이 속성은 `DESCRIBE DATABASE EXTENDED <database>` 문에서 정보를 출력할 때만 사용한다.

```
hive (default)> CREATE DATABASE financials
              > WITH DBPROPERTIES ("create" = "googolhkl", "data" = "2016-08-26");

hive (default)> DESCRIBE DATABASE financials;
db_name	comment	location	owner_name	owner_type	parameters
financials		hdfs://googolhkls-cluster/user/hive/warehouse/financials.db	hkl	USER	

hive (default)> DESCRIBE DATABASE EXTENDED financials;
db_name	comment	location	owner_name	owner_type	parameters
financials		hdfs://googolhkls-cluster/user/hive/warehouse/financials.db	hkl	USER	{data=2016-08-26, create=googolhkl}
```
<br />

##### `USE`명령어는 작업 데이터베이스를 설정하는데 사용한다.
```
hive> USE financials;
```
<br />

##### `DROP`명령어는 데이트베이스를 삭제할 때 사용한다.
```
hive> DROP DATABASE IF EXISTS financials;
```
##### 기본적으로 하이브는 테이블이 있는 데이터베이스는 삭제하는 것을 허용하지 않지만 `CASCADE` 예약어로 삭제할 수 있다.
```
hive> DROP DATABASE IF EXISTS financials CASCADE;
```
<br />


## 2. 데이터베이스 변경
##### `ALTER DATABASE` 명령어를 이용하면 데이트베이스의 DBPROPERTIES 내 키-값 속성을 설정할 수 있다. 하지만 `데이터베이스 이름이나 디렉토리 위치같은 메타데이터는 변경될 수 없다.`
