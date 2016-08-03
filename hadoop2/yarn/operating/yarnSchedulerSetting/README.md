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
<br />

### 큐 ACL 설정
##### 커패시티 스케줄러는 큐에 대한 접근을 제어하기 위해 ACL을 사용한다. ACL 정보는 `capacity-scheduler.xml`파일에 설정하며 다음과 같은 속성값을 설정할 수 있다.
| 속성 | 내용 |
| --- | --- |
| yarn.scheduler.capacity.<큐명칭>.state | 큐의 상태를 의미하며, RUNNING 혹은 STOPPED 중 하나를 지정할 수 있다. 만약 큐의 상태가 STOPPED로 설정돼 있다면 애플리케이션은 해당 큐를 이용할 수 없다. 참고로 루트 큐를 STOPPED로 설정할 경우 전체 큐를 사용할 수 없다. |
| yarn.scheduler.capacity.<큐명칭>.acl_submit_applications | 해당 큐에 애플리케이션을 등록할 수 있는 사용자명이나 그룹명을 설정한다. 콤마 단위로 구분해 여러 사용자나 그룹을 등록할 수 있다. 만약 이 속성값을 *로 표시하면 권한 제어 없이 모든 사용자에게 해당 큐를 오픈하겠다는 의미다. |
| yarn.scheduler.capacity.root.<큐명칭>.acl_administer_queue | 해당 애플리케이션을 종료하거나 애플리케이션의 우선순위를 변경할 수 있는 사용자나 그룹을 설정한다. 콤마 단위로 구분해서 입력할 수 있으며, *로 표시할 경우 모든 사용자가 애플리케이션을 제어할 수 있다. | 


### 커패시티 스케줄러 적용
##### 이번에는 커패시티 스케줄러에 두 개의 큐를 등록하고, 각 큐의 리소스 비율과 ACL을 설정해 보겠다. 아래 예제에서는 default와 test라는 두개의 큐를 등록한다.
##### `HADOOP_HOME/etc/hadoop/capacity-scheduler.xml` 파일에 다음과 같이 적어주고 얀 클러스터를 재구동한다.(참고로 googolhkl1서버의 파일만 변경해 주면 된다)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>

<!-- 공통 설정 -->
	<!-- 얀 클러스터에서 실행할 수 있는 최대 애플리케이션 개수 -->
	<property>
		<name>yarn.scheduler.capacity.maximum-applications</name>
		<value>10000</value>
	</property>

	<!-- 얀 클러스터에서 애플리케이션마스터를 실행하기 위해 사용할 수 있는 컨테이너 비율(10%) -->
	<property>
		<name>yarn.scheduler.capacity.maximum-am-resource-percent</name>
		<value>0.1</value>
	</property>

	<!-- 커패시티 스캐줄러 내에서 리소스를 계산할 때 사용하는 클래스로 기본값을 준다 -->
	<property>
		<name>yarn.scheduler.capacity.resource-calculator</name>
		<value>org.apache.hadoop.yarn.util.resource.DefaultResourceCalculator</value>
	</property>

	<!-- default, test라는 두개의 큐를 등록한다 -->
	<property>
		<name>yarn.scheduler.capacity.root.queues</name>
		<value>default,test</value>
	</property>

	<!-- 기본값으로 40을 준다 -->
	<property>
		<name>yarn.scheduler.capacity.node-locality-delay</name>
		<value>40</value>
	</property>

