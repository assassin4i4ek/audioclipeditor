package model

import java.util.concurrent.LinkedBlockingQueue
import javax.sound.sampled.Clip

class ClipUtilizer {
    private val blockingQueue = LinkedBlockingQueue<Clip>()
    private val utilizerThread = Thread {
        val clip = blockingQueue.take()
        clip.stop()
        clip.flush()
        clip.close()
    }.apply { isDaemon = true }

    fun utilizeClip(clip: Clip) {
        blockingQueue.add(clip)
    }

    fun close() {
        utilizerThread.interrupt()
    }
}