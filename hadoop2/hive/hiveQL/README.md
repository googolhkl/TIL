# 하이브QL
##### 하이브는 하이브QL이라는 SQL문과 매우 유사한 언어를 제공한다. 대부분의 기능은 SQL과 유사하지만 다음과 같은 차이점이 있으니 사용할 때 참고하기 바란다.

1. 하이브에서 사용하는 데이터가 HDFS에 저장되는데, HDFS가 한 번 저장한 파일은 수정할 수 없기 때문에 `UPDATE`와 `DELETE`는 사용할 수 없다. 같은 이유로 `INSERT`도 비어 있는 테이블에 입력하거나, 이미 입력된 데이터를 덮어 쓰는 경우에만 사용 가능하다. 그래서 하이브QL은 `"INSERT OVERWRITE"`라는 키워드를 사용하게 된다.
2. SQL은 어떠한 절에서도 서브쿼리를 사용할 수 있지만 하이브QL은 `FROM`절에서만 서브 쿼리를 사용할 수 있다.
3. SQL의 뷰는 업데이트할 수 있고, 구체화된 뷰 또는 비구체화된 뷰를 지원한다. 하지만 하이브QL의 뷰는 읽기 전용이며, 비 구체화된 뷰만 지원한다.
4. `SELECT` 문을 사용할 때 `HAVING` 절을 사용할 수 없다.
5. 저장 프로시저(sotred procedure)를 지원하지 않는다. 대신 맵리듀스 스크립트를 실행할 수 있다.

##### 여기서는 미국 항공 운항 지연 데이터를 분석하기 위한 하이브QL 쿼리문을 작성하겠다. 참고로 하이브QL은 대소문자를 구분하지 않는다.
<br />

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

<br />
<br />

## 2. 데이터 업로드
##### 여기서는 앞서 생성한 airline_delay 테이블에 데이터를 업로드하겠다. 하이브는 로컬 파일 시스템에 있는 데이터와 HDFS에 저장된 데이터를 모두 업로드할 수 있다. 여기서는 로컬 파일 시스템에 있는 파일을 업로드하겠다.

##### 하이브 CLI에서 다음과 같이 `LOAD DATA`를 입력한다. `OVERWRITE INTO`절은 중복된 데이터가 있어도 무시하고 입력한다는 의미다. 그리고 `PARTITION`절은 파티션 키인 `delayYear`값을 2008로 설정해 데이터를 입력하는 설정이다. 참고로 테이블에는 파티션을 설정했는데, 테이블을 등록할 때 `PARTITION` 절을 선언하지 않으면 `LOAD DATA` 실행 시 오류가 발생한다.

```
hive> LOAD DATA LOCAL INPATH '/home/hkl/airlineData/2008.csv'
    > OVERWRITE INTO TABLE airline_delay
    > PARTITION (delayYear='2008');
Loading data to table default.airline_delay partition (delayyear=2008)
Partition default.airline_delay{delayyear=2008} stats: [numFiles=1, numRows=0, totalSize=689413044, rawDataSize=0]
OK
Time taken: 63.498 seconds
```

##### `LOAD DATA`가 실행되면 아래와 같은 `SELECT` 쿼리문을 실행해 데이터가 정상적으로 등록댔는지 확인한다. `SELECT` 절의 기본 문법은 RDBMS의 SQL 문법과 유사하며, MySQL처럼 `LIMIT`을 이용해 상위 10개의 데이터만 조회가능하다.

```
hive> SELECT year, month, deptime, arrtime, uniquecarrier, flightnum
    > FROM airline_delay
    > WHERE delayYear = '2008'
    > LIMIT 10;
OK
year	month	deptime	arrtime	uniquecarrier	flightnum
2008	1	2003	2211	WN	335
2008	1	754	1002	WN	3231
2008	1	628	804	WN	448
2008	1	926	1054	WN	1746
2008	1	1829	1959	WN	3920
2008	1	1940	2121	WN	378
2008	1	1937	2037	WN	509
2008	1	1039	1132	WN	535
2008	1	617	652	WN	11
2008	1	1620	1639	WN	810
Time taken: 0.937 seconds, Fetched: 10 row(s)
```
<br />

