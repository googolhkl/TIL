# R에서 외부테이터 읽어들이기
##### 외부에서 테이블로 정리한 데이터를 R로 가져오면 `데이터프레임` 객체에 담긴다. `데이터프레임`은 테이블이라고 생각하면 된다.
##### 지금부터 외부에서 데이터를 가져오는 방법을 살펴볼 건데, 자신에게 필요한 방법은 꼭 익히기 바란다. 모두 볼 필요는 없고 필요한 것만 보자.

## 1. csv 포맷 읽어들이기
##### 데이터를 가져오는 가장 좋은 방법은 csv파일을 가져오는 것이다. 엑셀이나 기타 스프레드시트 프로그램은 대부분 csv파일로 저장이 가능하다.
##### 방법은 간단하다. read.csv() 함수를 이용하면 된다. `실행한 RData가 있다면 같은 디렉토리에 csv파일이 있어야 하고, `RStudio`를 바로 실행했다면 작업 디렉토리에 csv파일이 있어야 한다.

```
> List <- read.csv("/Users/googolhkl/Downloads/test.csv")
```
##### 한가지 유의해야 할 점은 칼럼 이름이 담겨있는 셀(보통 첫 번째 라인)은 변수행의 이름으로 인식하도록 기본값이 설정되어있다. 만약 csv파일에 변수행의 이름이 없다면 read.csv() 함수의 header인자를 FALSE로 설정해줘야 한다.

```
> List <- read.csv("/Users/googolhkl/Downloads/test.csv", header=F)
```
<br />



## 2. txt 포맷 읽어들이기
##### read.table()을 이용하면 .txt파일을 읽을 수 있다.

```
> List <- read.table("/Users/googolhkl/Downloads/test.txt")
> List
      V1   V2   V3
1   이름 성별 나이
2 이경훈   남   26
3 유재석   남    ?
```

##### 근데 첫 번째 행이 이상하다. read.table()은 read.csv()와 다르게 header인자의 기본값이 FALSE로 되어있다. 그래서 파일에 변수열 이름이 있다면 `header=T` 인자를 반드시 입력해야 한다. 

```
> List <- read.table("/Users/googolhkl/Downloads/test.txt", header=T)
> List
    이름 성별 나이
1 이경훈   남   26
2 유재석   남    ?
```

##### read.table() 함수는 탭을 기본 구분기호로 인식하기 때문에 파일에서 탭으로 값들이 구분되어 있어야 한다.
##### 하지만 다른 구분으로도 값을 인식할 수 있다. 만약 값들의 구분을 `;`로 되어있다면 아래와 같이 하면 된다.

```
> List <- read.table("/Users/googolhkl/Downloads/test.txt", header=T, sep=";")
> List
    이름 성별 나이
1 이경훈   남   26
2 유재석   남    ?
```
<br />



## 3. XML 포맷 읽어들이기
##### XML 파일을 불러오기 위해서는 먼저 XML 패키지를 설채해야 한다. 그리고 사용하기 위해 해당 패키지를 불러온다.

```
> install.packages("XML")
> library("XML")
```

##### xml파일을 불러외 위해서는 XML패키지에 있는 xmlTreeParse()를 이용한다.
```
> DocFromXML <- xmlTreeParse("score.xml", useInternal=T)
```
##### 필요없는 태그는 빼고 필요한 컨텐츠만 가져오기 위해 xmlRoot()를 사용한다.
```
> RootNode <- xmlRoot(DocFromXML)
> RootNode
<mathscore>
 <student>
  <name>Peter</name>
  <score>100</score>
 </student>
 <student>
  <name>Abel</name>
  <score>90</score>
 </student>
 <student>
  <name>Elin</name>
  <score>80</score>
 </student>
</mathscore>
```
##### XML 트리 구조를 그대로 불러왔다. 여기서 `name`과 `score`값을 가져오는 방법을 살펴보자.

```
> Names <- xpathSApply(RootNode, "//name", xmlValue)
> Names
[1] "Peter" "Abel" "Elin"
```

##### 이름값을 불러왔으니 점수를 가져오자.
```
> Scores <- xpathSApply(RootNode, "//score", xmlValue)
> Scores
[1] "100" "90" "80"
```

#####  이 두 값을 테이블로 만들기 위해 data.frame()을 이용해보자.

```
> MathScore <- data.frame(NAME=Names, SCORE=Scores)
> MathScore
    NAME    SCORE
1   Peter   100
2   Abel    90
3   Elin    80
