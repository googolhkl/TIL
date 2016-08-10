# 쓰레드 상태 제어
##### 쓰레드는 경우에 따라서 실행 중인 쓰레드의 상태를 변경해야 한다. 멀티 쓰레드 프로그램을 만들기 위해서는 정교한 쓰레드 상태 제어가 필요한데, 상태 제어가 잘못되면 프로그램에 오류가 발생할 수 있다. 이래서 멀티 쓰레드 프로그래밍이 어렵다는 말이 나온다. 쓰레드 제어를 제대로 하기 위해서 쓰레드의 상태 변화를 가져오는 메소드을 파악하고 있어야 한다.
##### 아래 그림은 상태 변화를 가져오는 메소드의 종류를 보여준다.

![쓰레드 상태](https://github.com/googolhkl/TIL/blob/master/java/thread/resource/ThreadState.png)

| 메소드 | 설명 |
| ------------------------------------------------------- | --- |
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
##### 실행 중인 쓰레드를 일정 시간 멈추게 하고 싶다면 Thread 클래스의 정적 메소드인 sleep()을 사용하면 된다. 아래와 같이 Thread.sleep() 메소드를 호출한 쓰레드는 주어진 시간 동안 일시 정지 상태가 되고, 다시 실행 상태로 돌아간다. 

```java
try
{
    Thread.sleep(1000); // 1초
}
catch(InterruptedException e)
{
    // interrupt() 메소드가 호출되면 실행
}
```

##### 매개값에는 얼마 동안 일시 정지 상태로 있을 것인지 밀리세컨드 단위로 시간을 적어주면 된다. 1000을 적으면 1초이다.
##### 일시 정지 상태에서 주어진 시간이 되기 전에 interrupt() 메소드가 호출되면 InterruptedException이 발생하기 때문에 예외 처리가 필요하다.
##### 아래는 3초 주기로 비프(bepp)음을 10번 발생시키는 예제다.

```java
import java.awt.Toolkit;

public class Main
{
    public static void main(String[] args)
    {
        Toolkit tk = Toolkit.getDefaultToolkit();
        for(int i=0; i<10; i++)
        {
            tk.beep();
            try
            {
                Thread.sleep(3000);
            }
            catch(InterruptedException e)
            {
            }
        }
    }
}
```
<br />

## yield() - 다른 쓰레드에게 실행 양보
##### 쓰레드가 처리하는 작업은 반복적인 실행을 위한 for문이나 while문을 포함하는 경우가 많다. 가끔은 이 반복문들이 무의미한 반복을 하는 경우가 있다. 아래 코드를 보면서 이해해보자.
```java
public void run()
{
    while(true)
    {
        if(flag)
        {
            System.out.println("작업할 내용");
        }
    }
}
```

##### 쓰레드가 시작되어 run() 메소드를 실행하면 while(true){} 블록을 무한 반복 실행한다. 만약 flag의 값이 false라면 그리고 flag에서 true에서 false로 변경되는 시점이 불명확하면, while문은 어떠한 실행문도 실행하지 않고 무의미한 반복을 하게된다. 이것보다 다른 쓰레드에게 실행을 양보하고 자신은 실행 대기 상태로 가는것이 프로그램 성능에 도움디 된다. 이런 기능을 위해 쓰레드는 yield() 메소드를 제공한다. 아래 코드는 위의 코드의 무의미한 반복을 줄이기 위한 yield() 메소드를 호출하는 코드이다.

```java
public void run()
{
    while(true)
    {
        if(flag)
        {
            System.out.println("작업할 내용");
        }
        else
        {
            Thread.yield();
        }
    }
}
```

##### 아래 예제는 처음 실행 후 3초 까지 ThreadA와 ThreadB가 동일하게 실행되지만 3초 이후 부터 ThreadB가 더 많이 실행된다. 다시 3초 뒤부터는 동일하게 실행되고 다시 3초 뒤에는 쓰레드들이 종료된다.

```java
public class Main
{
    public static void main(String[] args)
    {
        ThreadA threadA = new ThreadA();
        ThreadB threadB = new ThreadB();
        // 시작후 3초동안 A,B 같이 실행
        threadA.start();
        threadB.start();

        try{ Thread.sleep(3000); } catch(InterruptedException e){}
        // 3초 뒤 threadB만 실행
        threadA.work = false;

        try{ Thread.sleep(3000); } catch(InterruptedException e){}
        // 3초 뒤 A,B 같이 실행
        threadA.work = true;

        try{ Thread.sleep(3000); } catch(InterruptedException e){}
        // 3초 뒤 A,B 모두 종료
        threadA.stop = true;
        threadB.stop = true;
    }
}

class ThreadA extends Thread
{
    public boolean stop = false; // 종료 플래그
    public boolean work = true;  // 작업 진행 여부 플래그

    public void run()
    {
        while(!stop)
        {
            if(work)
            {
                System.out.println("ThreadA 작업중");
            }
            else
            {
                Thread.yield();
            }
        }
        System.out.println("ThreadA 종료");
    }
}

class ThreadB extends Thread
{
    public boolean stop = false;
    public boolean work = true;

    public void run()
    {
        while(!stop)
        {
           if(work)
           {
               System.out.println("ThreadB 작업중");
           }
           else
           {
               Thread.yield();
           }
        }
        System.out.println("ThreadB 종료");
    }
}
```
<br />

## join() - 다른 쓰레드의 종료를 기다림
##### 쓰레드는 다른 쓰레드와 독립적으로 실행하는 것이 기본이지만 다른 쓰레드가 종료될 때 까지 기다렸다가 실행해야 하는 경우가 발생할 수 있다. 예를 들어 계산 작업을 하는 쓰레드가 모든 계산을 마쳤을 때 그 결과값을 받아 이용하는 경우가 이에 해당한다.
##### 이런 경우를 위해 Thread는 join() 메소드를 제공하고 있다.
##### 아래는 1~100까지 계산하는 쓰레드와 그 결과를 받아서 출력하는 메인 쓰레드의 예제다.

```java
public class Main
{
    public static void main(String[] args)
    {
        SumThread sumThread = new SumThread();
        sumThread.start(); // sumThread 시작

        try
        {
            sumThread.join(); // sumThread가 종료될 때 까지 main 쓰레드를 일시 정지시킴
        }
        catch(InterruptedException e) {}

        System.out.println("1~100까지의 합 : " + sumThread.getSum());
    }
}

class SumThread extends Thread
{
    private long sum;

    public long getSum()
    {
        return sum;
    }

    public void setSum(long sum)
    {
        this.sum = sum;
    }

    public void run()
    {
        for(int i=1; i<=100; i++)
        {
            sum+=i;
        }
    }
}
```
<br />

## wait(), notify(), notifyAll() - 쓰레드 간 협업
##### 두 개의 쓰레드를 번갈아가면서 실행해야  할 경우가 있을 수도 있습니다. 정확한 교대 작업이 필요할 경우, 자신의 작업이 끝나면 상대방 쓰레드를 일시 정지 상태에서 풀어주고, 자신은 일시 정지로 만드는 것이다. 이 방법의 핵심은 공유 객체에 있다. 공유 객체는 두 쓰레드가 작업할 내용을 각각 동기화 메소드로 구분해 놓고, 한 쓰레드가 작업을 완료하면 notity() 메소드를 호출해서 일시 정지 상태에 있는 다른 쓰레드를 실행 대기 상태로 만들어 놓고, 자신은 두 번 작업을 하지 않도록 wait() 메소드를 호출하여 일시 정지 상태로 만든다.
##### 만약 wait() 대신 wait(long time)이나, wait(long time, int nanos)를 사용하면 notify()를 사용하지 않아도 지정된 시간이 지나면 쓰레드가 자동적으로 실행 대기 상태가 된다. notyfy() 메소드와 동일한 역할을 하는 notifyAll() 메소드도 있는데, notify()는 wait()에 의해 일시 정지된 쓰레드 중 한 개를 실행 대기 상태로 만들고, notifyAll() 메소드는 wait()에 의해 일시 정지된 모든 쓰레드들을 실행 대기 상태로 만든다. 이 메소드들은 Thread 클래스가 아닌 Object 클래스에 선언된 메소드이므로 모든 공유객체에서 호출이 가능하다. 
##### 주의할 점은 이 메소드들은 동기화 메소드 또는 동기화 블록 내에서만 사용할 수 있다.
<br />

##### 다음 예제는 데이터를 저장하는 쓰레드(생산자)가 데이터를 저장하면, 데이터를 소비하는 쓰레드(소비자)가 데이터를 읽고 처리하는 교대 작업을 구현한 것이다.
##### 생성자는 소비자가 읽기 전에 새로운 데이터를 두 번 생성하면 안되고(setData() 메소드를 두번 실행하면 안 됨), 소비자는 생성자가 새로운 데이터를 생성하기 전에 데이터를 두 번 읽어서도 안된다.(getData() 메소드를 두번 실행하면 안 됨). 
##### 구현 방법은 공유 객체(DataBox)에 데이터를 저장할 수 있는 data 필드의 값이 null이면 생산자를 실행 대기 상태로 만들고, 소비자를 일시 정지 상태로 만드는 것이다. 반대로 data 필드의 값이 null이 아니면 소비자를 실행 대기 상태로 만들고, 생산자를 일시 정지 상태로 만들면 된다.

```java
public class Main
{
    public static void main(String[] args)
    {
        DataBox dataBox = new DataBox();

        Producer producer = new Producer(dataBox);
        Consumer consumer = new Consumer(dataBox);

        producer.start();
        consumer.start();
    }
}

// 두 쓰레드의 작업 내용을 동기화 메소드로 작성한 공유 객체

class DataBox
{
    private String data;

    public synchronized String getData()
    {
        if(this.data == null)
        {
            try
            {
                wait();
            }
            catch(InterruptedException e){}
        }

        String returnValue = data;
        System.out.println("소비자가 읽은 데이터 = " + returnValue);
        data = null;
        notify();
        return returnValue;
    }

    public synchronized void setData(String data)
    {
        if(this.data != null)
        {
            try
            {
                wait();
            }
            catch(InterruptedException e){}
        }

        this.data = data;
        System.out.println("생산자가 생성한 데이터 = " + data);
        notify();
    }
}

// 데이터를 생산하는 쓰레드
class Producer extends Thread
{
    private DataBox dataBox;

    public Producer(DataBox dataBox)
    {
        this.dataBox = dataBox;
    }

    @Override
    public void run()
    {
        for(int i=1; i<=3; i++)
        {
            String data = "Data_" + i;
            dataBox.setData(data);
        }
    }
}

//데이터를 소비하는 쓰레드
class Consumer extends Thread
{
    private DataBox dataBox;

    public Consumer(DataBox dataBox)
    {
        this.dataBox = dataBox;
    }

    @Override
    public void run()
    {
        for(int i=1; i<=3; i++)
        {
            String data = dataBox.getData();
        }
    }
}
```
<br />

## stop플래그, interrupt() - 쓰레드의 안전한 종료