## 3. 집계 함수
##### 이번에는 하이브가 지원하는 다양한 집계 함수를 이용해 쿼리문을 작성하겠다. 아래는 하이브에서 지원하는 집계 함수를 정리한 것이다.

| 함수 | 내용 |
| --- | --- |
| COUNT(1), COUNT(*) | 전체 데이터 건수를 반환한다. |
| COUNT(DISTINCT 칼럼) | 유일한 칼럼값의 건수를 반환한다. |
| SUM(칼럼) | 칼럼값의 합계를 반환한다. |
| SUM(DISTINCT 칼럼) | 유일한 칼럼값의 합계를 반환한다. |
| AVG(칼럼) | 칼럼값의 평균을 반환한다. |
| AVG(DISTINCT 칼럼) | 유일한 칼럼값의 평균을 반환한다. |
| MAX(칼럼) | 칼럼의 최댓값을 반환한다. |
| MIN(칼럼) | 칼럼의 최솟값을 반환한다. |

##### 내장 집계 함수 중 건수를 구하는 COUNT 함수를 이용한 쿼리문을 작성해 보겠다. 아래 쿼리문은 미국 항공 운항 지연 데이터 가운데 2008년도의 지연 건수를 조회하는 쿼리문이다.

```
SELECT COUNT(1)
FROM airline_delay
WHERE delayYear = 2008;
```

##### 하이브는 사용자가 입력한 `하이브QL`을 분석한 후, 맵리듀스 잡으로 생성해서 실행한다. 로그 상단에 이 쿼리문은 하나의 잡으로 구성되고 리듀서는 필요 없다는 메시지가 출력된다. 매퍼와 리듀서의 수행 단계가 퍼센트로 표시되며, 작업이 완료되면 몇 개의 맵 태스크와 리듀스 태스크가 실행됐고 HDFS와 CPU자원은 얼마나 소모했는지 표시된다. 위 질의의 경우 3개의 맵 태스크와 1개의 리듀스 태스크를 실행한 후, 질의 결과로 7009728을 출력한다.

##### 하이브 쿼리는 SQL문의 `GROUP BY` 기능도 지원한다. `GROUP BY`를 써서 연도와 월별로 도착 지연 건수를 조회해 보겠다.
##### 아래는 미국 항공 운항 지연 데이터 가운데 2008년도의 도착 지연 건수를 조회하는 쿼리문이다.

```
SELECT year, month, COUNT(*) AS arrive_delay_count
FROM airline_delay
WHERE delayYear = 2008
AND ArrDelay > 0
GROUP BY year, month;
```
##### 쿼리문을 실행하면 다음과 같이 2008년 1월부터 12월까지의 도착 지연 건수가 출력된다.

```
year	month	arrive_delay_count
2008	2	278902
2008	5	254673
2008	8	239737
2008	11	181506
2008	3	294556
2008	6	295897
2008	9	169959
2008	12	280493
2008	1	279427
2008	4	256142
2008	7	264630
2008	10	183582
```
<br />

##### 이번에는 AVG 함수를 이용해 평균 지연 시간을 산출하겠다.
##### 아래 쿼리문은 2008년도의 평균 지연 시간을 연도와 월별로 계산하는 쿼리문이다.

```
SELECT year,month, AVG(ArrDelay) AS avg_arrive_delay_time, AVG(DepDelay) AS avg_departure_delay_time
FROM airline_delay
WHERE delayYear = 2008
AND ArrDelay > 0
GROUP BY year, month;
```

##### 쿼리문을 실행하면 `DOUBLE`형태로 평균값이 출력된다. 최소 10분에서 길게는 40분까지 도착 시간이 지연된 것을 확인할 수 있다.
<br />

