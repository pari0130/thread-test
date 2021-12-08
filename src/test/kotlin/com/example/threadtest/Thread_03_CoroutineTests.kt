package com.example.threadtest

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CancellationException
import java.util.concurrent.ForkJoinPool

@SpringBootTest
class Thread_03_CoroutineTests : BehaviorSpec(){

    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf

    companion object {
        var memberService = MemberService()
        var utils = Utils()
        val logger = LoggerFactory.getLogger(this::class.java)
        var parallelism = ForkJoinPool.commonPool().parallelism
        var futurePool = ForkJoinPool(parallelism)
        var timeoutSec = 60
        var exception = ""
    }

    init {
        this.given("JAVA 병렬 테스트 - Executors") {
            `when`("병렬처리 parallelism 개수를 commonPool 개수로 지정한다") {
                parallelism = ForkJoinPool.commonPool().parallelism
                then("parallelism 개수가 0개 이상이 되어야 한다"){
                    parallelism shouldBeGreaterThan 0 // >= 0
                }
            }

            `when`("병렬처리 parallelism 개수를 availableProcessors 개수로 지정한다") {
                parallelism = Runtime.getRuntime().availableProcessors()
                then("parallelism 개수가 0개 이상이 되어야 한다"){
                    parallelism shouldBeGreaterThan 0 // >= 0
                }
            }

            `when`("병렬처리 ForkJoinPool 개수 를 확인한다") {
                then("ForkJoinPool 개수가 0개 이상이 되어야 한다"){
                    futurePool.parallelism shouldBeGreaterThan 0 // >= 0
                }
            }

            `when`("모든 작업이 정상 종료 될 경우") {
                // SupervisorJob 을 통해 부모, 자식 job 간의 exception 영향도를 피할 수 있음
                val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
                val taskList = ArrayList<Long>()
                val task1 = scope.async { blockingTask((Dto(utils.random(), "tester-1"))) }
                val task2 = scope.async { blockingTask((Dto(utils.random(), "tester-2"))) }
                val task3 = scope.async { blockingTask((Dto(utils.random(), "tester-3"))) }
                val task4 = scope.async { blockingTask((Dto(utils.random(), "tester-4"))) }

                // launch, suspend 를 통해 코루틴 블록을 생성 후 현재 스레드 차단없이 스레드 작업 공간을 공유
                scope.launch {
                    taskList.addAll(listOf(task1.await(), task2.await(), task3.await(), task4.await()))
                    logger.info("[TEST] task wait -> $taskList")
                }.join()

                then("작업 목록 size 가 task 개수와 같아야 한다."){
                    taskList.size shouldBe 4 // >= 0
                }
            }

            `when`("타임아웃을 1초로 설정하여 초과 될 경우" ) {
                timeoutSec = 1
                // SupervisorJob 을 통해 부모, 자식 job 간의 exception 영향도를 피할 수 있음
                val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
                val taskList = ArrayList<Long>()
                val task1 = scope.async { blockingTask((Dto(utils.random(), "tester-1"))) }
                val task2 = scope.async { blockingTask((Dto(utils.random(), "tester-2"))) }
                val task3 = scope.async { blockingTask((Dto(utils.random(), "tester-3"))) }
                val task4 = scope.async { blockingTask((Dto(utils.random(), "tester-4"))) }
                try {
                    withTimeout(timeoutSec.toLong()) {
                        // launch, suspend 를 통해 코루틴 블록을 생성 후 현재 스레드 차단없이 스레드 작업 공간을 공유
                        scope.launch {
                            taskList.addAll(listOf(task1.await(), task2.await(), task3.await(), task4.await()))
                            logger.info("[TEST] task wait -> $taskList")
                        }.join()
                    }
                } catch (e : CancellationException) {
                    exception = e.message.toString()
                }

                then("exception message 가 정상 출력되어야 한다."){
                    logger.info("[TEST] exception message -> $exception")
                    exception.length shouldBeGreaterThan 0 // >= 0
                }
            }
        }
    }

    /**
     * suspend 를 사용할 경우 1개의 thread 를 block 하지 않고 유휴 thread 상태를 다른 작업과 공유 하므로 효율적인 처리가 가능
     * */
    suspend fun blockingTask(dto: Dto): Long {
        return memberService.getMemberBlockingTask(dto)
    }
}