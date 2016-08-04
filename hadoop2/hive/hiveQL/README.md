# 하이브QL
##### 하이브는 하이브QL이라는 SQL문과 매우 유사한 언어를 제공한다. 대부분의 기능은 SQL과 유사하지만 다음과 같은 차이점이 있으니 사용할 때 참고하기 바란다.

1. 하이브에서 사용하는 데이터가 HDFS에 저장되는데, HDFS가 한 번 저장한 파일은 수정할 수 없기 때문에 `UPDATE`와 `DELETE`는 사용할 수 없다. 같은 이유로 `INSERT`도 비어 있는 테이블에 입력하거나, 이미 입력된 데이터를 덮어 쓰는 경우에만 사용 가능하다. 그래서 하이브QL은 `"INSERT OVERWRITE"`라는 키워드를 사용하게 된다.
2. SQL은 어떠한 절에서도 서브쿼리를 사용할 수 있지만 하이브QL은 `FROM`절에서만 서브 쿼리를 사용할 수 있다.
3. SQL의 뷰는 업데이트할 수 있고, 구체화된 뷰 또는 비구체화된 뷰를 지원한다. 하지만 하이브QL의 뷰는 읽기 전용이며, 비 구체화된 뷰만 지원한다.
4. `SELECT` 문을 사용할 때 `HAVING` 절을 사용할 수 없다.
5. 저장 프로시저(sotred procedure)를 지원하지 않는다. 대신 맵리듀스 스크립트를 실행할 수 있다.

##### 여기서는 미국 항공 운항 지연 데이터를 분석하기 위한 하이브QL 쿼리문을 작성하겠다. 참고로 하이브QL은 대소문자를 구분하지 않는다.


## 분석용 데이터 준비
##### ASA(American Standards Association, 미국 규격 협회)에서 2009년에 공개한 미국 항공편 운항 통계 데이터를 이용한다.
##### 우선 [http://stat-computing.org/dataexpo/2009/](http://stat-computing.org/dataexpo/2009/)를 방문하자.
##### 여기에서 데이터를 다운로드 할 수 있다. 우리는 1987~2008년의 반복적인 다운로드를 스크립트를 작성해서 할 것이다.
##### 아래와 같은 순서로 디렉토리 생성, 스크립트 생성, 권한부여, 실행 순으로 실행하자

```
hkl@googolhkl1:~/$ mkdir airlineData
hkl@googolhkl1:~/$ cd airlineData
hkl@googolhkl1:~/airlineData/$ vi download.sh
#!/bin/bash

# 미국 항공편 운항 통계 데이터를 다운로드한다. 1987~ 2008년 까지의 데이터를 다운로드한다.

for((year=1987; year<=2008; year++))
do
	wget http://stat-computing.org/dataexpo/2009/$year.csv.bz2 #다운로드
	bzip2 -d $year.csv.bz2 #압축해제
	sed -e '1d' $year.csv > $year_temp.csv	# 첫 번째 줄 제거
	mv $year_temp.csv $year.csv
done

hkl@googolhkl1:~/airlineData/$ chmod +x download.sh
hkl@googolhkl1:~/airlineData/$ ./download.sh
```

##### 이제 다운로드가 진행된다. 전부 완료하면 다운로드 받은 파일들을 확인할 수 있다.

```
hkl@googolhkl1:~/airlineData$ ls
1987.csv  1989.csv  1991.csv  1993.csv  1995.csv  1997.csv  1999.csv  2001.csv  2003.csv  2005.csv  2007.csv  download.sh
1988.csv  1990.csv  1992.csv  1994.csv  1996.csv  1998.csv  2000.csv  2002.csv  2004.csv  2006.csv  2008.csv
```
<br />

## 1. 테이블 생성
##### 하둡은 `HDFS`에 저장된 파일에 직접 접근해서 처리하지만 하이브는 메타스토어에 저장된 테이블을 분석한다. 데이터를 조회하기 전에 먼저 테이블을 생성해야 한다. 아래와 같이 `CREATE TABLE`을 이용해 `airline_delay` 테이블을 생성한다. SQL문의 `CREATE TABLE`과 매우 유사하게 느껴 질 것이다.

