# 2. Thread 하위 클래스로부터 생성
#####다음은 Thread 하위 클래스로부터 생성하는 2가지 방법이다.
## 1. 첫 번째 방법

```java
import java.awt.*;

class BeepThread extends Thread
{
    @Override
    public void run()
    {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        for(int i=0; i<5; i++)
        {
            toolkit.beep();
            try
            {
                Thread.sleep(500);
            }
            catch(Exception e)
            {

            }
        }
    }
}

public class Main
{
    public static void main(String[] args) throws InterruptedException
    {
        Thread thread = new BeepThread();
        thread.start();

        for(int i=0; i<5; i++)
        {
            System.out.println("띵");
            try
            {
                Thread.sleep(500);
            }
            catch(Exception e)
            {

            }
        }
    }
}
```

## 2. 두 번째 방법
```java
import java.awt.*;

public class Main
{
    public static void main(String[] args) throws InterruptedException
    {
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                for(int i=0; i<5; i++)
                {
                    toolkit.beep();
                    try
                    {
                        Thread.sleep(500);
                    }
                    catch(Exception e)
                    {

                    }
                }
            }
        };
        thread.start();

        for(int i=0; i<5; i++)
        {
            System.out.println("띵");
            try
            {
                Thread.sleep(500);
            }
            catch(Exception e)
            {

            }
        }
    }
}
```

# 3. 쓰레드의 이름
##### 쓰레드는 자신의 이름을 가지고 있다. 식별용으로 쓰인다.
##### 메인 쓰레드는 "main"이라는 이름을 가지고 있고, 사용자가 직접 생성한 쓰레드는 "Thread-n"이라는 이름으로 설정된다.
##### 다른 이름으로 설정하고 싶다면 Thread클래스의 setName()메소드를 이용하면 된다.

```java
thread.setName("쓰레드 이름");
```

##### 반대로 쓰레드의 이름을 알고 싶은 경우 getName()메소드를 이용하면 된다.

```java
thread.getName();
```

##### 만약 쓰레드 참조를 가지고 있지 않다면, Thread의 정적 메소드인 currentThread()로 코드를 실행하는 현재 쓰레드의 참조를 얻을 수 있다.

```java
Thread thread = Thread.currentThread();
``` 

```java
public class Main
{
    public static void main(String[] args) throws InterruptedException
    {
        Thread mainThread = Thread.currentThread();
        System.out.println("프로그램 시작 쓰레드 이름 : " + mainThread.getName());

        ThreadA threadA = new ThreadA();
        System.out.println("작업 쓰레드 이름 : " + threadA.getName());
        threadA.start();

        ThreadB threadB = new ThreadB();
        System.out.println("작업 쓰레드 이름 : " + threadB.getName());
        threadB.start();
    }
}

class ThreadA extends Thread
{
    public ThreadA()
    {
        setName("ThreadA"); // 쓰레드 이름 설정
    }

    public void run()
    {
        for(int i=0; i<2; i++)
        {
            System.out.println(getName() + "가 출력한 내용");
        }
    }
}

class ThreadB extends Thread
{
    public ThreadB()
    {
        setName("ThreadB");
    }

    public void run()
    {
        for(int i=0; i<2; i++)
        {
            System.out.println(getName() + "가 출력한 내용");
        }
    }
}
```


