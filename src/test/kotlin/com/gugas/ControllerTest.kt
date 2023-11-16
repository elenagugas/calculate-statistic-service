package com.gugas

import com.gugas.event.EventProcessor
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.web.client.HttpClientErrorException.BadRequest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class ControllerTest {
    @Autowired
    private lateinit var restTemplateBuilder: RestTemplateBuilder

    @LocalServerPort
    var port = 0

    val eventEndpoint by lazy { "http://localhost:$port/event" }
    val statsEndpoint by lazy { "http://localhost:$port/stats" }

    @BeforeEach
    fun setUp() {
        EventProcessor.data.clear()
    }

    @Test
    fun `Should return zeros when no events`() {
        val response = restTemplateBuilder.build()
            .getForEntity("http://localhost:$port/stats", String::class.java)

        val expectedBody = "0,0.0000000000,0.0000000000,0,0.0000000000"

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatusCode.valueOf(200))
        Assertions.assertThat(response.body).isEqualTo(expectedBody)
    }

    @Test
    fun `Should accept valid data`() {
        val request = "1007341341814,0.0442672968,1282509067"
        val response = restTemplateBuilder.build().postForEntity(eventEndpoint, request, String::class.java)

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatusCode.valueOf(202))
    }

    @Test
    fun `Should throw exception if x out of range`() {
        val request = "1007341341814,1.0442672968,1282509067"
        assertThrows<BadRequest> {
            restTemplateBuilder.build().postForEntity(eventEndpoint, request, String::class.java)
        }
    }

    @Test
    fun `Should throw exception if y out of range`() {
        val request = "1007341341814,0.0442672968,12825090670000"
        assertThrows<BadRequest> {
            restTemplateBuilder.build().postForEntity(eventEndpoint, request, String::class.java)
        }
    }

    @Test
    fun `Should return valid stats`() {
        val now = System.currentTimeMillis()
        val input = listOf(
            "${now + 1},0.0442672968,1282509067",
            "${now + 2},0.0442672968,1282509067",
            "${now + 3},0.0442672968,1282509067",
        )
        insertData(input)
        val response = restTemplateBuilder.build().getForEntity(statsEndpoint, String::class.java)
        val expectedBody = "3,0.1328018904,0.0442672968,3847527201,1282509067.0000000000"

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        Assertions.assertThat(response.body).isEqualTo(expectedBody)
    }

    @Test
    fun `Should return valid stats if we get data with the same timestamp`() {
        val now = System.currentTimeMillis()
        val input = listOf(
            "${now},0.0442672968,1282509067",
            "${now},0.0442672968,1282509067",
            "${now},0.0442672968,1282509067",
        )
        insertData(input)
        val response = restTemplateBuilder.build().getForEntity(statsEndpoint, String::class.java)
        val expectedBody = "3,0.1328018904,0.0442672968,3847527201,1282509067.0000000000"

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        Assertions.assertThat(response.body).isEqualTo(expectedBody)
    }

    private fun insertData(input: List<String>) {
        input.forEach {
            restTemplateBuilder.build().postForEntity(eventEndpoint, it, String::class.java)
        }

    }
}