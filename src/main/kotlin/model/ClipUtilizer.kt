package model

import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.locks.ReentrantLock
import javax.sound.sampled.Clip
import kotlin.concurrent.withLock

class ClipUtilizer {
    private var isUtilizerClosed = false
    private val lock = ReentrantLock()
    private val lockCondition = lock.newCondition()
    private val utilizationQueue: MutableList<Clip> = LinkedList()

    private val utilizerThread = Thread {
        while (true) {
            lock.withLock {
                lockCondition.await()
                for (clip in utilizationQueue) {
                    clip.stop()
                    clip.flush()
                    clip.close()
                }
                utilizationQueue.clear()
                if (isUtilizerClosed) {
                    return@Thread
                }
            }
        }
    }.apply {
        isDaemon = true
        start()
    }

    fun utilizeClip(clip: Clip) {
        lock.withLock {
            utilizationQueue.add(clip)
            lockCondition.signal()
        }
    }

    fun close() {
        lock.withLock {
            isUtilizerClosed = true
            lockCondition.signal()
        }
        utilizerThread.join()
    }
}