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
<br />

## 3. 테이블 생성
##### 하이브의 `CREATE TABLE`문은 SQL 규칙을 따르지만 테이블의 데이터 파일 생성 위치나 사용할 포맷 등에 관해 다양한 유연성을 주는 확장 기능을 제공한다.
```
hive (default)> CREATE TABLE IF NOT EXISTS mydb.employees(
              > name STRING COMMENT "EMployee name",
              > salary FLOAT COMMENT "Employee salary",
              > subordinates ARRAY<STRING> COMMENT "Names of subordinates",
              > deductions MAP<STRING, FLOAT> COMMENT "Keys are deductions name, value are percentages",
              > address STRUCT<street:STRING, city:STRING, state:STRING, zip:INT> COMMENT "Home address")
              > COMMENT "Description of the table"
              > TBLPROPERTIES ("create" = "hkl", "create_at" = "2016-08-26 12:57:00");
```
##### 어떠한 칼럼이든 데이터형을 지정한 이후에 주석을 추가할 수 있다. 데이터베이스 처럼 테이블 자체에도 주석을 추가할 수 있고, 여러 개의 테이블 속성을 넣을 수 있다. 대부분 사례에서 TBLPROPERTIES의 가장 큰 이점은 키-값 형태로 추가 문서화를 할 수 있다는 점이다.
##### 이미 존재하는 테이블에서 데이터를 제외한 스키마만 복사할 수 있다.
```
hive (default)> CREATE TABLE IF NOT EXISTS mydb.employees2
              > LIKE mydb.employees;
```
<br />

##### `SHOW TABLE`명령어는 테이블을 나열할 때 사용한다. 별도의 인자가 없으면 현재 작업하는 데이터베이스의 테이블을 보여준다.
```
hive (default)> USE mydb;

hive (mydb)> SHOW TABLES;
tab_name
employees
employees2
```

##### 만약 같은 데이트베이스 내에서 작업하지 않더라도 아래 명령어를 실행하면 다른 데이트베이스의 테이블을 조회할 수 있다.
```
hive (mydb)> USE default;

hive (default)> SHOW TABLES IN mydb;
tab_name
employees
employees2
```
##### 너무 많은 테이블이 있을때 정규표현식을 이용하녀 나열할 테이블을 줄일 수 있다.
```
hive (default)> SHOW TABLES IN mydb "employee.";
tab_name
employees
```
<br />

##### 테이블을 자세히 살펴보기 위해서 `DESCRIBE EXTENDED mydb.employees`명령어를 사용할 수 있다.
```
hive (default)> DESCRIBE EXTENDED mydb.employees;
col_name	data_type	comment
name                	string              	EMployee name       
salary              	float               	Employee salary     
subordinates        	array<string>       	Names of subordinates
deductions          	map<string,float>   	Keys are deductions name, value are percentages
address             	struct<street:string,city:string,state:string,zip:int>	Home address        
	 	 
Detailed Table Information	Table(tableName:employees, dbName:mydb, owner:hkl, createTime:1472183873, lastAccessTime:0, retention:0, sd:StorageDescriptor(cols:[FieldSchema(name:name, type:string, comment:EMployee name), FieldSchema(name:salary, type:float, comment:Employee salary), FieldSchema(name:subordinates, type:array<string>, comment:Names of subordinates), FieldSchema(name:deductions, type:map<string,float>, comment:Keys are deductions name, value are percentages), FieldSchema(name:address, type:struct<street:string,city:string,state:string,zip:int>, comment:Home address)], location:hdfs://googolhkls-cluster/user/hive/warehouse/mydb.db/employees, inputFormat:org.apache.hadoop.mapred.TextInputFormat, outputFormat:org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat, compressed:false, numBuckets:-1, serdeInfo:SerDeInfo(name:null, serializationLib:org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe, parameters:{serialization.format=1}), bucketCols:[], sortCols:[], parameters:{}, skewedInfo:SkewedInfo(skewedColNames:[], skewedColValues:[], skewedColValueLocationMaps:{}), storedAsSubDirectories:false), partitionKeys:[], parameters:{transient_lastDdlTime=1472183873, create=hkl, comment=Description of the table, create_at=2016-08-26 12:57:00}, viewOriginalText:null, viewExpandedText:null, tableType:MANAGED_TABLE)	
```
##### 읽기가 불편하다. 이럴 땐 `EXTENDED` 대신 `FORMATTED`를 입력하면 읽기 좋고 더 많은 내용을 출력한다.
```
hive (default)> DESCRIBE FORMATTED mydb.employees;
OK
col_name	data_type	comment
# col_name            	data_type           	comment             
	 	 
name                	string              	EMployee name       
salary              	float               	Employee salary     
subordinates        	array<string>       	Names of subordinates
deductions          	map<string,float>   	Keys are deductions name, value are percentages
address             	struct<street:string,city:string,state:string,zip:int>	Home address        
	 	 
# Detailed Table Information	 	 
Database:           	mydb                	 
Owner:              	hkl                 	 
CreateTime:         	Fri Aug 26 12:57:53 KST 2016	 
LastAccessTime:     	UNKNOWN             	 
Protect Mode:       	None                	 
Retention:          	0                   	 
Location:           	hdfs://googolhkls-cluster/user/hive/warehouse/mydb.db/employees	 
Table Type:         	MANAGED_TABLE       	 
Table Parameters:	 	 
	comment             	Description of the table
	create              	hkl                 
	create_at           	2016-08-26 12:57:00 
	transient_lastDdlTime	1472183873          
	 	 
# Storage Information	 	 
SerDe Library:      	org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe	 
InputFormat:        	org.apache.hadoop.mapred.TextInputFormat	 
OutputFormat:       	org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat	 
Compressed:         	No                  	 
Num Buckets:        	-1                  	 
Bucket Columns:     	[]                  	 
Sort Columns:       	[]                  	 
Storage Desc Params:	 	 
	serialization.format	1  
```
<br />

