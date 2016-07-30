# 동기화 메소드와 동기화 블록
## 임계영역
##### 멀티쓰레드에서 임계영역의 접근을 할 때 별도의 처리를 하지 않으면 문제가 생길 수 있다.
##### 예를 들어서 User1 쓰레드가 Calculator 객체의 memory 필드에 100을 먼저 저장하고 2초간 일시 정지 상태에 있고, 그 동안 User2 쓰레드가 memory 필드값을 50으로 변경했을 때, 2초가 지나 User1 쓰레드가 다시 실행되어 memory 필드의 값을 출력하면 User2가 저장한 50이 나온다.
##### 아래는 이 예의 소스 코드이다.

```java
public class Main
{
    public static void main(String[] args) throws InterruptedException
    {
        Calculator calculator = new Calculator();
        User1 user1 = new User1();
        user1.setCalculator(calculator);
        user1.start();

        User2 user2 = new User2();
        user2.setCalculator(calculator);
        user2.start();
    }
}

class Calculator
{
    private int memory; // 임계영역

    public int getMemory()
    {
        return memory;
    }

    public void setMemory(int memory)
    {
        this.memory = memory;
        try
        {
            Thread.sleep(2000);
        }
        catch(InterruptedException e){}
        System.out.println(Thread.currentThread().getName() + ": " + this.getMemory());
    }
}

class User1 extends Thread
{
    private Calculator calculator;

    public void setCalculator(Calculator calculator)
    {
        this.setName("User1");
        this.calculator = calculator;
    }

    public void run()
    {
        calculator.setMemory(100);
    }
}

class User2 extends Thread
{
    private Calculator calculator;

    public void setCalculator(Calculator calculator)
    {
        this.setName("User2");
        this.calculator = calculator;
    }

    public void run()
    {
        calculator.setMemory(50);
    }
}
```

## 임계영역 문제 해결
##### 자바는 임계영역을 지정하기 위해 동기화(synchronized) 메소드와 동기화 블록을 제공한다. 쓰레드가 객체 내부의 동기화 메소드 또는 블록에 들어가면 즉시 객체에 잠금을 걸어 다른 쓰레드가 임계영역 코드를 실행하지 못하도록 한다. 
##### 동기화 메소드를 만드는 방법은 다음과 같이 메소드 선언에 synchronized 키워드를 붙이면 된다. synchronized 키워드는 인스턴스와 정적 메소드 어디든 붙일 수 있다.

```java
public synchronized void method()
{
    임계 영역; // 단 하나의 쓰레드만 실행
}
```

##### 동기화 메소드는 메소드 전체 내용이 임계영역이므로 쓰레드가 동기화 메소드를 실행하는 즉시 객체에는 잠금이 일어나고, 쓰레드가 동기화 메소드를 실행 종료하면 잠금이 풀린다. 메소드 전체 내용이 아니라, 일부 내용만 임계영역으로 만들고 싶다면 다음과 같이 동기화 블록을 만들면 된다.

```java
public void method()
{
    // 여러 쓰레드가 실행 가능 영역
    ...
    synchronized(공유객체)
    {
        임계 영역; // 단 하나의 쓰레드만 실행
    }
    // 여러 쓰레드가 실행 가능 영역
    ...
}
```

##### 동기화 블록의 외부 코드들은 여러 쓰레드가 동시에 실행할 수 있지만, 동기화 블록의 내부 코드는 임계영역이므로 한 번에 한 쓰레드만 실행할 수 있고 다른 쓰레드는 실행할 수 없다. 만약 동기화 메소드와 동기화 블록이 여러 개 있을 경우, 쓰레드가 이들 중 하나를 실행할 때 다른 쓰레드는 해당 메소드는 물론이고 다른 동기화 메소드 및 블록도 실행할 수 없다. 하지만 일반 메소드는 실행이 가능하다.
+
##### 다음 예제는 이전 예제에서 문제가 된 공유 객체인 Calculator를 수정한 것이다.

```java
class Calculator
{
    private int memory; // 임계영역

    public int getMemory()
    {
        return memory;
    }

    public synchronized void setMemory(int memory)
    {
        this.memory = memory;
        try
        {
            Thread.sleep(2000);
        }
        catch(InterruptedException e){}
        System.out.println(Thread.currentThread().getName() + ": " + this.getMemory());
    }
}
```

##### 다음과 같이 동기화 블록으로도 만들 수 있다.

```java
class Calculator
{
    private int memory; // 임계영역

    public int getMemory()
    {
        return memory;
    }

    public void setMemory(int memory)
    {
        synchronized (this)
        {
            this.memory = memory;
            try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}
        System.out.println(Thread.currentThread().getName() + ": " + this.getMemory());
        }
    }
}
```

##### 쓰레드가 동기화 블록으로 들어가면 this(Calculator 객체)를 잠그고, 동기화 블록을 실행한다. 동기화 블록은 모두 실행할 때까지 다른 쓰레드들은 this(Calculator 객체)의 모든 동기화 메소드 또는 동기화 블록을 실행할 수 없게 된다.
