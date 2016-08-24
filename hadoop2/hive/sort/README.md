# 데이터 정렬
##### 여기서는 하이브가 제공하는 데이터 정렬 기능에 대해 알아보겠다.

## 1. ORDER BY
##### 질의문 결과에 대해 전체 정렬을 수행하며, 이를 위해 맵리듀스 잡에서 하나의 리듀서만 실행한다. 그래서 정렬 대상 데이터가 클 경우 성능이 느려지는 단점이 있다.

###사용 예:
```
hive> SELECT * FROM airline_delay
    > ORDER BY year DESC;
```

## 2. SORT BY
##### SORT BY는 전체 정렬을 포기하는 대신 질의 성능을 높이는데 초점을 맞춘다. ORDER BY 절과는 다르게 여러 개의 리듀서를 실행하며, 각 리듀서의 출력 결과를 정렬한다. 하지만 이 방법은 각 리듀서가 같은 키만 받는 것을 보장하지 않기 때문에 리듀서의 출력 결과에 동일한 키가 생성될 수 있다.

###사용 예:
```
hive> SELEC * FROM airline_delay
    > SORT BY year DESC;
```

## 3. DISTRIBUTED BY
##### DISTRIBUTED BY는 같은 키를 가진 레코드가 같은 리듀서로 보내지는 것을 보장한다. 그래서 SORT BY와 함께 사용할 경우 리듀서는 키가 중복되지 않고, 정렬을 수행할 수 있다.

```
hive> SELECT * FROM airline_delay
    > DISTRIBUTED BY year
    > SORT BY year DESC;
```

## 4. CLUSTERED BY
##### CLUSTERED BY는 DISTRIBUTED BY와 SORT BY를 함께 사용하는 것과 동일한 기능을 제공한다. 아래는 바로 위에서 설명한 예제와 동일하다.

```
hive> SELECT * FROM airline_delay
    > CLUSTERED BY year DESC;
```
