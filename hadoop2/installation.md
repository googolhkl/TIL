# 하둡 설치하기
### 서버는 googolhkl1, googolhkl2, googolhkl3, googolhkl4로 가정한다.
### 서버의 모든 사용자는 hkl로 가정한다.

### 1. 호스트 파일 수정

##### 하둡은 서버 간에 ssh를 이용하는데 ssh는 다른 서버로 접근할 때 IP혹은 호스트명으로 접속한다.
##### 호스트 파일을 수정해준다.

`$ ifconfig`

##### 여기서 나온 아이피를 아래에 적어준다.
 
```
$ sudo vi /etc/hosts
127.0.0.1 		localhost
111.111.22.101	googolhkl1
111.111.22.102	googolhkl2
111.111.22.103	googolhkl3
111.111.22.104	googolhkl4
```

##### 127.0.1.1은 삭제한다. 
##### 이렇게 모든 서버에 반복해준다.

### 2. JAVA 설치

```
$ sudo add-apt-repository ppa:webupd8team/java
$ sudo apt-get update
$ sudo apt-get install oracle-java8-installer
$ java -version
```