##### 하이브는 집계 함수 외에도 다양한 내장 함수를 지원한다. 아래 표는 하이브의 주요 내장 함수를 정리한 것이다.
| 함수 | 내용 |
| --- | --- |
| concat(string a, string b, ...) | 문자열a 뒤에 문자열b를 붙여서 반환한다.<br />예를 들어, concat("facebook", "hive")를 수행하면 facebookhive를 반환한다. |
| substr(string str, int start) | 문자열 str의 start인덱스에서 문자열의 마지막 인덱스까지 잘라낸 문자열을 반환한다.<br />예를 들어, substr("hadoop",4)는 "oop"를 반환한다. |
| substr(string str, int start, int length) | 문자열 str의 start인덱스에서 설정한 length만큼을 잘라낸 문자열을 반환한다.<br />예를 들어, substr("hadoop",4,2)라고 하면 "oo"를 반환한다. |
| upper(string str) | 문자열 str를 대문자로 변환해서 반환한다.<br />예를 들어, upper("hive")는 "HIVE"를 반환한다. |
| ucase(string str) | upper 함수와 동일하다. |
| lower(string str) | 문자열 str를 소문자로 변환해서 반환한다.<br />예를 들어, lower("HIVE")는 "hive"를 반환한다. |
| lcase(string str) | lower 함수와 동일하다. |
| trim(string str) | 문자열 str의 양쪽에 있는 공백을 제거한다.<br />예를 들어, trim(" hive ")는 "hive"를 반환한다. |
| ltrim(string str) | 문자열 str의 왼쪽에 있는 공백을 제거한다.<br />예를 들어, ltrim(" hive")는 "hive"를 반환한다. |
| rtrim(string str) | 문자열 str의 오른쪽에 있는 공백을 제거한다.<br />예를 들어, rtrim("hive ")는 "hive"를 반환한다. |
| regexp_replace(string str, string regex, string replacement) | 문자열 str에서 정규식 표현식 regex와 일치하는 모든 문자열을 replacement로 변경해서 반환한다.<br />예를 들어, regexp_replace("hive","iv", "")는 "he"를 반환한다. |
| from_unixtime(int unixtime) | 유닉스 시간 문자열(1970-01-01 00:00:00 UTC)을 현재 시스템의 시간대로 변경해서 반환한다. |
| to_date(string timestamp) | 타임스탬프 문자열에서 날짜값만 반환한다.<br />예를 들어, to_date("2012_09_01 00:00:00")은 "2012-09-01"을 반환한다. |
| round(double a) | double 값 a에 대한 반올림 정수값(BIGINT)을 반환한다. |
| floor(double a) | double 값 a보다 작거나 같은 최대 정수값(BIGINT)을 반환한다. |
| ceil(double a) | double 값 a보다 크거나 같은 최소한의 정수값(BIGINT)을 반환한다. |
| rand(), rand(int seed) | 랜덤값을 반환한다. `seed` 파라미터로 랜덤값의 범위를 설정할 수 있다. |
| year(string date) | 날짜 혹은 타임스탬프 문자열에서 연도만 반환한다.<br />예를 들어, year("2012-09-01 00:00:00")은 "2012"를 반환한다. |
| month(string date) | 날짜 혹은 타임스탬프 문자열에서 월만 반환한다. <br />예를 들어, month("2012-09-01 00:00:00")은 "09"를 반환한다. |
| day(string date) | 날짜 혹은 타임스탬프 문자열에서 일만 반환한다.<br />예를 들어, day("2012-09-01 00:00:00")은 "01"을 반환한다. |
| get_json_object(string json_string, string path) | 디렉토리 path에서 문자열 json_string으로부터 json 객체를 추출하고 json 문자열로 반환한다. 만약 json이 유효하지 않으면 null 값을 반환한다. |
| size(Map<K.V>) | 맵 타입의 엘리먼트의 개수를 반환한다. |
| size(Array<T>) | 배열 타입의 엘리먼브의 개수를 반환한다. |
| cast(<expr> as<type>) | 정규 표현식 expr을 type으로 타입을 변환한다.<br />예를 들어, cast("100" as BIGINT)는 "100"을 BIGINT로 변환해서 반환한다. 변환에 실패하면 null값을 리턴한다. |
<br />

## 4. 조인
##### 맵리듀스로 조인을 구하려면 수십 줄 이상의 클래스를 작성해야한다.
##### 하지만 하이브를 이용하면 간단하게 조인을 수행할 수 있다. 대신 아래와 같은 제약사항이 있다.

- 하이브는 EQ 조인만 지원한다. EQ 조인이란 두 테이블을 대상으로 동일성을 비교한 후, 그 결과를 기준으로 조인하는 것이다. 이 조인에서는 조인 서술자로 등호(=)만 사용할 수 있다.
- 하이브는 FROM 절에 테이블 하나만 지정할 수 있고, ON 키워드를 사용해 조인을 처리해야 한다.<br />

