# 보조서비스
##### 맵리듀스는 맵태스크와 리듀스태스크 사이에는 서플이라는 작업이 있다. 서플 단계에서 리듀스로 전송할 데이터를 파티셔닝하고, 파티셔닝된 데이터를 네트워크를 통해 전달한다.
##### 얀 클러스터에서 맵리듀스 애플리케이션을 실행할 경우 맵태스크는 노드매니저의 컨테이너에서 실행된다. 그런데 노드매니저는 컨테이너에서 실행 중이던 애플리케이션 실행이 종료될 경우 컨테이너도 함께 종료 시킨다. (이렇게 컨테이너가 종료해버리면 맵태스크가 리듀스태스크로 정보를 못보내잖아..) 즉, 셔플을 실행할 수 없다.

##### 얀은 이러한 상황을 방지하기 위해 보조서비스(Auxiliary Service)를 제공한다. 보조서비스는 노드매니저 간의 서비스 제어를 위한 기능이며, 이 서비스를 이용해 서로 다른 노드매니저 사이에 데이터를 전달하거나 다른 노드매니저를 제어할 수 있다. 보조서비스를 맵리듀스에 적용하면 맵태스크를 실행하는 노드매니저와 리듀스태스크를 실행하는 노드매니저 사이에 셔플이 가능해 진다.

##### 보조서비스의 이해를 돕기 위해 이전에 얀의 전체적인 작업흐름에 봤던 그림을 떠올려보자.
##### 1. 클라이언트가 리소스매니저에게 애플리케이션 실행을 요청한다.
##### 2. 리소스매니저는 해당 애플리케이션의 애플리케이션마스터를 실행한다.
##### 3. 얀은 맵리듀스의 애플리케이션마스터로 MRAppMaster(org.apache.hadoop.mapreduce.v2.app.MRAppMaster)를 제공한다. 리소스매니저가 애플리케이션마스터 실행을 요청하면 노드매니저가 컨테이너에서 MRAppMaster를 실행한다.
##### 4. MRAppMaster는 또 다른 노드매니저에게 맵태스크 실행을 요청한다.
##### 5. 요청을 받은 또 다른 노드매니저가 컨테이너에서 맵태스크를 실행한다.
##### 6. 노드매니저에서 실행된 맵태스크는 태스크 수행 결과를 셔플 과정을 통해 리듀스 태스크에게 전달한다.

##### 6단계에서 셔플이 담당하는 클래스는 얀이 제공하는 ShuffleHandler(org.apache.hadoop.mapred.ShuffleHandler)이다. ShuffleHandler는 보조서비으싀 추상 클래스인 AuxiliaryService(org.apache.hadoop.yarn.server.api.AuxiliaryService)를 상속받아 구현된 클래스이다.

##### 노드매니저가 보조서비스를 인식하려면 노드매니저가 실행되기 전에 yarn-site.xml에 해당 보조서비스를 설정해야 한다. ShuffleHandler의 경우 다음과 같이 yarn-site.xml에 설정하면 된다.

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
