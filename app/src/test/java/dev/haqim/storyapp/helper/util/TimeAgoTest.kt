package dev.haqim.storyapp.helper.util

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.text.SimpleDateFormat
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class TimeAgoTest {
    
    
    private val currentDateTimeString = "2023-02-22T08:30:00.000Z"
    private val fixedCurrentTimeStamp = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", 
        Locale.getDefault()
    )
    .parse(currentDateTimeString)?.time ?: 0
    private val timeAgo = TimeAgo(fixedCurrentTimeStamp)
    


    @Test
    fun `When getTimeAgo receive String as param Expect returns valid date string`(){
        val expected = "just now"
        
        val actual = timeAgo.getTimeAgo(currentDateTimeString)
        
        assertEquals(expected, actual)
    }

    @Test
    fun `When getTimeAgo receive Long as param Expect returns valid date string`(){
        val expected = "just now"

        val actual = timeAgo.getTimeAgo(fixedCurrentTimeStamp)

        assertEquals(expected, actual)
    }

    @Test
    fun `When getTimeAgo receive invalid date String as param Expect returns null`(){
        val expected = null

        val actual = timeAgo.getTimeAgo("2023-02-20")

        assertEquals(expected, actual)
    }

    @Test
    fun `When getTimeAgo receive invalid date Long as param Expect returns null`(){
        val expected = null

        val actual = timeAgo.getTimeAgo(-1L)

        assertEquals(expected, actual)
    }

    @Test
    fun `When getTimeAgo receive datetime String in ahead then current date time Expect returns null`(){
        val expected = null

        val actual = timeAgo.getTimeAgo("2023-02-26T08:30:00.000Z")

        assertEquals(expected, actual)
    }
 
}