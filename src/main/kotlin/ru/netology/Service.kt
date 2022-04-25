package ru.netology

import ru.netology.exceptions.QuantityOfChatsExceeded

object Service {
    var chatId: Int = 0
    var messageId: Int = 0
    var currentUserId: Int = 101
    var chatList = mutableListOf<Chat>()
    var messageList = listOf<Message>()
    var deletedMessages = listOf<Int>()
    var deletedChats = listOf<Int>()

    fun send(
        userId: Int,
        opponentId: Int,
        message: String
    ) {
        chatList.asSequence()
            .filter { chat ->
                chat.usersId.contains(userId) && chat.usersId.contains(opponentId) && !deletedChats.contains(chat.chatId)
            }//должен найтись 1 активный чат, если чатов нет - создаем чат, если больше 1 - что-то пошло не так
            .let {
                if (it.count() > 1) {
                    throw QuantityOfChatsExceeded(it.count())
                } else it
            }
            .firstOrNull()
            ?.let { chat ->
                chatList[chat.chatId].message += Message(userId, opponentId, chatId, messageId, message)
                if (chatList[chat.chatId].lastViewedMessage[0].userId == userId) {
                    chatList[chat.chatId].lastViewedMessage[0].lastViewedMessageId = messageId
                }
                if (chatList[chat.chatId].lastViewedMessage[1].userId == userId) {
                    chatList[chat.chatId].lastViewedMessage[1].lastViewedMessageId = messageId
                }
                messageId++
            } ?: addChat(userId, opponentId, message)
    }

    private fun addChat(
        userId: Int,
        opponentId: Int,
        message: String
    ) {
        chatList += Chat(
            chatId, listOf(userId, opponentId), mutableListOf(
                Message(
                    userId, opponentId, chatId,
                    messageId, message
                )
            ), lastViewedMessage = mutableListOf(LastViewedMessage(userId, messageId), LastViewedMessage(opponentId, 0))
        )
        chatId++
        messageId++
    }

    fun getChat(userId: Int): List<Chat> {//возвращает список чатов пользователя или "Нет сообщений"
        var getChatList = listOf<Chat>()
        chatList.asSequence()
            .filter { it.usersId.contains(userId) && !deletedChats.contains(it.chatId) }
            .ifEmpty {
                println("Нет сообщений")
                listOf<Chat>().asSequence()
            }
            .let { chat -> getChatList += chat }
        return getChatList
    }

    fun getUnreadChatsCount(userId: Int): Int {
        return getChat(userId).count {
            (it.lastViewedMessage[0].userId == userId
                    && it.lastViewedMessage[0].lastViewedMessageId != it.message.last().messageId)
                    || (it.lastViewedMessage[1].userId == userId
                    && it.lastViewedMessage[1].lastViewedMessageId != it.message.last().messageId)
        }
    }

    fun getNewMessagesByChat(userId: Int, chatId: Int): List<Message> {
        var lastId: Int = 0

        deletedChats.asSequence()
            .filter { it.equals(chatId) }
            .let {
                if (it.count() > 0) {
                    println("Нет новых сообщений")
                    return listOf<Message>()
                } else {
                    if (chatList[chatId].lastViewedMessage[0].userId == userId) { //получаем Id последнего прочитанного
                        // сообщения пользователем
                        lastId = chatList[chatId].lastViewedMessage[0].lastViewedMessageId
                        chatList[chatId].lastViewedMessage[0].lastViewedMessageId =
                            chatList[chatId].message.last().messageId
                    }
                    if (chatList[chatId].lastViewedMessage[1].userId == userId) {
                        lastId = chatList[chatId].lastViewedMessage[1].lastViewedMessageId
                        chatList[chatId].lastViewedMessage[1].lastViewedMessageId =
                            chatList[chatId].message.last().messageId
                    }
                }
            }

        var listOfNewMessages = listOf<Message>()
        chatList[chatId].message.asSequence()
            .filter {
                it.messageId > lastId
                        && !deletedMessages.contains(it.messageId) && it.authorId != userId
            }
            .forEach { listOfNewMessages += it }

        listOfNewMessages.asSequence()
            .let {
                if (it.count() == 0) {
                    println("Нет новых сообщений")
                    return listOf<Message>()
                }
            }
        return listOfNewMessages
    }

    fun deleteMessage(userId: Int, chatId: Int, messageId: Int): Boolean { //удаление сообщений
        var result = false
        try {
            chatList[chatId].message.asSequence()
                .filter { it.messageId == messageId && it.authorId == userId }
                .firstOrNull()
                ?.let {
                    deletedMessages += messageId
                    println("Удаление сообщения id=$messageId прошло успешно")
                    result = true
                } ?: println("Такое сообщение не найдено.")
            chatList[chatId].message.asSequence()
                .filter { !deletedMessages.contains(it.messageId) }
                .let {
                    if (it.count() == 0) {
                        deleteChat(userId, chatId)
                    }
                }

        } catch (_: IndexOutOfBoundsException) {
        }
        return result
    }

    fun deleteChat(userId: Int, chatId: Int): Boolean {
        try {
            if (chatList[chatId].usersId.contains(userId) && !deletedChats.contains(chatList[chatId].chatId)) {
                chatList[chatId].message.asSequence()
                    .filter { !deletedMessages.contains(it.messageId) }
                    .forEach { deletedMessages += it.messageId }
                deletedChats += chatId
                println("Удаление чата id=$chatId прошло успешно")
                return true
            }
        } catch (_: IndexOutOfBoundsException) {
        }
        println("Такой чат не найден.")
        return false
    }

    fun reset() {
        chatId = 0
        messageId = 0
        currentUserId = 101
        chatList = mutableListOf<Chat>()
        messageList = listOf<Message>()
        deletedMessages = listOf<Int>()
        deletedChats = listOf<Int>()
    }
}


