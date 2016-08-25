# 궁금한 것이 있을 때
## 1. 검색 방법 추천
##### [http://rseek.org](http://rseek.org)를 활용해 보자. 구글링을 할 때 R이라는 한 글자 때문에 검색이 어려울 때 좋다.
<br />



## 2. Reference 문서를 참고한다.
##### [http://cran.r-project.org/manuals.html](http://cran.r-project.org/manuals.html)에 가면 reference를 볼 수 있다. 함수의 설명, 사용방법, 인자에 대한 설명, 주의할 점, 예제 등이 있다.
<br />



## 3. R에서 도움말 직접 보기
##### Reference 문서를 찾지 않아도 함수에 대해 도움말을 쉽게 볼 수 있다. help() 함수를 이용하면 된다.

```
> help("rep")
```


## 4. help() 함수로 도움말이 안나올 때
##### 설치한 패키지를 로드하지 않아서 그렇다. library("패키지명")으로 로드하고 다시 help()를 해보자.
##### 로드하지 않고 볼 수 있는데 다음과 같이 하면 된다.
```
> ?ggplot2::gplot
```