##### 먼저 데이터를 준비하자.
```
$ cd ~/airlineData
$ wget http://stat-computing.org/dataexpo/2009/carriers.csv
$ wget http://stat-computing.org/dataexpo/2009/airports.csv
```

### 내부 조인(INNER JOIN)
##### 여기서는 항공사 코드데 이터를 저장하기 위한 테이블을 생성한다. 아래와 같이 두 개의 문자열로 구성하며, 필드와 라인의 구분은 airline_delay와 동일하게 설정한다.

```
hive> CREATE TABLE carrier_code(Code STRING, Description STRING)
> ROW FORMAT DELIMITED
> FIELDS TERMINATED BY ","
> LINES TERMINATED BY "\n"
> STORED AS TEXTFILE;
OK
Time taken: 8.324 seconds
```

##### 테이블이 생성되면 다음과 같이 따옴표를 제거한 데이터를 업로드 한다.

```
hive> LOAD DATA LOCAL INPATH "/home/hkl/airlineData/carriers.csv"
    > OVERWRITE INTO TABLE carrier_code;
    Loading data to table default.carrier_code
    Table default.carrier_code stats: [numFiles=1, numRows=0, totalSize=43758, rawDataSize=0]
    OK
    Time taken: 6.822 seconds
```

##### 업로드가 완료되면 코드 테이블에서 10건의 데이터만 조회해보자.

```
hive> SELECT * FROM carrier_code LIMIT 10;
OK
carrier_code.code	carrier_code.description
Code	Description
"02Q"	"Titan Airways"
"04Q"	"Tradewind Aviation"
"05Q"	"Comlux Aviation
"06Q"	"Master Top Linhas Aereas Ltd."
"07Q"	"Flair Airlines Ltd."
"09Q"	"Swift Air
"0BQ"	"DCA"
"0CQ"	"ACM AIR CHARTER GmbH"
"0FQ"	"Maine Aviation Aircraft Charter
Time taken: 0.111 seconds, Fetched: 10 row(s)
```

##### 쿼리를 실행할 때 위와 같이 코드와 설명값이 정상적으로 출력된다. 참고로 첫 번째 줄의 경수 코드값이 일치하는 경우가 없으므로 삭제하지 않아도 된다.<br />

##### 이제 항공 운항 지연 테이블(`airline_delay`)과 항공사 코드 테이블(`carrier_code`)을 조인해보겠다. 아래와 같이 ON 키워드를 사용해 조인을 건다. 이 쿼리문은 두 테이블의 조인키인 `항공사 코드`로 내부 조인을 처리한다. 따라서 두 테이블의 `항공사 코드`가 일치하는 데이터만 조회하게 된다. 참고로 쿼리문을 보면 SQL 처럼 테이블 별칭(alias)을 사용했는데, 하이브QL도 SQL처럼 테이블 별칭을 지원한다. 대신 별칭 앞에 AS 키워드를 추가하면 오류가 발생하므로 사용할 때 주의해야 한다.

```
hive> SELECT A.Year, A.UniqueCarrier, B.Description, COUNT(*)
> FROM airline_delay A
> JOIN carrier_code B ON (A.UniqueCarrier = B.Code)
> WHERE A.ArrDelay > 0
> GROUP BY A.Year, A.UniqueCarrier, B.Description;
```

##### 이번에는 두 개 이상의 테이블을 내부 조인으로 처리해 보겠다. airports.csv 파일을 아래와 같이 모든 따옴표를 제거하고 첫 번째 줄을 삭제한다.

```
$ find .-name airports.csv -exec perl -p -i -e  's/"//g' {} \;
$ sed -e '1d' airports.csv > airports_new.csv
$ mv airports_new.csv airports.csv 
```

##### airports.csv 파일을 하이브에서 처리할 수 있게 다음과 같이 테이블을 생성한다.

```
hive> CREATE TABLE airport_code(Iata String, Airport String, City String, State String, Country String, Lat Double, Longitude Double)
> ROW FORMAT DELIMITED
> FIELDS TERMINATED BY ","
> LINES TERMINATED BY "\n"
> STORED AS TEXTFILE;
OK
Time taken: 2.014 seconds
```

##### 테이블이 생성되면 다음과 같이 airports.csv를 업로드 한다.

