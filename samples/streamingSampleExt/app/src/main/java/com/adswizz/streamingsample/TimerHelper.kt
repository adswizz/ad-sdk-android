package com.adswizz.streamingsample

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class TimerHelper(private val period: Duration, private val onTimePassed: (Duration) -> Unit) {

    private val scope = CoroutineScope(Dispatchers.Default)

    private var currentTimerJob : Job? = null
    private var previousOffset : Duration = Duration.ZERO
    private var lastDuration : Duration = Duration.ZERO
    private val isStarted: AtomicBoolean = AtomicBoolean(false)
    private val isCancelled: AtomicBoolean = AtomicBoolean(false)

    fun start() {
        if (isStarted.get() || isCancelled.get()) return
        isStarted.set(true)
        startInternal(onTimePassed)
    }

    fun pause(){
        // check if isAlreadyPaused
        if (!isStarted.get()) return

        previousOffset += lastDuration
        currentTimerJob?.cancel()
        currentTimerJob = null
        isStarted.set(false)
    }

    fun stop(){
        isCancelled.set(true)
        scope.cancel()
        currentTimerJob = null
    }

    private fun startInternal(onTimePassed: (Duration) -> Unit) {
        val start = System.currentTimeMillis()
        currentTimerJob = scope.launch {

            tickerFlow(period)
                .collect{
                    val now = System.currentTimeMillis()
                    launch(Dispatchers.Main) {
                        val timePassed = (now - start).toDuration(DurationUnit.MILLISECONDS)
                        lastDuration = timePassed
                        onTimePassed(timePassed + previousOffset)
                    }

                }
        }
    }


    private fun tickerFlow(period: Duration) = flow {
        while (true) {
            emit(Unit)
            delay(period)
        }
    }
}