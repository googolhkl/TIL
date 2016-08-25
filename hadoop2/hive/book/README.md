# 하둡 프로그래밍



## 소개
##### 여기서 설명하는 예제는 `하이브 완벽가이드` 책을 대부분 참고했다.
##### `Programming Hive(O'Relilly), Copyright 2012, Edward Capriolo, Dean Wampler, Jason Rutherglen, 978-1-449-31933-5`

## 자바와 하이브 비교
##### 이전 하둡 예제에서 WordCount예제를 한 적이 있다.
##### 간단한 예제인데도 코드가 꽤 많은 양이였다. 아래는 하이브로 WordCount를 구현한 예제다.
```
CREATE TABLE docs (line STRING);

LOAD DATA INPATH "docs" OVERWRITE INTO TABLE docs;

CREATE TABLE word_counts AS
SELECT word, count(1) AS count FROM (SELECT explode(split(line, '\s')) AS word FROM docs) w
GROUP BY word
ORDER BY word;
```
##### 자바 API로 구현할 때는 세세한 부분까지 커스터마이징 할 수 있다는 장점이 있다.
##### 하이브로 구현할 땐 쉽고 빠르게 구현할 수 있다는 장점이 있다.



## 목차
- [시작하기](https://github.com/googolhkl/TIL/tree/master/hadoop2/hive/book/1_start)
