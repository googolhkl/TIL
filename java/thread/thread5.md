# 쓰레드 상태
##### 쓰레드 객체를 생성하고 start() 메소드를 호출한다고 해서 바로 시작되는 것이 아니라 실행 대기 상태가 된다. 실행 대기 상태란 아직 스케줄링이 되지 않아서 실행을 기다리고 있는 상태이다. 실행 대기 상태에 있는 쓰레드 중에서 쓰레드 스케줄링으로 선택된 쓰레드가 비로서 CPU를 점유하고 run() 메소드를 실행한다. 이때를 실행(Running) 상태라고 한다. 실행 상태의 쓰레드는 run() 메소드를 모두 실행하기 전에 쓰레드 스케줄링이 의해 다시 실행 대기 상태로 갈 수 있고, 다른 쓰레드가 선택되어 그 쓰레드의 run() 메소드를 실행한다. 이렇게 쓰레드들을 조금씩 번갈아 가면서 실행하고 run() 메소드에서 더 이상 실행될 내용이 없으면 실행은 멈춘다. 이 상태를 종료 상태라고 한다.
##### 경우에 따라서는 쓰레드는 실행 상태에서 실행 대기 상태로 가지 않을 수도 있다. 실행 상태에서 일시 정지 상태로 가기도 하는데, 일시 정지 상태는 쓰레드가 실행할 수 없는 상태이다. 일시 정지 상태는 WAITING, TIMED_WAITING, BLOCKED가 있다.
##### 이러한 쓰레드의 상태를 코드에서 확인 할 수 있도록 하기 위해 자바 5부터 Thread 클래스에 getState() 메소드가 추가되었다. getState() 메소드는 다음 표처럼 쓰레드 상태에 따라서 Thread,State 열거 상수를 리턴한다.

| 상태         | 열거 상수    | 설명                              |
| --- | --- | --- |
| 객체 생성    | NEW          | 쓰레드 객체가 생성, 아직 start() 메소드가 호출되지 않은 상태 |
| 실행대기     | RUNNABLE     | 실행 상태로 언제든지 갈 수 있는 상태 |
| 일시 정지 | WAITING | 다른 쓰레드가 통지할 때까지 기다리는 상태 | 
| 일시 정지 | TIMED_WAITING | 주어진 시간 동안 기다리는 상태 |
| 일시 정지 | BLOCKED | 사용하고자 하는 객체의 락이 풀릴 때까지 기다리는 상태 |
| 종료 | TERMINATED | 실행을 마친 상태 |

## 예제

```java
public class Main
{
    public static void main(String[] args) throws InterruptedException
    {
        StatePrintThread statePrintThread = new StatePrintThread(new TargetThread());
        statePrintThread.start();
    }
}

class StatePrintThread extends Thread
{
    private Thread targetThread;

    public StatePrintThread(Thread targetThread) // 상태를 조사할 쓰레드
    {
        this.targetThread = targetThread;
    }

    public void run()
    {
        while(true)
        {
            Thread.State state = targetThread.getState(); // 실행상태 얻기
            System.out.println("타겟 쓰레드 상태 : " + state);

            // 객체 생성 상태일 경우 실행 대기 상태로 만듬
            if(state == Thread.State.NEW)
            {
                targetThread.start();
            }

            // 종료 상태일 경우 while문을 종료
            if(state == Thread.State.TERMINATED)
            {
                break;
            }

            try
            {
                Thread.sleep(500); // 0.5초 정지
            }
            catch(Exception e){}
        }
    }
}

class TargetThread extends Thread
{
    public void run()
    {
        for(long i=0; i<1000000000; i++){}

        try
        {
            Thread.sleep(1500);
        }
        catch(Exception e){}

        for(long i=0; i<1000000000; i++){}
    }
}
```
