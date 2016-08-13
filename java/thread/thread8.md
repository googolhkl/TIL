# 쓰레드 그룹
##### 쓰레드 그룹은 관련된 쓰레드를 묶어서 관리할 목적으로 이용된다. JVM이 실행되면 system 쓰레드 그룹을 만들고, JVM운영에 필요한 쓰레드들생성해서 system 쓰레드 그룹에 포함시킨다. 그리고 system의 하위 쓰레드 그룹으로 main을 만들고 메인 쓰레드를 main 쓰레드 그룹에 포함시킨다. 쓰레드는 반드시 하나의 쓰레드 그룹에 포함되는데, 명시적으로 쓰레드 그룹에 포함시키지 않으면 기본적으로 자신을 생성한 쓰레드와 같은 쓰레드 그룹에 속하게 된다.
##### 우리가 생성하는 작업 쓰레드는 대부분 main 쓰레드가 생성하므로 기본적으로 main 쓰레드 그룹에 속하게 된다.

## 1. 쓰레드 그룹 이름 얻기
##### 현재 쓰레드가 속한 쓰레드 그룹의 이름을 얻고 싶다면 다음과 같은 코드를 사용할 수 있다.

```java
ThreadGroup group = Thread.currentThread.getThreadGroup();
String groupName = group.getName();
```
<br />

##### Thread의 정적 메소드인 getAllStackTraces()를 이용하면 프로세스 내에서 실행하는 모든 쓰레드에 대한 정보를 얻을 수 있다.
```java
Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
```
<br />

##### getAllStackTraces() 메소드는 Map 타입의 객체를 리턴하는데, 키는 쓰레드 객체이고, 값은 쓰레드의 상태 기록들을 갖고 있는 StackTraceElement[] 배열이다. Map 타입에 대한 자세한 내용은 컬렉션 프레임워크에서 학습한다. 
##### 아래 예제는 현재 실행하고 있는 쓰레드의 이름과 데몬 여부 그리고 속한 쓰레드 그룹 이름이 무엇인지 출력한다.

```java
import java.util.Map;
import java.util.Set;

public class Main
{
    public static void main(String[] args)
    {
       AutoSaveThread autoSaveThread = new AutoSaveThread();
        autoSaveThread.setName("AutoSaveThread"); // 쓰레드 이름 설정
        autoSaveThread.setDaemon(true); // 데몬 쓰레드로 설정
        autoSaveThread.start();

        // 아래 두줄은 컬렉션 프레임워크에서 학습
        Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
        Set<Thread> threads = map.keySet();

        for(Thread thread : threads)
        {
            System.out.println("Name : " + thread.getName() + ((thread.isDaemon()) ? "(데몬)": "(주)"));
            System.out.println("\t" + "소속그룹: " + thread.getThreadGroup().getName());
            System.out.println();
        }
    }
}

class AutoSaveThread extends Thread
{
    public void save()
    {
        System.out.println("작업 내용을 저장함");
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
            save();
        }
    }
}
```
<br />
##### 아래는 실행 결과다.

```
Name : main(주)
    소속그룹: main

Name : Monitor Ctrl-Break(데몬)
    소속그룹: main

Name : Reference Handler(데몬)
    소속그룹: system

Name : AutoSaveThread(데몬)
    소속그룹: main

Name : Signal Dispatcher(데몬)
    소속그룹: system

Name : Finalizer(데몬)
    소속그룹: system
```
<br />

##### 실행 결과를 보면 가비지 컬렉션을 담당하는 Finalizer 쓰레드를 비롯한 일부 쓰레드들이 system 그룹에 속하고, main() 메소드를 실행하는 main 쓰레드는 system 그룹의 하위 그룹인 main에 속하는 것을 볼 수 있다. 그리고 main 쓰레드가 실행시킨 AutoSaveThread는 main 쓰레드가 소속된 main 그룹에 포함되어 있는 것을 볼 수 있다.

## 2. 쓰레드 그룹 생성
##### 지금 까지 만들어져 있는 쓰레드 그룹을 얻기만 했다. 명시적으로 쓰레드르 그룹을 만들고 싶다면 다음 생성자 중 하나를 이용해서 ThreadGroup 객체를 만들면 된다. ThreadGroup 이름만 주거나, 부모 ThreadGroup과 이름을 매개값으로 줄 수 있다.

