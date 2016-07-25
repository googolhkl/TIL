### 1. Thread 클래스로 부터 직접 생성
#####다음은 Thread 클래스로 부터 직접생성하는 3가지 방법이다.
#### 1. 기본 생성 방법

```java
import java.awt.*;

class BeepTask implements Runnable
{
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
        Runnable beepTask = new BeepTask();
        Thread thread = new Thread(beepTask);
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

#### 2. 익명객체를 사용한 방법
```java
import java.awt.*;


public class Main
{
    public static void main(String[] args) throws InterruptedException
    {
        Thread thread = new Thread(new Runnable()
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
        });
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

#### 3. 람다식을 사용한 방법
```java
import java.awt.*;

public class Main
{
    public static void main(String[] args) throws InterruptedException
    {
        Thread thread = new Thread( () ->
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
        });
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
