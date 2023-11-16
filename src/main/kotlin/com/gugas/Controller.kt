package com.gugas

import com.gugas.event.EventParser
import com.gugas.event.EventProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller {

    @PostMapping("/event", consumes = ["text/csv", MediaType.TEXT_PLAIN_VALUE])
    suspend fun saveEvent(
        @RequestBody data: String
    ) = withContext(Dispatchers.IO) {
        val response = async {
            try {
                val event = EventParser.parseCSV(data)
                EventProcessor.save(System.currentTimeMillis(), event)
                println("Saved event: $event")
                ResponseEntity("Successfully saved the data", HttpStatusCode.valueOf(202))
            } catch (e: Exception) {
                println(e.message)
                ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
            }
        }
        response.await()
    }

    @GetMapping(path = ["/stats"])
    suspend fun getStats() = withContext(Dispatchers.IO) {
        val stats = async {
            try {
                val result = EventProcessor.stats(System.currentTimeMillis()).formatToString()
                ResponseEntity(result, HttpStatus.OK)
            } catch (e: Exception) {
                ResponseEntity(e.message, HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
        stats.await()
    }
}