```
CREATE TABLE airline_delay(Year INT, Month INT,
		DayofMonth INT, DayOfWeek INT,
		DepTime INT, CRSDepTime INT,
		ArrTime INT, CRSArrTime INT,
		UniqueCarrier STRING, FlightNum INT,
		TailNum STRING, ActualElapsedTime INT,
		CRSElapsedTime INT, AirTime INT,
		ArrDelay INT, DepDelay INT,
		Origin STRING, Dest STRING,
		Distance INT, TaxiIN INT,
		TaxiOut INT, Cancelled INT,
		CancellationCode STRING
		COMMENT 'A = carrier, B = weather, C = NAS, D = security',
		Diverted INT COMMENT '1 = yes, 0 = no',
		CarrierDelay STRING, WeatherDelay STRING,
		NASDelay STRING, SecurityDelay STRING,
		LateAircraftDelay STRING)
COMMENT '데이터는 1987년 부터 2008년까지 미국의 모든 비행기사의 도착,출력의 관한 데이터이다. '
PARTITIONED BY (delayYear INT)
ROW FORMAT DELIMITED
	FIELDS TERMINATED BY ','
	LINES TERMINATED BY '\n'
	STORED AS TEXTFILE;
```

##### 이 쿼리문은 미국 항공 운항 지연 데이터의 양식과 동일하게 29개의 칼럼을 정의한다. 하이브QL의 칼럼 타입은 아래와 표와 같고, airline_delay 테이블에서는 INT와 STRING 타입만 사용했다.

| 타입 | 내용 |
| --- | --- |
| TINYINT | 1바이트 정수 |
| SMALLINT | 2바이트 정수 |
| INT | 4바이트 정수 |
| BIGINT | 8바이트 정수 |
| BOOLEAN | TRUE/FALSE |
| FLOAT | 단정밀도 부동 소수점 |
| DOUBLE | 배정밀도 부동 소수점 |
| STRING | 문자열 |
<br />

##### `CREATE TABLE 테이블명(칼럼명 칼럼_타입, ...)` 과 같은 방식으로 테이블을 생성하며, 각 칼럼은 콤마로 구분한다. 위 예제에서는 `LateAircraftDelay STRING)` 까지만 선언해도 테이블이 생성되며, 이후에 있는 구문은 테이블에 대한 부가적인 정보를 설정하는 부분이다.
##### `COMMENT '데이터는 ... 데이터이다. '` 절은 테이블의 설명을 참고용으로 등록하는 부분이다.
##### `PARTITIONED BY (delayYear INT)` 절은 테이블의 파티션을 설정하는 부분이다. 하이브는 쿼리문의 수행 속도를 향상시키기 위해 파티션을 설정할 수 있다. 파티션을 설정하면 해당 테이블의 데이터를 파티션별로 디렉토리를 생성해서 저장하게 된다. 실제로 이 테이블의 데이터를 업로드한 후 HDFS 디렉토리를 조회하면 파티션키인 delayYear별로 디렉토리가 생성돼 있다. 참고로 파티션키는 해당 테이블의 새로운 칼럼으로 추가된다.
##### `ROW FORMAT` 절은 해당 테이블 내의 데이터가 어떠한 형식으로 저장되는지 설정한다. 이 쿼리문은 필드를 콤마 기준으로 구분하고, 행과 행은 \n 값으로 구분한다.
##### 마지막으로 `STORED AS` 절은 데이터 저장 파일 포맷을 의미한다. 하이브는 텍스트 파일을 위한 `TEXTFILE`과 `SEQUENCEFILE`을 지원한다.

##### airline_delay 테이블을 생성하고 나면 다음과 같이 메타스토어 데이터베이스에 저장된 테이블목록을 조회한다.

```
hive> SHOW TABLES;
OK
tab_name
airline_delay
Time taken: 0.164 seconds, Fetched: 1 row(s)

```

##### 참고로 테이블을 생성할 때 `CREATE`문 뒤에 `EXTERNAL` 키워드를 추가할 수 있다. `EXTERNAL` 키워드로 생성하는 테이블은 외부 테이블이라고 한다. 외부 테이블은 `hive.metastore.warehouse.dir` 속성이 가리키는 디렉토리에 저장하지 않고, 테이블 생성 시 설정한 경로로 데이터를 저장한다. 사용자가 실수로 테이블을 `DROP` 했더라도 데이터가 보존된다는 장점이 있다.
##### 참고로 위에서 작성했던 예제는 아래와 같이 외부 테이블을 생성할 수 있다. 기존 코드와의 차이점은 `CREATE`문 뒤에 `EXTERNAL`이 추가되고 마지막 줄에 `LOCATION`이라는 키워드 뒤에 데이터를 저장할 경로가 설정된다는 것이다.

