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
