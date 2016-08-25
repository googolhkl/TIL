## 시작하기
### 하둡과 하이브 설치하기
##### [하둡 설치하기](https://github.com/googolhkl/TIL/tree/master/hadoop2/installation)과 [하이브 설치하기](https://github.com/googolhkl/TIL/tree/master/hadoop2/hive/installation)를 설치하자.



### 하이브 명령어



#### 1.CLI 옵션
##### 다음 명령어를 실행하면 CLI의 옵션 목록을 볼 수 있다.
```
$ hive --service cli --help
usage: hive
 -d,--define <key=value>          Variable subsitution to apply to hive
                                  commands. e.g. -d A=B or --define A=B
    --database <databasename>     Specify the database to use
 -e <quoted-query-string>         SQL from command line
 -f <filename>                    SQL from files
 -H,--help                        Print help information
    --hiveconf <property=value>   Use value for given property
    --hivevar <key=value>         Variable subsitution to apply to hive
                                  commands. e.g. --hivevar A=B
 -i <filename>                    Initialization SQL file
 -S,--silent                      Silent mode in interactive shell
 -v,--verbose                     Verbose mode (echo executed SQL to the
                                  console)
```
##### 더 간단히 실행하려면 `hive -아무문자`를 입력하면 된다.



#### 2.변수와 속성
##### --define key=value 옵션은 --hivevar key=value 옵션과 동일하게 동작한다. 이 두 옵션은 하이브 스크립트에서 사용자 정의 변수를 명령행에서 정의할 수 있다. 이 옵션을 사용하면 하이브는 hivevar 네임스페이스에 키-값을 저장하는데, 이는 내장된 세 가지 네임스페이스인 `hiveconf`, `system`, `env`에서 정의한 것과 구분할 수 있도록 해준다.
##### 아래는 네임스페이스 옵션을 정리한 것이다.

| 네임스페이스 | 접근 | 설명 |
| --- | --- | --- |
| hivevar | 읽기/쓰기 | 사용자 정의 변수 |
| hiveconf | 읽기/쓰기 | 하이브만의 설정 속성 |
| system | 읽기/쓰기 | 자바가 정의한 설정 속성 |
| env | 읽기 | 쉘 환경에서 정의한 환경 변수 |

##### CLI 내부에서는 SET 명령을 사용해서 변수를 나타내고 변경한다.


#### 3.하이브 원 샷 명령
##### -e 명령은 하나 또는 세미콜론으로 구분된 여러 개의 쿼리를 수행한 후에 하이브 CLI를 곧바로 빠져나오게 하는 명령어다.

```
$ hive -e "SELECT * FROM airline_delay LIMIT 5";
```

##### 쿼리 결과를 바로 파일에 담을 필요가 있을 때가 있다. -S를 명령에 추가시키면 `OK, Time taken ...`과 불필요한 출력을 없앨 수 있는데 아래 예제를 보자.

```
$ hive -S -e "SELECT * FROM airline_delay LIMIT 5" > result
```

##### 주의할 점은 `HDFS`가 아니라 로컬 파일시스템으로 리다이렉트하는 것이다.



#### 4. 파일로 하이브 쿼리 실행하기
##### 하이브는 -f 파일 옵션으로 파일에 저장된 하나 또는 그 이상의 쿼리를 실행할 수 있다. 일반적으로 하이브 쿼리가 저장된 파일은 .q 또는 .hql확장자를 사용한다. 

```
$ cat test.hql 
SELECT * FROM airline_delay LIMIT 3;

$ hive -f test.hql 
```

##### 만약 하이브 쉘에서 실행시키고 싶다면 다음과 같이 실행한다.
```
hive> source test.hql;
```



#### 5. .hiverc 파일
##### $HOME/.hiverc 파일에 시스템 속성을 넣어두면 편리하다.
##### 아래는 CLI 프롬프트를 현재 작업 중인 데이터베이스 이름으로 변경하고, 하이브가 되도록이면 로컬 모드로 많이 실행되도록 하는 것이다.

```
set hive.cli.print.current.db=true;
set hive.exec.mode.local.auto=true;
```




#### 6.명령 히스토리
##### $HOME/.hivehistory 파일에 마지막 10,000개의 입력했던 명령어를 저장한다.



#### 7. 쉘 실행
##### 간단한 배시 쉘 명령을 수행하기 위해 하이브 CLI를 빠져나갈 필요가 없다. 간편하기 ! 뒤에 명령을 입력하고 줄 마지막에 세미콜론으로 끝내면 된다!
```
hive> ! ls;
```




#### 8. 하이브에서 하둡 dfs 명령 수행하기
##### 하이브 CLI hrjcnt hadoop dfs ... 명령을 수행할 수 있다. 
```
hive> dfs -ls;
Found 8 items
drwxr-xr-x   - hkl supergroup          0 2016-08-08 14:38 input
drwxr-xr-x   - hkl supergroup          0 2016-08-09 11:50 input2
drwxr-xr-x   - hkl supergroup          0 2016-08-08 15:35 meta
drwxr-xr-x   - hkl supergroup          0 2016-08-08 15:10 movie_out
drwxr-xr-x   - hkl supergroup          0 2016-08-09 12:21 movie_out2
drwxr-xr-x   - hkl supergroup          0 2016-08-18 15:45 movie_out3
drwxr-xr-x   - hkl supergroup          0 2016-08-19 14:12 movie_result
drwxr-xr-x   - hkl supergroup          0 2016-08-19 14:11 movie_sequence
```



#### 9. 하이브 스크립트에서 주석 달기
##### 하이브 0.8.0 버전에서부터 문자열 --로 시작하는 주석을 하이브 스크립트에 넣을 수 있다.
```
-- 3개의 데이터 확인하기
SELECT * FROM airline_delay LIMIT 3;
```



#### 10. 쿼리 컬럼 헤더
##### 항상 헤더를 보고 싶을 때 아래 예제의 첫 번째 줄을 $HOME/.hiverc 파일에 넣으면 된다.

```
hive> set hive.cli.print.header=true;
hive> SELECT * FROM airline_delay LIMIT 3;
```
