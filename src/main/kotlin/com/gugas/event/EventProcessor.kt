package com.gugas.event

import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap

data class StatsResponse(
    val num: Int = 0,
    val xSum: Double = 0.0,
    val xAvg: Double = 0.0,
    val ySum: BigInteger = BigInteger.ZERO,
    val yAvg: Double = 0.0,
) {
    fun formatToString(): String {
        return "$num,${formatDouble(xSum)},${formatDouble(xAvg)},$ySum,${formatDouble(yAvg)}"
    }

    private fun formatDouble(value: Double): String {
        return String.format("%.10f", value)
    }
}

object EventProcessor {

    private const val LIVE_MILLISECONDS = 60 * 1000

    internal val data = ConcurrentHashMap<Long, EventAgg>()

    fun save(currentTimeMillis: Long, event: Event) {
        if (currentTimeMillis - event.timestamp < LIVE_MILLISECONDS) {
            saveValue(currentTimeMillis, Event(event.timestamp, event.x, event.y))
        }
    }

    fun stats(currentTimeMillis: Long): StatsResponse {
        var num = 0
        var xSum = 0.0
        var ySum: BigInteger = BigInteger.ZERO
        cleanOldValues(currentTimeMillis)

        data.forEach {
            num += it.value.num
            xSum += it.value.xSum
            ySum += BigInteger.valueOf(it.value.ySum.toLong())
        }

        if (num == 0) {
            return StatsResponse()
        }

        return StatsResponse(
            num,
            xSum,
            xSum / num,
            ySum,
            ySum.toDouble() / num
        )
    }

    private fun saveValue(currentTimeMillis: Long, event: Event) {
        cleanOldValues(currentTimeMillis)
        val eventAgg = data.getOrDefault(event.timestamp, null)

        data[event.timestamp] = EventAgg(
            (eventAgg?.xSum ?: 0.0).plus(event.x),
            (eventAgg?.ySum ?: BigInteger.ZERO).plus(event.y.toBigInteger()),
            (eventAgg?.num ?: 0) + 1
        )
    }

    private fun cleanOldValues(currentTimeMillis: Long) {
        val minAcceptableTimestamp = currentTimeMillis - LIVE_MILLISECONDS
        data.keys.removeIf { it < minAcceptableTimestamp }
    }
}