```
hive> LOAD DATA LOCAL INPATH "/home/hkl/airlineData/airports.csv" 
> OVERWRITE INTO TABLE airport_code;
Loading data to table default.airport_code
Table default.airport_code stats: [numFiles=1, numRows=0, totalSize=210668, rawDataSize=0]
OK
Time taken: 3.536 seconds
```

##### airport_code 테이블에 공항 데이터 등록이 완료되면 다음과 같이 두 개의 테이블을 조인하는 쿼리를 실행한다.
##### 이 쿼리문은 출발 공항 코드(Origin)와 도착 공항 코드(Dest), airport_code 테이블을 조인해 공항별 지연 건수를 계산하는 쿼리문이다.

```
hive> SELECT A.year, A.origin, B.AirPort, A.dest, C.AirPort, COUNT(*)
> FROM airline_delay A
> JOIN airport_code B ON (A.origin = B.Iata)
> JOIN airport_code C ON (A.dest = C.Iata)
> WHERE A.arrdelay > 0
> GROUP BY A.year, A.origin, B.Airport, A.dest, C.AirPort;

(중략)
2008	XNA	Northwest Arkansas Regional	ATL	William B Hartsfield-Atlanta Intl	709
2008	XNA	Northwest Arkansas Regional	CAK	Akron-Canton Regional	1
2008	XNA	Northwest Arkansas Regional	DFW	Dallas-Fort Worth International	1144
2008	XNA	Northwest Arkansas Regional	DTW	Detroit Metropolitan-Wayne County	107
2008	XNA	Northwest Arkansas Regional	LEX	Blue Grass 	1
2008	XNA	Northwest Arkansas Regional	LGA	LaGuardia	367
2008	XNA	Northwest Arkansas Regional	MEM	Memphis International	397
2008	YAK	Yakutat	CDV	Merle K (Mudhole) Smith	131
2008	YUM	Yuma MCAS-Yuma International	IPL	Imperial County	118
2008	YUM	Yuma MCAS-Yuma International	LAS	McCarran International	29
2008	YUM	Yuma MCAS-Yuma International	LAX	Los Angeles International	398
Time taken: 46.09 seconds, Fetched: 5166 row(s)
```

### 외부 조인(OUTER JOIN)
하이브는 내부 조인 외에 외부 조인도 지원한다. 외부 조인을 테스트하기 위해 항공사 코드에서 일부 항공사 코드를 삭제하겠다. 다음과 같이 로컬에 있는 carrier.csv 파일의 1389번째 줄을 삭제한다.1389번째 줄은 항공사 코드값이 WN인 Southwest Airlines Co 항공사의 데이터다.

```
$ sed -e '1390d' carriers.csv >carriers_new.csv
```

##### 하이브 메타스토어 데이터베이스에는 항공사 코드 테이블을 추가로 생성한다.

```
hive> CREATE TABLE carrier_code2(Code STRING, Description STRING)
> ROW FORMAT DELIMITED
> FIELDS TERMINATED BY ","
> LINES TERMINATED BY "\n"
> STORED AS TEXTFILE;
OK
Time taken: 6.073 seconds
```

##### 추가한 항공사 테이블인 carrier_code2에 WN코드를 삭제한 코드 데이터를 업로드 한다.

```
hive> LOAD DATA LOCAL INPATH "/home/hkl/airlineData/carriers_new.csv"
    > OVERWRITE INTO TABLE carrier_code2;
    Loading data to table default.carrier_code2
    Table default.carrier_code2 stats: [numFiles=1, numRows=0, totalSize=43727, rawDataSize=0]
    OK
    Time taken: 5.946 seconds

```

##### 코드 데이터 등록이 완료되면 다음과 같이 왼쪽 외부 조인 쿼리를 입력한다. 조인할 carrier_code2테이블에 WN코드가 없더라도 airline_delay 테이블에서 WN 코드가 등록된 데이터를 모두 출력한다.

