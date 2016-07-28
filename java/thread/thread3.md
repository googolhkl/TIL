# 4. 쓰레드 우선순위
##### 자바의 쓰레드 스케줄링은 우선순위 방식과 라운드 로빈 방식이 있다.
##### 우선순위 방식은 우선순위가 높은 쓰레드가 실행 상태를 더 많이 가지도록 스케줄링하는 것이다.
##### 라운드 로빈 방식은 Time Slice를 정해서 하나의 쓰레드를 정해진 시간만큼 실행하고 다른 쓰레드를 실행하는 것이다.
##### 개발자는 우선순위 방식을 이용할 수 있다. 라운드 로빈방식은 JVM에 의해 정해지기 때문에 제어할 수 없다.
##### 우선순위는 1~10까지 부여되는데 숫자가 높을수룩 우선순위가 높다.
##### 우선순위를 설정하지 않으면 기본적으로 5로 할당된다.
##### 우선순위를 변경하고 싶으면 Thread 클래스의 setPriority()메소드를 이용하면 된다.

```java
thread.setPriority(우선순위);
```

##### 우선순위의 매개값으로 1~10까지의 값을 직접 줘도 되지만, 보통 Thread 클래스의 상수를 이용한다.

```java
thread.setPriority(Thread.MAX_PRIORITY);    // 10
thread.setPriority(Thread.NORM_PRIORITY);   // 5
thread.setPriority(Thread.MIN_PRIORITY);    // 1
```
```java
public class Main
{
    public static void main(String[] args) throws InterruptedException
    {
        for(int i=1; i<=10; i++)
        {
            Thread thread = new CalThread("thread" + i);
            if(i != 10)
            {
                thread.setPriority(Thread.MIN_PRIORITY);
            }
            else
            {
                thread.setPriority(Thread.MAX_PRIORITY);
            }
            thread.start();
        }
    }
}

class CalThread extends Thread
{
    public CalThread(String name)
    {
        setName(name);
    }
    public void run()
    {
        for(int i=0; i<2000000000; i++)
        {
        }
        System.out.println(getName());
    }
}
```
