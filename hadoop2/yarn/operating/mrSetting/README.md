# 맵리듀스 설정
##### 여기선 얀 클러스터에서 맵리듀스를 실행하기 위한 옵션을 알아보겠다.
<br />

## 보조서비스 설정
##### 얀 클러스터에서 맵리듀스를 사용하려면 반드시 보조서비스를 설정해야 한다. 이전에도 설명했지만 다시 설명을 하겠다. 다음과 같이 yarn-site.xml에 설정을 한다.

```xml
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>
 <property>
  <name>yarn.nodemanager.aux-services</name>
  <value>mapreduce_shuffle</value>
 </property>
 <property>
  <name>yarn.nodemanager.aux-services.mapreduce_shuffle.class</name>
  <value>org.apache.hadoop.mapred.ShuffleHandler</value>
 </property>
</configuration>
```
<br />

## 메모리 설정
##### 리소스매니저는 노드매니저가 실행되는 서버의 CPU 및 메모리와 같은 리소스를 바탕으로 컨테이너를 실행한다. 이때 특정 리소스에 작업이 집중될 경우 전체 클러스터의 성능이 저하될 수 있다. 그래서 각 리소스가 균형 있게 작업을 처리할 수 있도록 설정해야 한다. 일반적으로 디스크와 CPU당 컨테이너 개수를 1~2개로 설정하는 것을 권한다. 예를 들어, 다음과 같은 사양의 서버에 대해서는 최대 20개의 컨테이너를 실행할 수 있다. 이번엔 다음과 같은 서버 사양을 기분으로 얀 클러스터의 메모리 크기를 설정해보겠다.

- CPU 코어 개수 : 12개
- 메모리 크기 : 48GB
- 디스크 개수 : 12개

##### 첫째, 노드매니저가 컨테이너에게 할당하는 메모리 크기를 설정하겠다. **컨테이너의 메모리 크기는 노드매니저에게 할당된 힙 메모리 크기를 초과하지 않도록 설정해야 한다.** 위 서버의 경우, 8GB는 운영체제 및 다른 소프트웨어에 사용하고, 40GB를 컨테이너에 할당한다. 위와 같은 경우 yarn-site.xml에 다음과 같이 40GB를 MB 단위로 설정한다.

```xml
<property>
 <name>yarn.nodemanager.resource.memory-mb</name>
 <value>40960</value>
</property>
```
##### 그리고 yarn-env.sh에 노드 메모리의 힙 크기를 다음과 같이 42000으로 설정한다. **yarn-nodemanager.resource.memory-mb는 컨테이너의 힙 크기를 초과할 수 없기 때문에 위와 같이 힙 크기를 더 여유있게 설정해야만 한다.**

> **export YARN_NODEMANAGER_HEAPSIZE=42000**

##### 둘째, 리소스매니저가 하나의 컨테이너에 할당할 수 있는 최대, 최소 메모리 크기를 설정한다. 앞서 20개의 컨테이너를 실행한다고 가정했기 때문에 컨테이너 20개는 40GB의 메모리를 나눠서 사용해야 한다. 전체 메모리인 40GB를 컨테이너 개수로 나눌 경우 한 컨테이너는 2GB의 메모리를 사용할 수 있다. ** 이 값은 yarn-site.xml에 yarn.scheduler.maximum-allocation-mb 옵션으로 설정하며, 이 옵션은 리소스매니저가 한 컨테이너에게 할당할 수 있는 최대 메모리 값을 의미한다. 다음은 컨테이너의 최대 메모리 값을 2GB로 설정한 경우다. 또한 최대 메모리값 외에 최소 메모리값도 설정할 수 있으며, 이 옵션은 yarn.scheduler.minimum-allocation-mb를 사용한다. 참고로 여기선 최소 메모리를 512MB로 설정했다. **

```xml
<property>
 <name>yarn.scheduler.minimum-allocation-mb</name>
 <value>512</value>
</property>
<property>
 <name>yarn.scheduler.maximum-allocation-mb</name>
 <value>2048</value>
</property>
```

##### 셋째, 맵리듀스 프레임워크의 메모리를 설정한다. 맵리듀스 애플리케이션의 애플리케이션마스터인 MRAppMaster용 컨테이너 메모리를 **$HADOOP_HOME/etc/hadoop/mapred-site.xml**에 설정한다.

```xml
<property>
 <name>yarn.app.mapreduce.am.resource.mb</name>
 <value>1536</value>
</property>
```

##### 이 속성을 설정할 때 주의할 점은 **리소스매니저가 컨테이너에 할당하는 최대 메모리 설정보다 작아야 한다**는 점이다. 예를 들어, yarn.app.mapreduce.am.resource.mb는 1536MB, yarn.scheduler.maximum-allocation-mb는 1024MB로 설정한 후 맵리듀스 애플리케이션을 실행할 경우 오류가 발생한다.
<br />