### 3.1 매니지드 테이블(내부 테이블)
##### 우리가 지금까지 생성한 테이블은 하이브가 데이터의 생명 주기를 제어하기 때문에 `매니지드 테이블`또는 `내부 테이블`이라고 부른다, 앞서 봐온 것처럼 하이브는 `hive.metastore.warehouse.dir(예를 들어 /user/hive/warehouse)` 속성에서 정의한 디렉토리에 하위 디렉토리를 만들어 테이블을 저장한다.
##### 매니지드 테이블을 삭제할 때 하이브는 테이블 내의 데이터를 삭제한다.
##### 하지만 매니지드 테이블은 다른 도구와 데이터를 공유하기에는 조금 불편한 점이 있다. 예를 들면 피그나 다른 도구로 테이블을 생성하고 이 데이터에 대해 쿼리를 실행하고는 싶으나 하이브가 데이터를 소유하지 않기를 원할 수 있다. 이럴 때 매니지드 테이블 대신 `외부 테이블`을 정의하여 하이브가 해당 데이터를 소유하지 않도록 한다.
<br />

### 3.2 외부 테이블
##### 주식 시장 데이터를 분석하는 사례를 생각해보자. 주기적으로 Infochimps(http://infochimps.com/datasets)와 같은 데이터 소스로부터 나스닥과 NYSE데이터를 가져온 뒤 다양한 도구를 이용해 이 데이터를 연구하고자 한다. 다음에 사용할 스키마와 이러한 데이터 소스의 스키마는 일치한다. 데이터 파일이 분산 파일시스템의 `/data/stocks` 디렉토리 안에 존재한다고 가정하자.
##### 아래 테이블 선언은 `/data/stocks` 내 쉼표로 구분된 데이터를 가진 파일을 읽을 수 있는 외부 테이블을 생성한다.

```
hive> CREATE EXTERNAL TABLE IF NOT EXISTS stocks(
    > exchange		STRING,
    > symbol 		STRING,
    > ymd		STRING,
    > price_open	FLOAT,
    > price_high	FLOAT,
    > price_low		FLOAT,
    > price_close	FLOAT,
    > volume		INT,
    > price_adj_close   FLOAT)
    > ROW FORMAT DELIMITED FIELDS TERMINATED BY ","
    > LOCATION "/data/stocks";
```
##### `EXTERNAL` 예약어는 해당 테이블이 외부에 있으면 `LOCATION ...`절에서 지정한 위치에 존재한다는 것을 하이브에게 알려준다.
##### 외부에 존재하기 때문에 하이브는 해당 데이터를 소유하지 않는다. 따라서 테이블을 삭제할 때 테이블의 메타데이터는 지워지지만 해당 데이터는 지우지는 않는다. 
##### 매니지드 테이블(내부 테이블)과 외부 테이블 사이에는 작은 차이점이 존재한다. 일부 HiveQL 문은 외부 테이블에 대해서 적용되지 않는데 이것은 다음에 살펴보자.
##### 매니지드 테이블인지 외부 테이블인지 여부는 `DESCRIBE EXTENDED 테이블명` 또는 `DESCRIBE FORMATTED 테이블명'을 실행하면 알 수 있다.
##### 매매니지드 테이블과 마찬가지로 이미 존재하는 테이블로부터 데이터를 제외하고 스키마를 복사할 수 있다.
```
hive> CREATE EXTERNAL TABLE IF NOT EXISTS mydb.employees3
    > LIKE mydb.employees
    > LOCATION "/path/to/data";
```
<br />