```
hive> SELECT A.year, A.uniquecarrier, B.Code, B.Description
    > FROM airline_delay A
    > LEFT OUTER JOIN carrier_code2 B ON (A.uniqueCarrier = B.Code)
    > WHERE A.uniquecarrier = "WN"
    > LIMIT 10;
(중략)
a.year	a.uniquecarrier	b.code	b.description
2008	WN	NULL	NULL
2008	WN	NULL	NULL
2008	WN	NULL	NULL
2008	WN	NULL	NULL
2008	WN	NULL	NULL
2008	WN	NULL	NULL
2008	WN	NULL	NULL
2008	WN	NULL	NULL
2008	WN	NULL	NULL
2008	WN	NULL	NULL
Time taken: 19.754 seconds, Fetched: 10 row(s)
```

##### airline_delay 테이블에서 항공사 코드가 WN인 데이터를 출력했지만 carrier_code2 테이블에는 `WN`코드 데이터가 존재하지 않아 NULL로 출력됐다. 하지만 이 때 LEFT OUTER JOIN 절 때문에 carrier_code2 테이블에 데이터가 없더라도 airline_delay의 데이터가 조회된 것이다.


## 5. 버킷 활용
##### 버킷은 버킷 칼럼 해시를 기준으로 데이터를 지정된 개수의 파일로 분리해서 저정한다. 버킷은 테이블을 생성할 때 아래와 같은 형태로 선언한다.

```
CLUSTERED BY (칼럼) INTO 버킷 개수 BUCKETS;
```

##### 버킷을 사용하면 쿼리의 성능을 향상시킬 수 있다. 예를 들어, 조인키로 버킷을 생성해 두면 생성된 버킷 중 필요한 버킷만 조회하면 되기 때문에 디렉토리 전체를 풀스캔하는 것 보다 훨씬 빠르게 작업을 처리할 수 있다. 또한 버킷을 이용하면 샘플링을 이용한 다양한 쿼리를 수행할 수 있다.
<br />

##### 그럼 실제로 버킷이 어떠한 구조로 생성되고, 샘플링은 어떻게 하는지 알아보자.
##### 우선 테스트용 테이블을 생성한다. 이 테이블은 UniqueCarrier 칼럼을 대상으로 20 개의 버킷을 생성한다.

```
hive> CREATE TABLE airline_delay2(Year INT, Month INT, UniqueCarrier STRING, ArrDelay INT, DepDelay INT)
	    > CLUSTERED BY (UniqueCarrier) INTO 20 BUCKETS;
	    OK
	    Time taken: 2.972 seconds
```

##### 테이블을 생성했으면 2008년도 항공 운항 지연 데이터를 새로운 테이블에 등록한다.

```
hive> INSERT OVERWRITE TABLE airline_delay2
    > SELECT year, month, uniquecarrier, arrdelay, depdelay
    > FROM airline_delay
    > WHERE delayyear = 2008;
```

##### HDFS의 하이브 웨어하우스 디렉토리를 조회하면 airline_delay2 디렉토리에 20개의 파일이 생성된 것을 확인할 수 있다. 000nn_0인 파일은 모두 버킷을 나타낸다.

```
hive> dfs -ls /user/hive/warehouse/airline_delay2;
```

##### 쿼리문에서 버킷 데이터 샘플을 사용하려면 TABLESAMPLE 절을 이용하면 된다. 다음은 20개의 버킷 중 첫 번째 버킷에서 샘플을 조회하는 쿼리문이다.

```
hive> SELECT uniquecarrier, COUNT(*)
> FROM airline_delay2
> TABLESAMPLE(BUCKET 1 OUT OF 20)
> GROUP BY uniquecarrier;
```

##### 쿼리가 수행되면 다음과 같은 결과가 나오는데, `airline_delay` 조회했을 때와 항공사별 합계 건수가 다르게 나왔다. 이는 파일 전체를 조회하지 않고 첫 번째 버킷만 조회했기 때문이다.

```
uniquecarrier	_c1
AA	204519
B6	67478
UA	193900
Time taken: 5.301 seconds, Fetched: 3 row(s)
```

##### 이런 식으로 버킷을 활용한다면 샘플용 데이터를 조회할 때 크게 도움이 된다. 
##### 버킷을 사용할 때는 버킷이 너무 작은 크기로 만들어지지 않게 주의해야 한다. 하둡에서 너무 작은 파일을 많이 처리하게 되면 부하가 발생하기 때문이다. 그리고 버킷은 사용자가 생각하는 샘플 데이터와 크기가 같거나 작아야만 한다.
