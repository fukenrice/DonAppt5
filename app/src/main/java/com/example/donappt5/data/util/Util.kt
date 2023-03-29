package com.example.donappt5.data.util

import java.util.*

object Util {
    private const val ALLOWED_CHARACTERS =
        "0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM"

    fun getRandomString(sizeOfRandomString: Int): String {
        val random = Random()
        val sb = StringBuilder(sizeOfRandomString)
        for (i in 0 until sizeOfRandomString) sb.append(
            ALLOWED_CHARACTERS[random.nextInt(
                ALLOWED_CHARACTERS.length
            )]
        )
        return sb.toString()
    }
    var FILLING_ALPHABET = 0
    var FILLING_DISTANCE = 1
    var FILLING_SEARCH = 2
    var FILLING_FAVORITES = 3
}