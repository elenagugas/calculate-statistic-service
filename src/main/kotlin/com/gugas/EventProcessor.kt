package com.gugas

import java.util.concurrent.ConcurrentHashMap

data class Event(
    val timestamp: Long,
    val x: Double,
    val y: Int
)

data class StatsResponse(
    val num: Int = 0,
    val xSum: Double = 0.0,
    val xAvg: Double = 0.0,
    val ySum: Long = 0,
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

    //𝑥: A real number with a fractional part of up to 10 digits, always in 0..1.
    private const val X_MIN = 0
    private const val X_MAX = 1

    //𝑦: An integer in 1,073,741,823..2,147,483,647.
    private const val Y_MIN = 1_073_741_823
    private const val Y_MAX = 2_147_483_647

    private val data = ConcurrentHashMap<Int, Event>()

    fun parse(data: String): Event {
        val params = data.split(",")
        val timestamp = params[0].toLongOrNull()
            ?: throw TimestampIllegalArgumentException(
                "Timestamp must be a Unix timestamp in millisecond resolution, but received ${params[0]}"
            )

        val x = params[1].toDoubleOrNull()
        if (x == null || X_MAX <= x || x <= X_MIN) {
            throw XIllegalArgumentException("X must be in 0..1, but received ${params[1]}")
        }
        val y = params[2].toIntOrNull()
        if (y == null || Y_MAX <= y || y <= Y_MIN) {
            throw YIllegalArgumentException("Y must be in 1,073,741,823..2,147,483,647, but received ${params[2]}")
        }
        return Event(timestamp, x, y)
    }

    fun save(currentTimeMillis: Long, event: Event) {
        cleanOldValues(currentTimeMillis)
        if (currentTimeMillis - event.timestamp < LIVE_MILLISECONDS) {
            saveValue(Event(event.timestamp, event.x, event.y))
        }
    }

    fun stats(currentTimeMillis: Long): StatsResponse {
        synchronized(data) {
            cleanOldValues(currentTimeMillis)
            val num = data.count()

            if (num == 0) {
                return StatsResponse()
            }

            val xSum = data.map { it.value.x }.sumOf { it }
            val ySum = (data.map { it.value.y.toLong() }.sumOf { it })

            return StatsResponse(
                num,
                xSum,
                xSum / num,
                ySum,
                ySum.toDouble() / num
            )
        }
    }

    private fun saveValue(event: Event) {
        //TODO process clashes
        val id: Int = (event.timestamp % LIVE_MILLISECONDS).toInt()
        data[id] = event
    }

    private fun cleanOldValues(currentTimeMillis: Long) {
        val minAcceptableTimestamp = currentTimeMillis - LIVE_MILLISECONDS
        data.forEach { (key, event) ->
            if (event.timestamp < minAcceptableTimestamp) {
                data.remove(key)
            }
        }
    }
}