package ru.netology

import java.text.SimpleDateFormat
import java.util.*

data class Message(
    val authorId: Int,
    val opponentId: Int,
    val chatId: Int,
    val messageId: Int,
    val message: String,
    val time: Long = Date().time
) {

    override fun toString(): String {
        val sdf = SimpleDateFormat("dd.M.yyyy HH:mm:ss")
        return "\nПользователь $authorId в ${(sdf.format(Date(time)))} написал(а): \n$message"
    }
}


