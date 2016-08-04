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