<!-- root 큐 -->
	<!-- default 큐가 전체 컨테이너에서 사용할수 있는 비율을 80%로 설정-->
	<property>
		<name>yarn.scheduler.capacity.root.default.capacity</name>
		<value>80</value>
	</property>

	<!-- 하나의 사용자가 추가로 획득할 수 있는 컨테이너수는 1개 -->
	<property>
		<name>yarn.scheduler.capacity.root.default.user-limit-factor</name>
		<value>1</value>
	</property>

	<!-- default 큐에서 사용할 수 있는 최대 컨테이너 비율은 90% -->
	<property>
		<name>yarn.scheduler.capacity.root.default.maximum-capacity</name>
		<value>90</value>
	</property>

	<!-- default큐를 RUNNING 상태로 만든다 -->
	<property>
		<name>yarn.scheduler.capacity.root.default.state</name>
		<value>RUNNING</value>
	</property>

	<!-- 모든 사용자와 그룹이 애플리케이션을 등록할 수 있다. -->
	<property>
		<name>yarn.scheduler.capacity.root.default.acl_submit_applications</name>
		<value>*</value>
	</property>

	<!-- 모든 사용자와 그룹이 애플리케이션을 제어할 수 있다 -->
	<property>
		<name>yarn.scheduler.capacity.root.default.acl_administer_queue</name>
		<value>*</value>
	</property>

<!-- test 큐 -->
	<!-- test 큐가 전체 컨테이너에서 사용할 수 있는 비율을 20%로 설정 -->
	<property>
		<name>yarn.scheduler.capacity.root.test.capacity</name>
		<value>20</value>
	</property>

	<!-- 하나의 사용자가 추가로 획득할 수 있는 컨테이너수는 1개 -->
	<property>
		<name>yarn.scheduler.capacity.root.test.user-limit-factor</name>
		<value>1</value>
	</property>

	<!-- test 큐에서 사용할 수 있는 최대 컨테이너 비율은 30% -->
	<property>
		<name>yarn.scheduler.capacity.root.test.maximum-capacity</name>
		<value>30</value>
	</property>

	<!-- test큐를 RUNNING 상태로 만든다 -->
	<property>
		<name>yarn.scheduler.capacity.root.test.state</name>
		<value>RUNNING</value>
	</property>

	<!-- 모든 사용자와 그룹이 애플리케이션을 등록할 수 있다 -->
	<property>
		<name>yarn.scheduler.capacity.root.test.acl_submit_applications</name>
		<value>*</value>
	</property>

	<!-- 모든 사용자와 그룹이 애플리케이션을 제어할 수 있다 -->
	<property>
		<name>yarn.scheduler.capacity.root.test.acl_administer_queue</name>
		<value>*</value>
	</property>
</configuration>
```

##### 이제 웹 인터페이스(http://호스트(googolhkl1):8088/cluster/scheduler)에 접속하면 root큐와 default,test 큐가 등록된 것을 확인할 수 있다. ~무슨 이유인지 모르겠지만 스크린샷이 안찍힌다..~
<br />

##페어 스케줄러
##### 페어 스케줄러도 커패시티 스케줄러처럼 기존 맵리듀스에서 사용하던 스케줄러다. 맵리듀스의 페어 스케줄러는 풀(Pool) 단위로 태스크 슬롯을 관리했다. 얀의 페어 스케졸러는 기존의 풀을 큐로 명칭만 변경했을 뿐 내부 방식은 풀 관리 방식과 동일하게 동작한다.

### 기본 설정
##### 페어 스케줄러를 적용하려면 얀의 스케줄러를 커패시티 스케줄러에서 페어 스케줄러로 변경해야 한다. `yarn-site.xml`을 다음과 같이 작성한다.
```xml
<property>
	<name>yarn.resourcemanager.scheduler.class</name>
	<value>org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.FairScheduler</value>
