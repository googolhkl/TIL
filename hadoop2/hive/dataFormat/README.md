# 데이터 저장 포맷
##### 하이브의 데이터 저장 포맷은 다음 두 가지로 구분된다.
- 파일 포맷
 * 파일에 레코드를 저장할 때 인코딩되는 방식(텍스트 파일, 시퀀스 파일 등)
- 레코드 포맷
 * 칼럼 값이 레코드 내에서 인코딩되는 방식

## 1. SerDe
##### 하이브는 레코드 내에 인코딩돼 있는 데이터를 처리하기 위한SerDe(Serialize/Deserialize)를 제공한다.
##### 하이브는 `INSERT`나 `CTAS(CREATE TABLE AS SELECT)` 구문을 실행할 때 SerDe를 이용해 레코드의 직렬화(Serialize)를 수행한다. 또한 테이블에 저장된 데이터를 조회할 때는 SerDe를 이용해 역직렬화(Deserialize)를 수행한다. 하이브는 다음과 같은 SerDe를 제공한다.
- LazySimpleSerDe : CSV 파일과 같은 단순 텍스트 파일용. 기본 SerDe로 사용
- LazyBinarySerDe : 텍스트 파일을 바이너리 형태로 저장할 때 사용
- ColumnarSerDe : RC 파일을 텍스트 형태로 저장할 때 사용
- LazyBinaryColumnarSerDe : RC 파일을 바이너리 형태로 저장할 때 사용
- OrcSerde : ORC 파일용 Serde
- RegexSerDe : 정규표현식으로 설정된 칼럼이 포함된 텍스트 데이터를 조회할 때 사용
- ThriftByteStreamTypedSerDe : 쓰리프트 바이너리 데이터용 SerDe
- HBaseSerDe : HBASE 테이블에 데이터를 저장하고 조회할 때 사용
<br />

##### 이번엔 RegexSerDe를 이용해 아파치 웹 로그 조회용 테이블을 만들어 보자.
##### 아래 예제는 테이블 생성 CREATE 질의문을 나타낸다. SerDer는 ROW FORMAT SERDE 옵션으로 설정할 수 있으며, 옵션값은 해당 SERDE의 패키지와 클래스명을 입력하면 된다.

```
hive> CREATE TABLE apache_access_logs(
    > remote_host STRING,
    > remote_logname STRING,
    > remote_userid STRING,
    > finish_time STRING,
    > request STRING,
    > status_code STRING,
    > size STRING,
    > referer STRING,
    > user_agent STRING)
    > ROW FORMAT SERDE "org.apache.hadoop.hive.contrib.serde2.RegexSerDe"
    > WITH SERDEPROPERTIES(
    > "input.regex" = "([^ ]*) ([^ ]*) ([^ ]*) (-|\\[[^\\]]*\\]) ([^ \"]*|\"[^\"]*\") (-|[0-9]*) (-|[0-9]*)(?: ([^ \"]*|\".*\") ([^ \"]*|\".*\"))?",
    > "output.format.string" = "%1$s %2$s %3$s %4$s %5$s %6$s %7$s %8$s %9$s"
    > )
    > STORED AS TEXTFILE;
OK
Time taken: 7.654 seconds
```

##### 하이브에서 위처럼 테이블을 생성한 후 아파치 액세스 로그 파일을 로딩한다. 아파리 로그 파일은 아래 명령어로 다운로드 한다.

```
$ wget https://github.com/googolhkl/TIL/tree/master/hadoop2/hive/source/apache_access.log
```

##### 다운로드 받은 후 데이터를 로딩한다.

```
hive> LOAD DATA LOCAL INPATH "/home/hkl/apache_access.log" OVERWRITE INTO TABLE apache_access_logs;
Loading data to table default.apache_access_logs
Table default.apache_access_logs stats: [numFiles=1, numRows=0, totalSize=33375, rawDataSize=0]
OK
Time taken: 6.135 seconds
```

##### RegexSerDe가 정상적으로 적용됐는지 각 칼럼을 조회하겠다.

```
hive> SELECT remote_host, remote_userid, finish_time, status_code
    > FROM apache_access_logs limit 5;
```

## 2. 파일 포맷
##### 하이브는 다양한 포맷을 지원한다. 데이터의 크기 및 성능을 고려해 적절한 파일 포맷을 설정해야 한다. 아래는 하이브가 지원하는 파일 포맷을 정리한 것이다.

| 항목 | 텍스트 파일 | 시퀀스 파일 | RC 파일 | ORC 파일 | 파케이 |
| --- | --- | --- | --- | --- | --- |
| 저장 기반 | 로우 기반 | 로우 기반 | 칼럼 기반 | 칼럼 기반 | 칼럼 기반 |
| 압축 | 파일 압축 | 레코드/블록 압축 | 블록 압축 | 블록 압축 | 블록 압축 |
| 스플릿 지원 | 지원 | 지원 | 지원 | 지원 | 지원 |
|압축 적용시 스픨릿 지원 | 미지원 | 지원 | 지원 | 지원 | 지원 |
| 하이브 키워드 | TEXTFILE | SEQUENCEFILE | RCFILE | ORCFILE | PARQUET |

