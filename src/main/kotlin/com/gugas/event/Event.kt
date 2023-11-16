package com.gugas.event

import com.gugas.XIllegalArgumentException
import java.math.BigInteger

class Event(
    val timestamp: Long,
    val x: Float,
    val y: Int
) {
    //𝑥: A real number with a fractional part of up to 10 digits, always in 0..1.
    private val xRange = 0.0..1.0
    //𝑦: An integer in 1,073,741,823..2,147,483,647.
    private val yRange = Integer.valueOf(1_073_741_823)..Integer.valueOf(2_147_483_647)

    init {
        require(x in xRange) { XIllegalArgumentException("X must be in 0..1, but received $x") }
        require(y in yRange) { XIllegalArgumentException("Y must be in 1,073,741,823..2,147,483,647, but received $y") }
    }
}

data class EventAgg(
    val xSum: Double,
    val ySum: BigInteger,
    val num: Int
)