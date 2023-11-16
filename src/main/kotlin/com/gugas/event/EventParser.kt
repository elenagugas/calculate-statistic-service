package com.gugas.event

import com.gugas.TimestampIllegalArgumentException
import com.gugas.XIllegalArgumentException
import com.gugas.YIllegalArgumentException

object EventParser {

    fun parseCSV(data: String): Event {
        val params = data.split(",")
        val timestamp = params[0].toLongOrNull()
            ?: throw TimestampIllegalArgumentException(
                "Timestamp must be a Unix timestamp in millisecond resolution, but received ${params[0]}"
            )

        val x = params[1].toFloatOrNull()
            ?: throw XIllegalArgumentException("Couldn't parse x, received: ${params[1]}")
        val y = params[2].toIntOrNull()
            ?: throw YIllegalArgumentException("Couldn't parse y, received ${params[2]}")
        return Event(timestamp, x, y)
    }

}