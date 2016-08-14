# 쓰레드 풀
##### 병렬 작업 처리가 많아지면 쓰레드 개수가 증가되고 그에 따른 쓰레드 생성과 스케줄링으로 인해 CPU가 바빠져 메모리 사용량이 늘어난다. 따라서 애플리케이션의 성능이 저하된다. 갑작스런 병렬 작업의 폭증으로 인한 쓰레드의 폭증을 막으려면 쓰레드풀(ThreadPool)을 사용해야한다.
##### 쓰레드풀은 작업 처리에 사용되는 쓰레드를 제한된 개수만큼 정해 놓고 작업 큐(Queue)에 들어오는 작업들을 하나씩 쓰레드가 맡아 처리한다. 작업처리가 끝난 쓰레드는 다시 작업 큐에서 새로운 작업을 가져와 처리한다. 그렇기 때문에 작업 처리 요청이 폭증되어도 쓰레드의 전체 개수가 늘어나지 않으므로 애플리케이션의 성능이 급격히 저하되지 않는다.
##### 자바는 쓰레드풀을 생성하고 사용할 수 있도록 java.util.concurrent 패키지에서 ExecutorService 인터페이스와 Executors 클래스를 제공하고 있다. Executors 의 다양한 정적 메소드를 이용해서 ExecutorService 구현 객체를 만들 수 있는데, 이것이 바로 쓰레드풀이다.
<br />

## 1. 쓰레드풀 생성 및 종료
### 쓰레드풀 생성
##### ExecutorService 구현 객체는 Executors 클래스의 다음 두 가지 메소드 중 하나를 이용해서 간편하게 생성할 수 있다.

| 메소드명(매개 변수) | 초기 쓰레드 수 | 코어 쓰레드 수 | 최대 쓰레드 수 |
| --- | --- | --- | --- |
| newCachedThreadPool() | 0 | 0 | Integer.MAX_VALUE |
| newFixedThreadPool(int nThreads) | 0 | nThreads | nThreads |

##### 초기 쓰레드 수는 ExecutorService 객체가 생성될 때 기본적으로 생성되는 쓰레드 수를 말하고, 코어 쓰레드 수는 쓰레드 수가 증가된 후 사용되지 않는 쓰레드를 쓰레드에서 제거할 때 최소한 유지해야 할 쓰레드 수를 말한다. 최대 쓰레드 수는 쓰레드풀에서 관리하는 최대 쓰레드 수이다.
##### newCachedThreadPool() 메소드로 생성된 쓰레드풀의 특징은 초기 쓰레드 개수와 코어 쓰레드 개수는 0개이고, 쓰레드 개수보다 작업 개수가 많으면 새 쓰레드를 생성시켜 작업을 처리한다. 이론적으로는 int 값이 가질 수 있는 최대값만큼 쓰레드가 추가되지만, 운영체제의 성능과 상황에 따라 달라진다. 1개 이상의 쓰레드가 추가되었을 경우 60초 동안 추가된 쓰레드가 아무 작업을 하지 않으면 추가된 쓰레드를 종료하고 풀에서 제거한다. 다음은 newCachedThreadPool() 을 호출해서 ExecutorService구현 객체를 얻는 코드이다.

```java
ExecutorService executorService = Executors.newCachedThreadPool();
```
<br />

##### newFixedThreadPool(int nThreads) 메소드로 생성된 쓰레드풀의 초기 쓰레드 개수는 0개이고, 코어 쓰레드 수는 nThreads이다. 쓰레드 개수 보다 작업 개수가 많으면 새 쓰레드를 생성시키고 작업을 처리한다. 최대 쓰레드 개수는 매개값으로 준 nThreads이다. 이 쓰레드풀은 쓰레드가 작업을 처리하지 않고 놀고 있더라도 쓰레드 개수가 줄지 않는다. 다음은 CPU 코어의 수 만큼 최대 쓰레드를 사용하는 쓰레드풀을 생성한다.