##### 이번에는 맵 태스크와 리듀스 태스크가 요청하는 컨테이너의 메모리 크기를 설정한다. 각 설정은 아래 표와 같이 yarn-site.xml에 설정한다. `mapreduce.map.memory.mb` 속성은 맵 태스크가 요청하는 컨테이너 메모리 크기이고, `mapreduce.reduce.memory.mb`는 리듀스 태스크가 요청하는 컨테이너 메모리 크기다.

```xml
<property>
 <name>mapreduce.map.memory.mb</name>
 <value>1024</value>
</property>

<property>
 <name>mapreduce.reduce.memory.mb</name>
 <value>1024</value>
</property>
```
<br />

#####참고로 맵리듀스 애플리케이션이 실행되려면 적어도 3개의 컨테이너가 필요하다. **애플리케이션마스터**용 컨테이너 1개, **맵 태스크**용 컨테이너 1개, **리듀스 태스크**용 컨테이너 1개가 필요하기 때문이다. 그래서 각 컨테이너 메모리 크기의 합이 yarn.nodemanager.resource.memory-mb보다는 작아야 한다.
<br />

#####마지막으로 맵 태스크와 리듀스 태스크의 쓰레드에 할당하는 최대 힙 메모리 크기는 아래와 같이 설정한다. 일반적으로 컨테이너 메모리 크기의 75% 정도로 설정하는 것을 추천한다. mapreduce.map.java.opts는 **맵 태스크 쓰레드**의 최대 힙 메모리 크기, mapreduce.reduce.java.opts는 **리듀스 태스크 쓰레드**의 최대 힙 메모리 크기를 나타낸다.

```xml
<property>
 <name>mapreduce.map.java.opts</name>
 <value>-Xmx768m</value>
</property>

<property>
 <name>mapreduce.reduce.java.opts</name>
 <value>-Xmx768m</value>
```

## 우버 태스크 설정
#####얀 클러스터에서 동작하는 맵리듀스의 기능 중 우버 태스크(Uber Task)라는 기능이 있다. 이 기능은 **리소스가 적게 필요한 맵리듀스 잡을 실행할 때 맵 태스크와 리듀스 태스크를 동일한 JVM에서 실행**하는 기능이다. 새로운 컨테이너를 실행하고 태스크를 할당하는 리소스보다 이미 실행돼 있는 하나의 JVM을 활용하는 것이 더 효과적이라고 보고 우버 태스크를 실행하는 것이다. 우버 태스크는 아래의 조건을 모두 만족해야한 실행된다. 참고로 각 옵션은 **mapred-site.xml**에서 설정할 수 있다.
- mapreduce.job.ubertask.enable 옵션값이 true로 설정돼 있어야 한다.
- 맵리듀스 잡이 실행하는 맵 태스크 개수가 mapreduce.job.ubertask.maxmaps 옵션값보다 작아야 한다.
- 맵리듀스 잡이 실행하는 리듀스 태스크 개수가 mapreduce.jop.ubertask.maxreduces 옵션값보다 작아야 한다.
- 맵리듀스 잡의 입력 데이터의 바이트 크기가 mapreduce.job.ubertask.maxbytes보다 작아야 한다.
- 맵리듀스 잡을 실행하기 위한 메모리 크기가 yarn.app.mapreduce.am.resource.mb 옵션값 이하거나 yarn.app.mapreduce.am.resource.mb 옵션값이 -1로 설정돼 있어야 한다.
- 맵리듀스 잡을 실행하기 위한 CPU 코어 개수가 yarn.app.mapreduce.am.resource.cpu-vcores 옵션값 이하여야 한다.
- 체인잡은 적용할 수 없다.
<br />

## 로그 설정
##### 맵리듀스 애플리케이션의 실행 이력은 얀이 제공하는 기본 웹 페이지(http://호스트:8088)에서 확인할 수 있다. 하지만 얀 클러스터가 종료될 경우 기존의 실행 이력은 더 이상 웹페이지에서 조회할 수 없다. 왜냐하면 얀은 애플리케이션 실행 이력을 메모리에 보관하고 있기 때문이다.
<br />

#####그래서 맵리듀스 잡의 실행 이력을 지속적으로 확인하려면 반드시 잡 히스토리 서버를 실행해야 한다.
- 잡 히스토리 서버 실행 : sbin/mr-jobhistory-daemon.sh start historyserver
- 잡 히스토리 서버 종료 : sibn/mr-jobhistory-daemon.sh stop  historyserver

#####위와 같이 서버를 실행할 경우 웹 페이지(http://호스트:19888)에서 **맵리듀스 잡**의 실행 이력과 해당 잡의 **맵 태스크와 리듀스 태스크** 실행 이력도 함께 확인할 수 있다. 
#####웹 페이지에 접속하면 **logs** 링크가 보이는데 이 링크는 해당 잡의 로그 파일을 출력한다. 하지만 링크를 클릭하면 오류가 출력된다. 그 이유는 로그 취합 옵션이 **false**로 설정돼 있기 때문이다. 정상적으로 확인하려면 **yarn-site.xml**에 다음과 같이 입력한다.

```xml
<property>
 <name>yarn.log-aggregation-enable</name>
 <value>false</value>
</property>
```