</property>
```
<br />

### 얀 환경설정 파일 수정
##### 페어 스케줄러를 실제로 적용하려면 다양한 설정을 변경해야 한다. 페어 스케줄러의 큐 설정은 별도 파일로 설정하지만 다른 속성들은 yarn-site.xml에 설정하면 된다. 다음표는 yarn-site.xml에서 설정할 수 있는 페어 스케줄러의 속성값이다.
| 속성 | 내용 |
| --- | --- |
| yarn.scheduler.fair.allocation.file | 페어 스케줄러에서 사용하는 큐에 대한 설정 파일 경로를 나타낸다. 기본값은 fair-scheduler.xml 이다. |
| yarn.scheduler.fair.user-as-default-queue | 이 속성값은 애플리케이션을 실행할 때 큐를 별도로 설정하지 않은 경우에 대처하기 위한 속성값이다. 이 속성값이 true로 설정돼 있다면 사용자 이름을 큐의 이름으로 사용하게 된다. 하지만 이 값이 false로 돼 있다면 큐를 설정하지 않은 애플리케이션은 무조건 default 큐로 할당된다. 참고로 이 속성의 기본값은 true로 설정돼 있다. |
| yarn.scheduler.fair.preemption | 페어 스케줄은 선점 기능을 제공한다. 선점은 얀 클러스터의 자원을 공평하게 사용하지 못하고 있는 경우 자원을 과도하게 사용하는 컨테이너를 종료시키는 기능이다. 기본값은 false로 돼 있어서 선점 기능을 사용하지 않게 돼 있다. 참고로 현재 버전의 preemption 기능은 실험적인 기능으로, 완벽하게 동작하지 않을 수 있다. |
| yarn.scheduler.fair.preemption.cluster-utilization-threshold | 선점 기능이 실행되는 임계치를 의미한다. 전체 리소스의 최대 사용 비율이 이 임계치에 도달하면 선점이 실행된다. 기본값은 0.8f이며 80%를 의미한다. |
| yarn.scheduler.fair.sizebasedweight | 애플리케이션의 가중치를 애플리케이션의 크기로 측정하게 하는 값이다. 기본적으로 애플리케이션의 가중치는 애플리케이션의 우선순위 속성으로 측정된다. 하지만 이 속성을 true로 설정할 경우 애플리케이션이 필요한 메모리 용량이 많은 순서대로 가중치가 부여된다. 기본값은 false로 설정돼 있다. |
| yarn.scheduler.fair.assignmultiple | 하나의 하트비트에 다중 컨테이너 할당을 허용할지를 설정한다. 기본값은 false다. |
| yarn.scheduler.fair.max.assign | yarn.scheduler.fair.assignmultiple 속성을 true로 설정할 경우 하나의 하트비트에서 할당 가능한 컨테이너 개수다. 기본값은 -1이며, 무제한을 의미한다. |
| yarn.scheduler.fair.allow-undeclared-pools | 이 속성이 true일 경우 새로운 큐는 애플리케이션의 실행 요청 시간에 생성될 수 있다. 왜냐하면 애플리케이션을 실행할 때 큐가 설정돼 있거나 yarn.scheduler.fair.user-as-default-queue 속성으로 사용자 이름이 큐로 설정되기 때문이다. 하지만 이 속성이 false일 경우 큐 설정 파일에 정의된 속성은 무시된다. 그래서 애플리케이션은 큐 설정 파일에 정의되지 않은 큐로 변경될 수 있다. 기본값은 true다. |
<br />

### 큐 설정
##### 큐 설정 파일에는 최소/최대 리소스, 최대 애플리케이션 실행 개수와 같은 큐의 기본 정보를 설정한다.
##### 아래는 큐 설정 파일의 예이다.
```xml
<?xml version="1.0"?>
<allocations>
	<queue name="sample_queue">
		<minResources>10000 mb,0vcores</minResources>
		<maxResources>90000 mb,0vcores</maxResources>
		<maxRunningApps>50</maxRunningApps>
		<maxAMShare>0.1</maxAMShare>
		<weight>2.0</weight>
		<schedulingPolicy>fair<schedulingPolicy>
		<queue name="sample_sub_queue">
			<aclSubmitApps>charlie</aclSubmitApps>
			<minResources>5000 mb,0vcores</minResources>
		</queue>
	</queue>

	<queueMaxAMShareDefault>0.5</queueMaxAMShareDefault>

	
	<!-- 아래에 나오는 secondary_group_queue는 부모 큐이고 유저 큐는 이 큐의 자식으로 온다. -->
	<queue name="secondary_group_queue" type="parent">
	<weight>3.0</weight>
	</queue>

	<user name="sample_user">
		<maxRunningApps>30</maxRunningApps>
	</user>
	<userMaxAppsDefault>5</userMaxAppsDefault>

	<queuePlacementPolicy>
		<rule name="specified" />
		<rule name="primaryGroup" create="false" />
		<rule name="nestedUserQueue">
			<rule name="secondaryGroupExistingQueue" create="false" />
		</rule>
		<rule name="default" queue="sample_queue" />
	</queuePlacementPolicy>