```java
ExecutorService executorService = Executors.newFixedThreadPool(
    Runtime.getRuntime().availableProcessors()
);
```
<br />

##### newCachedThreadPool()과 newFixedThreadPool() 메소드를 사용하지 않고 코어 쓰레드 개수와 최대 쓰레드 개수를 설정하고 싶다면 직접 ThreadPoolExecutor 객체를 생성하면 된다.
##### 위에서 설명한 두 가지 메소드도 내부적으로 ThreadPoolExecutor 객체를 생성해서 리턴한다. 다음은 초기 쓰레드 개수가 0개, 코어 쓰레드 개수가 3개, 최대 쓰레드 개수가 100개인 쓰레드풀을 생성한다. 그리고 코어 쓰레드 3개를 제외한 나머지 추가된 쓰레드가 120초 동안 놀고 있을 경우 해당 쓰레드를 제거해서 쓰레드 수를 관리한다.

```java
ExecutorService threadPool = new ThreadPoolExecutor(
    3, // 코어 쓰레드 개수
    100, // 최대 쓰레드 개수
    120L, // 놀고 있는 시간
    TimeUnit.SECONDS, // 놀고 있는 시간 단위
    new SynchronousQueue<Runnable>() // 작업 큐
);
```

### 쓰레드풀 종료
##### 쓰레드풀의 쓰레드는 기본적으로 데몬 쓰레드가 아니기 때문에 main 쓰레드가 종료되더라도 작업을 처리하기 위해 계속 실행 상태로 남아있다. 그래서 main() 메소드가 실행이 끝나도 애플리케이션 프로세스는 종료되지 않는다. 애플리케이션을 종료하려면 쓰레드풀을 종료시켜 쓰레드들이 종료 상태가 되도록 처리해줘야 한다. ExecutorService는 종료와 관련해서 아래와 같이 세 개의 메소드를 제공하고 있다.

| 리턴타입 | 메소드명(매개 변수) | 설명 |
| --- | --- | --- |
| void | shutdown() | 현재 처리 중인 작업뿐만 아니라 작업 큐에 대기하고 있는 모든 작업을 처리한 뒤에 쓰레드풀을 종료시킨다. |
| List<Runnable> | shutdownNow() | 현재 작업 처리 중인 쓰레드를 interrupt해서 작업 중지를 시도하고 쓰레드풀을 종료시킨다. 리턴값은 작업 큐에 있는 미처리된 작업(Runnable)의 목록이다. |
| boolean | awaitTermination(long timeout, TimeUnit unit) | shutdown() 메소드 호출 이후, 모든 작업 처리를 timeout 시간 내에 완료하면 true를 리턴하고, 완료하지 못하면 작겁 처리 중인 쓰레드를 interrupt하고 false를 리턴한다. | 
<br />

##### 보통 남아있는 작업을 마무리하고 쓰레드풀을 종료할 때에는 shutdown()을 호출하고, 남아있는 작업과는 상관없이 강제 종료할 때에는 shutdownNow()를 호출한다.

```java
executorService.shutdown();
또는
executorService.shutdownNow();
```

## 2. 작업 생성과 처리 요청
### 작업 생성
##### 하나의 작업은 Runnable 또는 Callable 구현 클래스로 표현한다. Runnable과 Callable의 차이는 작업 처리 완료 후 리턴값이 있냐 없냐 차이다. 
##### 아래는 작업을 정의하기 위해 Runnable과 Callable 구현 클래스를 작성하는 방법을 보여준다.

| Runnable 구현 클래스 | Callable 구현 클래스 |
| --- | --- |
| Runnable task = new Runnable() {<br /> @Override<br /> public void run() {<br /> // 쓰레드가 처리할 작업 내용<br /> }<br />} | Callable<T> task = new Callable<T> {<br /> @Override<br /> public T call() throws Exception {<br />// 쓰레드가 처리할 작업 내용<br /> }<br />} |