##### 테이블의 파일 포맷은 CREATE의 STORED AS 옵션으로 설정할 수 있다. 참고로 하이브는 텍스트 파일을 기본 포맷으로 사용한다.

```
// 텍스트 파일 SerDe
hive> CREATE TABLE table1 (id INT,name STRING, score FLOAT, type STRING)
    > ROW FORMAT SERDE "org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe"
    > STORED AS SEQUENCEFILE

// 바이너리 SerDe
hive> CREATE TABLE table1 (id INT,name STRING, score FLOAT, type STRING)
    > ROW FORMAT SERDE "org.apache.hadoop.hive.serde2.lazybinary.LazyBinarySerde"
    > STORED AS SEQUENCEFILE;
```

### RC 파일
##### RC(Record_Columar) 파일은 칼럼 기반의 파일 포맷이며, 로우 그룹 단위로 레코드를 관리 한다.

##### RC 파일은 칼럼 개수가 많은 테이블에 유리하다. 로우 기반의 테이블은 특정 칼럼만 조회할 경우에도 내부적으로 해당 레코드의 전체 칼럼을 조회하게 된다. 왜냐하면 각 레코드에 모든 칼럼이 저장돼 있기 때문이다. 하지만 RC 파일에서는 데이터가 각 칼럼별로 분리되어 저장돼 있기 때문에 필요한 칼럼만 조회한다. 그래서 데이터 조회 속도가 `로우 기반 파일보다 더 뛰어나다.` 또한 RC 파일은 칼럼별로 압축하기 때문에 해당 칼럼이 필요한 시점에만 압축을 해제한다.

##### 아래는 SerDe별 RC 파일 테이블 생성 구문이다.

```
// 텍스트 파일 SerDe
hive> CREATE TABLE table1 (id INT,name STRING, score FLOAT, type STRING)
    > ROW FORMAT SERDE "org.apache.hadoop.hive.serde2.columnar.ColumnarSerDe"
    > STORED AS RCFILE;

// 바이너리 SerDe
hive> CREATE TABLE table1 (id INT,name STRING, score FLOAT, type STRING)
    > ROW FORMAT SERDE "org.apache.hadoop.hive.serde2.columnar.LazyBinaryColumnarSerDe"
    > STORED AS RCFILE;
```

### ORC 파일
##### RC 파일은 칼럼 타입에 영향을 받으며, 한 번에 하나의 레코드에만 SerDe를 적용할 수 있다. 그래서 데이터 타입별 효과적인 압축 방식을 적용할 수 없다. 또한 RC 파일은 Map, List와 같은 데이터 타입의 특벙 값을 조회할 때도 비효율적이다. 왜냐하면 RC 파일은 순차적인 레코드 접근을 위해 설계돼 있어서 Map과 List 내의 불필요한 데이터도 함께 조회하기 때문이다.

##### ORC(Optimized Record-Columnar) 파일은 이러한 RC 파일의 단점을 개선한 파일 포맷이다.
##### ORC 파일은 다음과 같은 특정이 있다.
- 하나의 파일에 칼럼을 JSON처럼 중첩(nested) 구조로 저장할 수 있다.
- Map, Struct, List등을 칼럼값 대신 사용할 수 있다.
- 네임노드의 부하를 줄이기 위해 하나의 태스크는 하나의 출력 파일만 생성하게 한다.
- 빠른 조회를 위해 파일 내에 경량 인덱스를 저장한다.
- 필터 조건에 해당되지 않는 로우 그룹을 제외함으로써 빠른 스캔이 가능하다.
- 파일을 읽고 저장할 때 일정량의 메모리만 필요하다.

##### ORC 파일은 하이브 0.12.0 부터 추가됐고, 특정 형태의 질의의 경우 기존 파일 포맷보다 높은 성능 향상을 보여준다. 하지만 기존 파일에 대비해 ORC 파일을 생성하는데 상당히 많은 시간이 소요된다는 단점이 있다.

##### 아래는 ORC 파일 생성 구문이다.

```
hive> CREATE TABLE table1 (id INT,name STRING, score FLOAT, type STRING)
    > STORED AS ORC;
```

### 파케이
##### ORC 파일은 중첩 구조의 칼럼 저장 방식과 높은 압축율이라는 뛰어난 장점이 있지만, 하이브 외에 다른 플랫폼에서는 사용할 수 없다. 파케이 파일 포맷은 ORC 파일 처럼 중첩 구조로 칼럼을 저장할 수 있다. 그래서 SNS에 댓글을 다는 것처럼 칼럼을 계층적으로 늘려갈 수 있다. 또한 파케이는 하이브와는 달리 범용적으로 사용 가능하며, 현재 하이브, 피그, 타조, 임팔라 등 다양한 플랫폼에 적용돼 있다. 파케이는 하나의 파일에 여러 칼럼을 저장하기 때문에 조인 작업을 최소화할 수 있으며, 다양한 압축과 인코딩 방식을 적용할수 수 있다.
##### 아래는 파케이 테이블 생성 구문이다.

```
hive> CREATE parquet_test(
	id INT,
	str STRING,
	mp MAP<STRING,STRING>,
	lst ARRAY<STRING>,
	strct STRUCT<A:STRING,B:STRING>
	)
    > PARTITIONED BY (part string)
    > STORED AS PARQUET;
```