```
CREATE EXTERNAL TABLE airline_delay(Year INT, Month INT,
		DayofMonth INT, DayOfWeek INT,
		DepTime INT, CRSDepTime INT,
		ArrTime INT, CRSArrTime INT,
		UniqueCarrier STRING, FlightNum INT,
		TailNum STRING, ActualElapsedTime INT,
		CRSElapsedTime INT, AirTime INT,
		ArrDelay INT, DepDelay INT,
		Origin STRING, Dest STRING,
		Distance INT, TaxiIN INT,
		TaxiOut INT, Cancelled INT,
		CancellationCode STRING
		COMMENT 'A = carrier, B = weather, C = NAS, D = security',
		Diverted INT COMMENT '1 = yes, 0 = no',
		CarrierDelay STRING, WeatherDelay STRING,
		NASDelay STRING, SecurityDelay STRING,
		LateAircraftDelay STRING)
COMMENT '데이터는 1987년 부터 2008년까지 미국의 모든 비행기사의 도착,출력의 관한 데이터이다. '
PARTITIONED BY (delayYear INT)
ROW FORMAT DELIMITED
	FIELDS TERMINATED BY ','
	LINES TERMINATED BY '\n'
	STORED AS TEXTFILE
LOCATION '/usr/hkl/airline_delay';
```

##### 이번에는 `airline_delay` 테이블의 칼럼이 정상적으로 구성돼 있는지 `DESCRIBE` 명령어를 이용해 확인해 보겠다. `CREATE TABLE(..)` 내에 있는  29개의 칼럼과 파티션 칼럼인 delayYear이 모두 출력된다.

```
hive> DESCRIBE airline_delay;
OK
col_name	data_type	comment
year                	int                 	                    
month               	int                 	                    
dayofmonth          	int                 	                    
dayofweek           	int                 	                    
deptime             	int                 	                    
crsdeptime          	int                 	                    
arrtime             	int                 	                    
crsarrtime          	int                 	                    
uniquecarrier       	string              	                    
flightnum           	int                 	                    
tailnum             	string              	                    
actualelapsedtime   	int                 	                    
crselapsedtime      	int                 	                    
airtime             	int                 	                    
arrdelay            	int                 	                    
depdelay            	int                 	                    
origin              	string              	                    
dest                	string              	                    
distance            	int                 	                    
taxiin              	int                 	                    
taxiout             	int                 	                    
cancelled           	int                 	                    
cancellationcode    	string              	A = carrier, B = weather, C = NAS, D = security
diverted            	int                 	1 = yes, 0 = no     
carrierdelay        	string              	                    
weatherdelay        	string              	                    
nasdelay            	string              	                    
securitydelay       	string              	                    
lateaircraftdelay   	string              	                    
delayyear           	int                 	                    
	 	 
# Partition Information	 	 
# col_name            	data_type           	comment             
	 	 
delayyear           	int                 	                    
Time taken: 5.107 seconds, Fetched: 35 row(s)

```

##### 이미 생성된 테이블은 `ALTER TABLE`을 이용해 수정할 수 있다. 예를 들어, 테이블 이름은 `ALTER TABLE`에 `RENAME` 옵션을 설정해 변경할 수 있다.
> ALTER TABLE airline_delay RENAME TO delay_statics;

##### 기존의 테이블의 칼럼을 추가할 때도 다음과 같이 `ADD COLUMNS` 옵션을 설정하면 된다. 여러 개의 칼럼을 추가할 경우 콤마로 구분해서 입력한다.
> ALTER TABLE delay_statics ADD COLUMNS (delayMonth STRING);

##### 테이블을 삭제할 때는 `DROP TABLE`을 이용하면 된다. 이 경우 메타스토어 데이터베이스에 저장된 테이블과 HDFS에 저장된 데이터가 모두 삭제된다. `DROP TABLE`을 실행할 때 실행 여부를 묻지 않으므로 중요한 테이블이 삭제되지 않도록 주의해야 한다. 참고로 `EXTERNAL` 키워드를 이용해 외부 테이블을 생성했다면 데이터는 남아 있고 메타데이터만 삭제된다.
> DROP TABLE delay_statics;
