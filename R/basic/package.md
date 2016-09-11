# 패키지
## 1. 패키지 설치방법
##### R이 좋은 이유는 기능을 무한 확장할 수 있기 때문이다. R은 기본 함수 외 분석에 필요한 패키지를 설치해 다양하게 사용할 수 있다. 패키지 종류는 엄청나게 많다. 테스트 하기 위해 3D 그래프를 보여주는 패키지인 rgl패키지늘 설치해보겠다. rgl 패키기는 3D plot을 구현해 주는 패키지다. 아래와 같이 입력하자.

```
> install.packages("rgl")
URL 'https://cran.rstudio.com/bin/macosx/mavericks/contrib/3.3/rgl_0.95.1441.tgz'을 시도합니다
Content type 'application/x-gzip' length 4127595 bytes (3.9 MB)
==================================================
downloaded 3.9 MB


The downloaded binary packages are in
    /var/folders/vc/9vdnz5p949b3mzwpvvd5bwsm0000gn/T//RtmpEiYXfu/downloaded_packages
>
```
##### 위와 같이  error메시지가 없고 `>`가 나타나면 설치 성공이다.
<br />



## 2. 설치된 패키지 사용하기
##### 설치된 패키지를 사용하기 위해서는 `로드`해야 한다. `library('rgl')를 Console 창에 입력하고 실행해보자. `require('rgl')`라고 입력해도 된다.
##### library()와 require()의 차이는 함수 호출 후 Boolean값을 돌려주느냐의 차이다. require()는 Boolean값이 필요할 때 사용한다.

```
> library("rgl")
```
<br />



## 3. 예제 실행해보기
##### 방금 설치한 패키지 안에 있는 plot3d() 함수의 예제코드를 실행시켜 보자.
##### 예제코드를 바로 실행하는 함수는 example()이다. 이 함수 안에 plot3d 함수명을 넣으면 plot3d()함수의 예제코드가 바로 실행된다.
```
> example(plot3d)
```
##### 만약 패키지에 버전이 업그레이드 되면 `update.packages("패키지명")`을 실행하면 된다.
- 패키지 설치 : install.packages("패키지명")
- 패키지 로드 : library("패키지명") 또는 require("패키지명")
- 패키지 업그레이드 : update.packages("패키지명")
<br />



## 4. 예제를 위한 패키지들
```
> install.packages("rgl")
> install.packages("ggplot2")
> install.packages("ggthemes")
> install.packages("data.table")
> install.packages("devtools")
> install.packages("dplyr")
> install.packages("plyr")
> install.packages("reshape2")
> install.packages("scales")
> install.packages("stringr")
```
<br />



## 5. 설치된 패키지 목록보기
- library()
- installed.packages()

##### installed.packages는 library보다 더 자세한 정보를 준다.

```
> installed.packages()[,c("Package","Version")]
```
