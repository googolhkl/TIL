# 데몬 쓰레드
##### 데몬 쓰레드는 주 쓰레드의 작업을 돕는 보조적인 역할을 수행하는 쓰레드다. 주 쓰레드가 종료되면 데몬 쓰레드는 강제적으로 자동 종료되는데, 그 이유는 주 쓰레드의 보조 역할을 하기 때문에 주 쓰레드가 종료되면 데몬 쓰레드의 존재 의미가 없어지기 때문이다.
##### 데몬 쓰레드의 적용 예는 워드프로세스의 자동 저장, 미디어 플레이어의 동영상 및 음악 재생, 가비지 컬렉터 등이 있는데, 이 기능들은 주 쓰레드가 종료되면 자동 종료된다.

##### 쓰레드를 데몬으로 만들기 위해서는 주 쓰레드가 데몬이 될 쓰레드의 setDaemon(true)를 호출해 주면 된다. 아래 코드를 보면 메인 쓰레드가 주 쓰레드가 되고 AutoSaveThread가 데몬 쓰레드가 된다.

```java
public static void main(String[] args)
{
    AutoSaveThread thread = new AutoSaveThread();
    thread.setDaemon(true);
    thread.start();
    ...
}
```
<br />
##### 주의할 점은 start() 메소드가 호출되고 나서 setDaemon(true)를 호출하면 `IllegalThreadStateException`이 발생하기 때문에 start() 메소드 호출 전에 setDaemon(true)를 호출해야 한다.
##### 현재 실행 중인 쓰레드가 데몬 쓰레드인지 아닌지 구별방법은 isDaemon()메소드를 호출하면 된다.
##### 다음 예제는 1초 주기로 save() 메소드를 자동 호출하도록 AutoSaveThread를 작성하고, 메인 쓰레드가 3초후 중료되면 AutoSaveThread도 같이 종료되도록 하는 예제다.

```java
public class Main
{
    public static void main(String[] args)
    {
        AutoSaveThread daemon = new AutoSaveThread();
        daemon.setDaemon(true);
        daemon.start();

        try
        {
            Thread.sleep(3000);
        }
        catch(InterruptedException e)
        {
        }
        System.out.println("메인 쓰레드 종료");
    }
}

class AutoSaveThread extends Thread
{
    public void save()
    {
        System.out.println("작업 내용이 저장됐습니다");
    }

    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                Thread.sleep(1000);
            }
            catch(InterruptedException e)
            {
                break;
            }
            save(); // 1초 간격으로 저장
        }
    }
}
```
