# 쓰레드 상태 제어
##### 쓰레드는 경우에 따라서 실행 중인 쓰레드의 상태를 변경해야 한다. 멀티 쓰레드 프로그램을 만들기 위해서는 정교한 쓰레드 상태 제어가 필요한데, 상태 제어가 잘못되면 프로그램에 오류가 발생할 수 있다. 이래서 멀티 쓰레드 프로그래밍이 어렵다는 말이 나온다. 쓰레드 제어를 제대로 하기 위해서 쓰레드의 상태 변화를 가져오는 메소드을 파악하고 있어야 한다.
##### 아래 그림은 상태 변화를 가져오는 메소드의 종류를 보여준다.

![쓰레드 상태](https://github.com/googolhkl/TIL/blob/master/java/thread/resource/ThreadState.png)

| 메소드 | 설명 |
| --- | --- |
| interrupt() | 일시 정지 상태의 쓰레드에서 InterruptedException 예외를 발생시켜, 예외 처리 코드(catch)에서 실행 대기 상태로 가거나 종료 상태로 갈 수 있도록 한다. |
| notify()<br />notifyAll() | 동기화 블록 내에서 wait() 메소드에 의해 일시 정지 상태에 있는 쓰네드를 실행 대기 상태로 만든다. | 
| resume() | suspend() 메소드에 의해 일시 정지 상태에 있는 쓰ㄴ레드를 실행 대기 상태로 만든다.<br /> - Deprecated (대신 notify(), notifyAll() 사용) |
| sleep(long milis)<br />sleep(long milis, int nanos) | 주어진 시간 동안 쓰레드를 일시 정지 상태로 만든다. 주어진 시간이 지나면 자동적으로 실행 대기 상태가 된다. |
| join()<br />join(long milis)</br />join(long milis, int nanos) | join() 메소드를 호출한 쓰레드는 일시 정지 상태가 된다. 실행 대기 상태로 가려면, join() 메소드를 멤버로 가지는 쓰레드가 종료되거나, 매개값으로 주어진 시간이 지나야 한다. |
| wait()<br />wait(long milis)<br />wait(long milis, int nanos) | 동기화(synchronized) 블록 내에서 쓰레드를 일시 정지 상태로 만든다. 매개값으로 주어진 시간이 지나면 자동적으로 실행 대기 상태가 된다. 시간이 주어지지 않으면 notify(), notifyAll() 메소드에 의해 실행 대기 상태로 갈 수 있다. |
| suspend() | 쓰레드를 일시 정지 상태로 만든다. resume() 메소드를 호출하면 다시 실행 대기 상태가 된다.<br /> - Deprecated (대신 wait() 사용) |
| yield() | 실행 중에 우선순위가 동일한 다른 쓰레드에게 실행을 양보하고 실행 대기 상태가 된다. |
| stop() | 쓰레드를 즉시 종료시킨다.<br /> - Deprecated |

##### 위 표에서 wait(), notify(), notifyAll() 은 Object 클래스의 메소드이고, 그 외의 메소드는 모두 Thread 클래스의 메소드들이다. wait(), notify(), notifyAll() 메소드의 사용 방법은 쓰레드의 동기화에서 자세히 알아보고, 여기서는 Thread 클래스의 메소드들만 살펴보자.

## sleep() - 주어진 시간동안 일시 정지
