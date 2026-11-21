import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

// Task 1: Start two coroutines and observe their completion order.
fun task1() = runBlocking {
    launch {
        delay(200)
        println("A done")
    }

    launch {
        delay(100)
        println("B done")
    }

    println("Start")
}

// Task 2: Try fetching a name up to n times.
var fetchCount = 0

suspend fun fetchName(): String? {
    delay(100)
    fetchCount++
    return if (fetchCount <= 2) null else "Ada"
}

suspend fun showUser(n: Int) {
    for (k in 1..n) {
        val name = fetchName()
        if (name != null) {
            println("User: $name")
            return
        }
        println("try $k failed")
    }

    println("Server is down, show cached user")
}

fun task2() = runBlocking {
    println("Run with n = 3:")
    showUser(3)

    fetchCount = 0
    println("Run with n = 2:")
    showUser(2)
}

// Task 3: Cancel a running job and run cleanup code.
fun task3() = runBlocking {
    val job = launch {
        try {
            for (i in 0..4) {
                println("tick $i")
                delay(100)
            }
        } finally {
            println("cleanup")
        }
    }

    delay(250)
    job.cancelAndJoin()
    println("isCancelled=${job.isCancelled}")
}

// Task 4: Load a name and age at the same time with async.
suspend fun loadName(): String {
    delay(100)
    return "Ada"
}

suspend fun loadAge(): Int {
    delay(100)
    return 30
}

fun task4() = runBlocking {
    val time = measureTimeMillis {
        val name = async { loadName() }
        val age = async { loadAge() }

        println("Name: ${name.await()}")
        println("Age: ${age.await()}")
    }
    println("Time: $time ms")
}

// Task 5: Print the thread used by each dispatcher.
fun task5() = runBlocking {
    launch {
        println("Inherited: ${Thread.currentThread().name}")
    }.join()

    launch(Dispatchers.Default) {
        println("Default: ${Thread.currentThread().name}")
    }.join()

    launch(Dispatchers.IO) {
        println("IO: ${Thread.currentThread().name}")
    }.join()

    launch(Dispatchers.Unconfined) {
        println("Unconfined: ${Thread.currentThread().name}")
        delay(50)
        println("Unconfined after delay: ${Thread.currentThread().name}")
    }.join()

    @OptIn(kotlin.coroutines.ExperimentalCoroutinesApi::class)
    val singleThread = newSingleThreadContext("MyThread")
    try {
        launch(singleThread) {
            println("Single thread: ${Thread.currentThread().name}")
            delay(50)
            println("Single thread after delay: ${Thread.currentThread().name}")
        }.join()
    } finally {
        singleThread.close()
    }
}

fun main() {
    task1()
    task2()
    task3()
    task4()
    task5()
}