## 4. 파티셔닝된 매니지드 테이블
##### 많은 형태를 가질 수 있지만, 수평적으로 부하를 분산하기 위해 자주 사용하는 데이터를 사용자와 물리적으로 가까운 위치에 두는 등의 목적으로 사용한다.
##### 하이브에는 파티셔닝된 테이블 개념이 있다. 이 기능은 성능상으로 중요한 이점이 있고 계층구조와 같은 논리 형태로 데이터를 구성하는데 도움을 즐 수 있다.
##### 먼저 파티셔닝된 매니지드 테이블을 알아보자. employees 테이블로 돌아가서 우리가 아주 거대한 다국적 기업에서 일한다고 가정해보자. 인사 관리팀 사람들은 종종 특정 나라나 첫 번째 구획(미국의 주 단위 혹은 캐나다의 프로빈스)의 조건과 함께 쿼리를 실행한다. 단순하게 하기 위해 특정 지역을 `state` 단어로 쓰겠다. `address` 필드에는 반복되는 `state`값이 있다. 이것은 `state`파티션을 구분 짓는데 사용할 수 있다. `address`필드에서 `state`를 삭제할 수 있다. 주소에서 값을 빼내기 위해서 `address.state` 표현을 사용하므로 쿼리상의 모호함은 없다. 이제 아래와 같이 데이터를 `country`와 `state`로 파티셔닝해보자.

```
hive (mydb)> CREATE TABLE employees(
           > name STRING,
           > salary FLOAT,
           > subordinates ARRAY<STRING>,
           > deductions MAP<STRING, FLOAT>,
           > address STRUCT<street:STRING, city:STRING, state:STRING, zip:INT>
           > )
           > PARTITIONED BY (country STRING, state STRING);
```

##### 테이블을 파티셔닝하는 것은 하이브가 데이터 저장소를 구성하는 방식을 바꾸도록 한다. 만약 mydb 데이터베이스 내에 테이블을 생성한다면 해당 테이블에 대한 `employees`디렉토리는 여전히 존재한다.

```
hdfs://googolhkls-cluster/user/hive/warehouse/mydb.db/employees
```
##### 하지만 하이브는 파티션 구조를 반영하기 위해 다음과 같이 하위 디렉토리를 생성한다.
```
...
.../employees/country=CA/state=AB
.../employees/country=CA/state=BC
...
.../employees/country=US/state=AL
.../employees/country=US/state=AK
```
<br />

### 4.1 파티셔닝된 외부 테이블
##### 하이브에서 외부 테이블도 매니지드 테이블과 마찬가지로 파티셔닝할 수 있다. 일반적으로 아주 많은 데이터셋을 생성하는 곳에서 사용한다. 외부 테이블을 파티셔닝하는 것은 쿼리 성능을 최적화하는 동시에 다른 도구와 데이터를 공유하는 방법을 제공한다.

##### 로그 파일 분석이라는 예를 생각해보자. 대부분은 `시간, 심각도(ERROR,WARNING, INFO), 서버 이름, 프로세스 아이디, 임의의 텍스트 메시지로 이루어진 표준 포맷의 로그 메시지`를 사용한다. 이때 ETL(추출, 변환, 로딩) 프로세스가 로그 메시지를 탭으로 구분된 레코드로 변환하고, 타임스탬프를 년, 월, 일 필드로 나누고, 시, 분, 초를 `hms` 필드로 합쳐 저장한다. 이러한 메시지 분석 작업은 하이브나 피그에서 제공하는 함수를 이용하여 수행할 수 있다.공간을 절약하기 위해서 타임스탬프와 관련된 필드는 작은 크기의 정수형 필드를 사용할 수 있다.