</allocations>
```
<br />

##### 모든 정보는 XML 엘리먼트로 정의되며, queue와 user의 경우에만 하위 엘리먼트로 속성을 정의한다. queue와 user엘리먼트는 특정 큐와 특정 사용자에 대한 설정이며, 나머지 엘리먼트는 전체 큐 및 사용자를 대상으로 하는 설정이다. 각 엘리먼트는 다음과 같은 내용을 나타낸다.

* Queue : 각 큐에 대한 설정 정보다.
 * minResources : 큐가 최소한으로 점유해야 하는 리소스 규모이며, "X mb, Y cores" 형식으로 표현한다. 예를 들어. 512MB, 1 코어를 최소로 공유하고 싶다면 "512mb,1vcores"로 설정한다.
 * maxResources : 큐가 최대한으로 점유할 수 있는 리소스 규모이며, "X mb, Y cores"형식으로 표현한다.
 * maxRunningApps : 큐에서 최대로 실행할 수 있는 애플리케이션 개수다.
 * maxAMShare : 애플리케이션마스터를 실행하기 위해 사용할 수 있는 큐의 리소스 공유 비율을 제한한다. 이 속성은 leaf queue(리프큐)에서만 사용할 수 있다. 예를 들어, 이 속성값을 1.0f로 설정할 경우 리프큐에 있는 애플리케이션마스터는 공유된 메모리와 CPU를 100% 사용할 수 있다. 참고로 기본값은 -1이며, 이 값은 비율 제한을 확인하지 않겠다는 의미다.
 * weight : 다른 큐와 비교할 때 사용되는 가중치이며, 기본값은 1.0을 사용한다. 예를 들어, weight가 2.0으로 설정된 큐는 가중치가 1.0인 큐보다 2배 높은 가중치가 부여된 것이다.
 * aclSubmitApps : 큐에 애플리케이션 실행을 요청할 수 있는 사용자와 그룹 목록이다.
 * aclAdministerApps : 큐에 대한 관리자 기능이 있는 사용자 및 그룹 목록이다. 참고로 관리자 기능이 있어야만 애플리케이션을 강제로 종료할 수 있다.
 * minSharePreemptionTimeout : 최소 공유 자원보다 작은 자원을 사용하고 있는 컨테이너가 있을 경우 다른 컨테이너가 종료되기 전까지 대기해야만 하는 시간이다. 기본값은 무한대(infinite)다.
 * schedulingPolicy : 큐의 내부 스케줄링 모드를 결정한다. 페어 스케줄링 방식과 FIFO 방식 중에서 선택할 수 있다. 기본값은 페어 스케줄링 방식인 fair이다.
* User : 큐를 사용하는 사용자에 대한 정보를 설정한다. 사용자에게 제한을 두지 않으려면 이 엘리먼트를 정의하지 않아도 된다.
 * maxRunningApps : 해당 사용자가 큐에서 최대로 실행할 수 있는 애플리케이션 개수다.
* userMaxAppsDefault : 등록된 전체 사용자가 최대한으로 실행할 수 있는 애플리케이션 개수다.
* fairSharePreemptionTimeout : 애플리케이션이 자신에게 할당된 공유 자원의 절반 이하만 사용할 수 있을 경우 선점을 대기해야 하는 시간이다.
* defaultMinSharePreemptionTimeout : 등록된 전체 큐에 적용돼야 하는 최소 선점 대기 시간이다.
* queueMaxAppsDefault: 등록된 전체 큐가 최대한으로 실행할 수 있는 애플리케이션 개수다.
* queueMaxAMShareDefault : 전체 큐에 적용돼야 하는 애플리케이션 마스터 리소스 공유 비율이다. 이 속성값을 정의할 경우 Queue 엘리먼트에 정의한 maxAMShare는 오버라이드된다.
* defaultQueueSchedulingPolicy : 전체 큐에 적용돼야 하는 내부 스케줄링 모드다. 페어 스케줄링 방식과 FIFO 방식 중에서 선택할 수 있다, 기본값은 fair이며, 이는 페어 스케줄링을 의미한다. 만약 이 속성값을 정의할 경우 Queue 엘리먼트에서 정의한 schedulingPolicy는 오버라이드된다.
* queuePlacementPolicy : 페어 스케줄러는 실행을 요청한 애플리케이션의 큐를 변경할 수 있다. 이때 큐 변경에는 다양한 룰을 적용할 수 있으며, 이 엘리먼트에 각 룰을 설정할 수 있다. 참고로 룰은 작성된 순서대로 적용되며, 룰에 맞는 큐를 찾을 수 없을 경우 다름 룰을 실행한다.
 * specified : 애플리케이션이 요청한 큐에 할당한다. 예를 들어, 애플리케이션을 요청할 때 큐를 명사히지 않았다면 default 큐로 할당된다.
 * user : 애플리케이션 실행을 요청한 사용자 이름과 동일한 큐로 할당된다.
 * primaryGroup : 애플리케이션 실행을 요청한 사용자의 기본 그룹 이름과 동일한 큐에 할당된다.
 * secondaryGroupExistingQueue : 애플리케이션 실행을 요청한 사용자의 보조 그룹 이름과 동일한 큐에 할당된다.
 * nestedUserQueue : 이 룰이 제안한 큐에 포함돼 있는 사용자 이름을 가진 큐로 애플리케이션을 할당한다. 이 속성은 User 엘리먼트와 규칙이 매우 유사하지만 부모 큐를 다루는 방식에 차이가 있다. User 엘리먼트는 부모 큐의 하위에만 등록할 수 있지만 이 속성은 부모 큐를 중첩해서 사용할 수 있다.
 * default : 기본룰의 queue 속성에 정의된 큐로 할당된다. 만약 queue 속성이 없다면 root.default 큐로 할당된다.
 * reject : 애플리케이션 할당이 거부된다.
<br />

### 페어 스케줄러 적용
##### 이번 단계에서 페어 스케줄러에 세 개의 큐를 등록해보겠다. 아래 예제에서는 default, service, test라는 세 개의 큐를 등록한다. 참고로 페어 스케줄러는 root의 default 큐를 기본으로 등록하며, 여기서 등록한 default 큐는 이 큐를 오버라이드한다.
##### 아래 예제는 `HADOOP_HOME/etc/hadoop/fair-scheduler.xml` 파일로 작성한 후 얀 클러스터를 재구동한다.

```xml
<?xml version="1.0"?>

