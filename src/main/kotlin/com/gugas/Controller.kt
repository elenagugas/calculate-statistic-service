package com.gugas

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                val event = EventProcessor.parse(data)
                EventProcessor.save(System.currentTimeMillis(), event)
                println("Saved event: $event")
                ResponseEntity("Successfully saved the data", HttpStatusCode.valueOf(202))
            }
            response.await()
        }

        @GetMapping(path = ["/stats"])
        suspend fun getStats(): String = withContext(Dispatchers.IO) {
            val stats = async {
                EventProcessor.stats(System.currentTimeMillis()).formatToString()
            }
            stats.await()
        }
}