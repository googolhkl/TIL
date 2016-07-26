# 메이븐 설치
##### 빌드관리 도구로 메이븐을 이용하겠다.
##### [메이븐 다운로드 홈페이지](http://maven.apache.org/download.cgi) 에서 메이븐 3.2.3을 설치하거나 다음 명령어로 설치하면 된다.

```
$ wget https://archive.apache.org/dist/maven/maven-3/3.2.3/binaries/apache-maven-3.2.3-bin.tar.gz
```

##### 받은 파일을 /usr/local 디렉터리로 이동시켜 주고 압축을 풀어주자.

```
$ sudo mv apache-maven-3.2.3-bin.tar.gz /usr/local
$ cd /usr/local
$ sudo tar xvfz apache-maven-3.2.3-bin.tar.gz
```

##### 압축이 풀린 디렉터리를 리눅스 프로파일(/etc/profile)에 추가한다.

```
export PATH=$PATH:/usr/local/apache-maven-3.2.3/bin
```