<allocations>
	<user name="hkl">
		<maxRunningApps>1000</maxRunningApps>
	</user>

	<userMaxAppsDefault>1000</userMaxAppsDefault>
	<queue name="default">
		<minResources>1024 mb, 1 vcores</minResources>
		<maxResources>5120 mb, 4 vcores</maxResources>
		<schedulingPolicy>fair</schedulingPolicy>
		<weight>0.25</weight>
		<minSharePreemptionTimeout>2</minSharePreemptionTimeout>
	</queue>

	<queue name="test">
		<minResources>1024 mb, 1 vcores</minResources>
		<maxResources>5120 mb, 4 vcores</maxResources>
		<schedulingPolicy>fair</schedulingPolicy>
		<weight>0.25</weight>
		<minSharePreemptionTimeout>2</minSharePreemptionTimeout>
	</queue>

	<queue name="service">
		<minResources>1024 mb, 1 vcores</minResources>
		<maxResources>8192 mb, 10 vcores</maxResources>
		<weight>0.5</weight>
		<schedulingPolicy>fair</schedulingPolicy>
		<minSharePreemptionTimeout>2</minSharePreemptionTimeout>
	</queue>
</allocations>
```

##### 얀의 웹 인터페이스 (http://호스트(googolhkl1):8088/cluster/scheduler)에 접속하면 확인할 수 있다.


