Thread test
-------

## 참고 학습 자료
* 도서
  * 모던 자바 인 액션 : http://www.yes24.com/Product/Goods/77125987
  * Do It 코틀린 : http://www.yes24.com/Product/Goods/74035266
* completable
  * https://codechacha.com/ko/java-completable-future/
* 비동기 기술
  * https://jongmin92.github.io/2019/03/31/Java/java-async-1/
* 코루틴 
  * https://kotlinworld.com/139
  * https://nuritech.tistory.com/16
  * https://link.medium.com/QqLQUN3WMlb
* 스케줄링 
  * https://javacan.tistory.com/entry/Reactor-Start-6-Thread-Scheduling
* 자바 executor 
  * https://velog.io/@neity16/Java-8-4-%EC%9E%90%EB%B0%94-Concurrent-Executors-Callable-Future
* 유휴 thread check
  * https://meetup.toast.com/posts/291
  * java 8 에서는 유휴 thread 를 일정 기간 후 종료 시키지만 11 버전 이후 유휴 thread 가 남는 문제가 있음


## Kotest(test) 참고
* kotest
  * https://kotest.io/
  * https://techblog.woowahan.com/5825/

## 코루틴 Dispatchers 참고
* Dispatchers.Main - Android 메인 스레드에서 코루틴을 실행하는 디스패처. 이 디스패처는 UI와 상호작용하는 작업을 실행하기 위해서만 사용해야 한다.
* Dispatchers.IO - 디스크 또는 네트워크 I/O 작업, 블로킹 동작이 많은 경우 최적화되어 있는 디스패처.
* Dispatchers.Default - CPU를 많이 사용하는 작업을 기본 스레드 외부에서 실행하도록 최적화되어 있는 디스패처. 정렬 작업이나 JSON 파싱 작업 등에 최적화 되어 있다.

## 스케줄링 참고
* Schedulers.immediate() : 현재 쓰레드에서 실행한다.
* Schedulers.single() : 쓰레드가 한 개인 쓰레드 풀을 이용해서 실행한다. 즉 한 쓰레드를 공유한다.
* Schedulers.elastic() : 쓰레드 풀을 이용해서 실행한다. 블로킹 IO를 리액터로 처리할 때 적합하다. 쓰레드가 필요하면 새로 생성하고 일정 시간(기본 60초) 이상 유휴 상태인 쓰레드는 제거한다. 데몬 쓰레드를 생성한다.
  → boundedElastic() 가 나온 이후로는 잘 사용하지 않는다.
* Schedulers.parallel() : 고정 크기 쓰레드 풀을 이용해서 실행한다. 병렬 작업에 적합하다.
  → cpu 코어 수 만큼 워커를 생산 한다.