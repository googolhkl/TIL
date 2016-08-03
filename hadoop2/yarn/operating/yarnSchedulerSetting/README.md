# 얀 스케줄러 설정
##### 리소스매니저는 전체 클러스터의 가용한 자원을 관리하고, 애플리케이션이 실행될 경우에 필요한 자원을 할당한다. 이때 애플리케이션의 요청 순서와 우선순위에 따라 적절히 할당해야 한다. 위와 같은 작업은 스케줄러가 담당한다. 얀은 ResourceScheduler 인터페이스를 제공하며, 이 인터페이스를 구현한 클래스를 리소스매니저의 스케줄러로 사용할 수 있다.

## 커패시티 스케줄러
##### 커패시티 스케줄러는 여러 개의 큐를 이용해 스케줄링을 수행한다. 큐는 클러스터에 자신이 사용할 수 있는 리소스의 할당 비율을 가지고 있으며, 이 할당 비율을 바탕으로 컨테이너를 할당받게 된다.또한 스케줄러는 큐를 지속적으로 모니터링해서 자원을 재분배한다. 특정 큐가 자원을 사용하지 않을 경우 여분의 용량을 다른 큐에게 임시로 할당한다(용통성이 있군). 그리고 큐에서 실행되는 애플리케이션의 우선순위를 설정할 수 있어 우선순위가 높은 애플리케이션이 빠르게 실행될 수 있다. 참고로 맵리듀스의 커패시티 스케줄러와는 달리 동일 큐에서 실행되는 애플리케이션들의 우선순위는 조정할 수 없다(이슈번호: YARN-1963). 그래서 각 큐는 애플리케이션이 큐에 등록된 순서대로 리소스를 할당한다. 마지막으로 커패시티 스케줄러는 큐를 계층적으로 구성할 수 있다.

### 기본 설정
##### 커패시티 스케줄러를 사용하려면 아래와 같이 yarn-site.xml에 설정해야한다. 참고로 얀은 기본 스케줄러로 커패시티 스커줄러를 사용한다.

```xml
<property>
 <name>yarn.resourcemanager.scheduler.class</name>
 <value>org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CapacityScheduler</value>
</property>
```
<br />

### 큐 등록
##### 이번 단계에서는 커패시티 스케줄러가 사용할 큐를 등록한다. 다음과 같이 `$HADOOP_HOME/etc/hadoop/capacity-scheduler.xml` 에 큐를 설정한다. 커패시티 스케줄러는 반드시 root 큐를 가지고 있어야 하고, root 큐는 여러개의 하위 큐로 구성될 수 있다.

```xml
<property>
 <name>yarn.scheduler.capacity.root.queues</name>
 <value>A,B</value>
</property>

<property>
 <name>yarn.scheduler.capacity.root.A.queues</name>
 <value>A1,A2</value>
</property>

<property>
 <name>yarn.scheduler.capacity.root.B.queues</name>
 <value>B1,B2</value>
</property>
```
<br />

### 큐 커패시티 설정
##### 커패시티 스케줄러에 등록한 큐는 다양한 설정을 적용할 수 있다. 다음표는 `HADOOP_HOME/etc/hadoop/capacity-scheduler.xml`에 설정하는 다양한 커패시티 스케줄러 속성을 나타낸다.
| 속성 | 내용 |
| --- | --- |
| yarn.scheduler.capacity.<큐명칭>.capacity | 큐가 전체 컨테이너에서 사용할 수 있는 비율을 의미한다. 모든 큐의 사용 비율을 합산했을 때 100이하여야 한다. |
| yarn.scheduler.capacity.<큐며칭>.maximum-capacity | 큐에서 사용할 수 있는 최대 컨테이너 비율이다. 큐에 잡이 등록되지 않은 경우에도 이 값을 설정함으로써 여분의 컨테이너를 남겨둘 수 있다. 기본값은 -1이며, 이 옵션은 사용하지 않는다는 것이다. |
| yarn.scheduler.capacity.<큐명칭>.minimum-user-limit-percent | 하나의 사용자가 큐에서 사용할 수 있는 컨테이너 비율이다. |
| yarn.scheduler.capacity.<큐명칭>.user-limit-factpr | 하나의 사용자가 추가로 획득할 수 있는 컨테이너의 개수이다. |
| yarn.scheduler.capacity.maximum-applications yarn.scheduler.capacity/<큐명칭>.maximum-applications | 얀 클러스터에서 실행할 수 있는 최대 애플리케이션 개수를 나타낸다. 큐 별로 제어하고 싶을 경우 maximum-applications 앞에 큐 이름을 추가하면 된다. | 
| yarn.scheduler.capacity.maximum-am-resource-percent yarn.scheduler.capacity.<큐명칭>.maximum-am-resource-percent | 얀 클러스터에서 애플리케이션마스터를 실행하기 위해 사용할 수있는 컨테이너 비율을 의미한다. 큐별로 제어하고 싶을 경우 maximum-am-resource-percent앞에 큐 이름을 추가하면 된다. |
| yarn.scheduler.capacity.resource-calculator | 커패시티 스케줄러 내에서 리소스를 계산할 때 사용하는 클래스다. 기본값은 org.apache.hadoop.yarn.util.resource.DefaultResourceCalculator로 설정돼 있으며, DefaultResourceCalculator는 메모리 값을 기준으로 계산을 수행한다. | 
| yarn.scheduler.capacity.node-locality-delay | 커패시티 스케줄러는 컨테이너가 실행되는 노드의 종류에따라 컨테이너를 할당한다. 컨테이너 종류로는 노드 로컬(Node local), 랙 로컬(Rack Local), 스위치 오프(Switch off)가 있다. 각 노드는 다음과 같은 특징이 있다. <br />첫째, 노드 로컬은 애플리케이션이 필요한 데이터가 위치한 노드의 컨테이너를 할당하는 것이다.<br />둘째, 랙 로컬은 애플리케이션이 필요한 데이터가 위치한 랙에 포함돼있는 컨테이너를 할당하는 것이다.<br />셋째, 데이터가 있는 랙과 별개의 랙의 컨테이너를 할당하는 것이다. 스케줄러는 각 노드 타입에 맞게 컨테이너를 할당하는데, 할당하기 전에 해당 노드에 할당이 가능한지 검사한다. 이때 랙 로컬의 경우 조금 복잡한 검사 로직을 수행한다.<br />우선 스케줄러는 애플리케이션의 최종적인 스케줄링을 완료하기 전에 주어진 우선순위로 작업을 스케줄링할 수 있는 기회가 몇 번이나 있었는지 확인한다. 이 횟수는 SchedulerApplicationAttempt의 getSchedulingOppotunities 메소드로 조회할 수 있다. 참고로 SchedulerApplicationAttempt는 스케줄러 관점에 애플리케이션어템프트를 표현한 클래스다.<br />그리고 얀 클러스터의 노드 개수와 이 옵션값 중 최소값을 산출한다. 마지막으로 이 최소값이 스케줄링할 수 있는 기회 횟수보다 작다면 랙 로컬이 가능한 것으로 가정한다(이슈번호: YARN-80).<br />참고로 이 속성의 기본값은 40으로 설정돼 있다. 일반적으로 얀 클러스터의 노드 개수만큼 설정하는 것을 권장한다. |