```
hive> CREATE EXTERNAL TABLE IF NOT EXISTS log_messages(
    > hms 		INT,
    > severity		STRING,
    > server		STRING,
    > process_id	INT,
    > message		STRING)
    > PARTITIONED BY (year INT, month INT, day INT)
    > ROW FORMAT DELIMITED FIELDS TERMINATED BY "\t";
```
##### 하루에 쌓이는 로그 데이터자 파티셔닝과 쿼리를 충분히 빠르게 처리할 수 있는 크기라고 가정하자.
##### 파티셔닝하지 않은 외부 테이블인 `stocks`를 생성할 때 (LOCATION 문 필요)를 떠올려보자.
##### `ALTER TABLE`문으로 파티션을 추가하여 파티셔닝된 외부 테이블을 만들 수 있다, 년, 월, 일에 대해서 각 파티션의 키값을 지정했다고 가정하자. 예를들어 다음과 같이 2012년 1월 2일 파티션을 추가할 수 있다.
```
hive> ALTER TABLE log_messages ADD PARTITION(year = 2012, month = 1, day =2)
    > LOCATION "hdfs"//googolhkls-cluster/data/log_message/2012/01/02';
```
##### 사용할 디렉토리 규칙은 사용자가 결정할 수 있는데, 반드시 지켜야 하는 것은 아니지만 데이터를 구성하는 논리 방식인 계층적 디렉토리 구조를 사용한다.
<br />


### 4.2 테이블 저장 포맷 사용자화
##### 하이브는 `STORED AS TEXTFILE`문을 통해 텍스트 파일 형식을 기본으로 사용한다. 그리고 사용자는 테이블을 생성할 때 다양한 구분자를 기본값 대신 쓸 수 있다. `employees`테이블 정의를 다시 만들어 보자.
```
hive (mydb)> CREATE TABLE employees(
           > name STRING,
           > salary FLOAT,
           > subordinates ARRAY<STRING>,
           > deductions MAP<STRING, FLOAT>,
           > address STRUCT<street:STRING, city:STRING, state:STRING, zip:INT>
           > )
           > ROW FORMAT DELIMITED
           > FIELDS TERMINATED BY "\001"
           > COLLECTION ITEMS TERMINATED BY "\002"
           > MAP KEYS TERMINATED BY "\003"
           > LINES TERMINATED BY "\n"
           > STORED AS TEXTFILE;
```
##### `TEXTFILE`은 모든 필드가 영어 알파벳이나 숫자, 국제 문자셋 내의 문자를 이용하여 인코딩되는 것을 암시한다. 하이브는 출력되기 않는 문자를 구분자로 이용하기도 한다. `TEXTFILE`을 사용할 때에는 각 줄을 하나의 레코드로 간주한다. 

##### 하이브는 레코드들이 파일 내에서 인코딩되는 방법과 컬럼들이 레코드 내에서 인코딩되는 방법을 구분한다. 사용자는 이러한 각각의 방식을 바꿀 수 있다.
##### 레코드 인코딩은 `TEXTFILE`내부에 존재하는 자바 클래스(org.apache.hadoop.mapred.TextInputFormat)와 같은 입력 포맷 객체에 의해 다루어진다. 그리고 마지막 이름인 `TextInputFormat`은 최하위 패키지인 `mapred` 내의 클래스가 된다. 

##### 써드-파티 입출력 포맷과 `SerDe`는 다양한 파일 포맷으로 사용자가 정의할 수 있다.
##### 아래는 에이브로 통신 규칙을 통해서 접근할 수 있는 파일을 위한 입출력 파일 포맷과 SerDe다.  에이브로에 대해서는 `에이브로 하이브 SerDe`에서 알아보자.

```
hive> CREATE TABLE kst
    > PARTITIONED BY (ds string)
    > ROW FORMAT SERDE "com.linkedin.haivvreo.AvroSerDe"
    > WITH SERDEPROPERTIES ("schema.url"="http://schema_provider/kst.avsc")
    > STORED AS
    > INPUTFORMAT "com.linkedin.haivvreo.AvroContainerInputFormat"
    > OUTPUTFORMAT "com.linkedin.haivvreo.AvroContainerOutputFormat";
```

##### 위에서 `ROW FORMAT SERDE...`문은 사용할 `SerDe`를 지정한다. 하이브는 `SerDe`에서 사용될 설정 정보를 넘겨주기 위해 `WITH SERDEPROPERTIES` 기능을 제공한다. 여기서 지정하는 속성은 `SerDe`에서만 처리되고 하이브는 전혀 관여하지 않는다. 각 속성의 이름과 값은 작은 따옴표로 둘러 싸야 한다.
<br />

## 5. 테이블 삭제
##### 하이브는 SQL에서도 이미 친숙한 `DROP TABLE` 명령어를 지원한다.
```
hive> DROP TABLE IF EXISTS employees;
```
##### 외부 테이블이라면 메타데이터는 지워지지만 데이터는 남아있다.

  
## 6. 테이블 변경
##### 대부분의 테이블 속성은 `ALTER TABLE` 문을 통해서 변경한다. 이는 결국 테이블의 메타데이터는 변경시키지만, 데이터 자체는 변경시키지 않는다. 이러한 문은 스키마(스키마는 테이블의 열이름 이라고 생각하면 된다.)) 내에 실수가 있는 것을 바로 잡거나 파티션 위치를 이동하거나 그 외에 다른 동작을 할 때 사용한다.

  
### 6.1 테이블명 변경
##### 아래 문장을 통해 `airline_delay`테이블명을 `renamed_airline_delay`로 변경할 수 있다.

```
hive> ALTER TABLE airline_delay RENAME TO renamed_airline_delay;
```

  
### 6.2 테이블 파티션 추가, 변경, 삭제
##### 테이블에 새로운 파티션을 넣으려면 `ALTER TABLE table_이름 ADD PARTITION` 문을 수행한다. 아래에서 추가 옵션과 함께 동일한 명령을 보여준다.
```
hive> ALTER TABLE log_message ADD IF NOT EXISTS
PARTITION (year = 2011, month = 1, day = 1) LOCATION '/logs/2011/01/01'
PARTITION (year = 2011, month = 1, day = 2) LOCATION '/logs/2011/01/02'
PARTITION (year = 2011, month = 1, day = 3) LOCATION '/logs/2011/01/03'
...;
```

  
##### 유사하게 아래와 같이 파티션 위치를 변경할 수 있다.
```
hive> ALTER TABLE log_message PARTITION(year = 2011, month = 12, day =12)
SET LOCATION 's3n://ourbucket/logs/2011/01/02';
```
##### 이 명령어는 이전 위치에서 데이터를 옮기거나 삭제하지 않는다.

  
##### 마지막으로 다음과 같이 파티션을 삭제할 수 있다.
```
hive> ALTER TABLE log_message DROP IF EXISTS PARTITION(year = 2011,  month =12, day =2);
```

##### `IF EXISTS~는 마찬가지로 옵션이다. 매니지드 테이블이라면 `ALTER TABLE ... ADD PARTITION`으로 생성한 파티션일지라도 데이터와 메타데이터가 동시에 삭제된다. 외부 테이블은 데이터가 삭제되지 않는다.

  
### 6.3 칼럼 변경
##### 다음과 같이 칼럼명이나 위치, 데이터형 혹은 주석을 변경할 수 있다.
```
hive> ALTER TABLE test
    > CHANGE COLUMN age number STRING
    > COMMENT '이름과 과 나이를 주민번호와 이름으로 수와 이름으로 수정'
    > FIRST;
```
##### `test`테이블의 칼럼은 name,age 였다. 이 구조를 위 명령어로 number,name구조로 바꾸고 number는 STRING타입이다. FIRST는 지금 변경하는 칼럼을 가장 앞으로 오라는 의미이고 `name`뒤로 옮기고 싶다면 `AFTER name`명령어를 사용하면 된다.
##### 마찬가지로 이 명령어는 메타데이터만 변경한다. 컬럼을 옮길 때 데이터는 새로운 스키마와 일치하거나 다른수단을 이용해서 일치할 수 있도록 변경해야 한다.

  
### 6.4 칼럼 추가
##### 이미 존재하는 칼럼의 마지막과 파티셔닝 칼럼 앞에 새로운 칼럼을 추가할 수 있다.
```
hive> ALTER TABLE test ADD COLUMNS(
    > age INT COMMENT '나이,
    > address STRING COMMENT '주소');

```
##### 새로운 칼럼의 위치가 마음에 들지 않는다면 `ALTER COLUMN table_이름 CHANGE COLUMN`문을 이용해 올바른 위치로 변경할 수 있다.

  
### 6.5 칼럼 삭제 및 교체
##### 아래는 이미 존재하는 모든 칼럼을 삭제하고 새로운 칼럼을 추가하는 명령이다.
```
hive> ALTER TABLE test REPLACE COLUMNS(
    > name STRING,
    > age INT,
    > phone STRING);
```

  
### 6.6 테이블 속성 변경
##### 다음과 같이 테이블 속성을 추가하거나 변경할 수 있지만 삭제는 불가능하다.
```
hive> ALTER TABLE test SET TBLPROPERTIES(
    > 'notes' = 'hello world');
```

  
### 6.7 저장소 속성 변경
##### 포맷과 SerDe속성을 변경하는 여러 가지 `ALTER TABLE` 문이 있다.
##### 다음은 시퀀스 파일로 변경하는 예제다.
```
hive> ALTER TABLE test
    > SET FILEFORMAT SEQUENCEFILE;
```
