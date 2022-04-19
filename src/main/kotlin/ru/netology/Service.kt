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
        val chatWithUsers = chatList.filter { chat ->
            chat.usersId.contains(userId) && chat.usersId.contains(opponentId) && !deletedChats.contains(chat.chatId)
        }//должен найтись 1 активный чат, если чатов нет - создаем чат, если больше 1 - что-то пошло не так
        if (chatWithUsers.size == 1) {
            val chat = chatWithUsers[0]
            chatList[chat.chatId].message += Message(//добавление сообщения в имеющийся чат с оппонентом
                userId, opponentId, chatId, messageId, message
            )
            if (chatList[chat.chatId].lastViewedMessage[0].userId == userId) {//нужно более оптимально определять Id последнего сообщения
                chatList[chat.chatId].lastViewedMessage[0].lastViewedMessageId = messageId
            }
            if (chatList[chat.chatId].lastViewedMessage[1].userId == userId) {
                chatList[chat.chatId].lastViewedMessage[1].lastViewedMessageId = messageId
            }

            messageId++
        } else if (chatWithUsers.isEmpty()) {
            addChat(userId, opponentId, message)
        } else {
            throw QuantityOfChatsExceeded(chatWithUsers.size)
        }
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
        val chatsForUser = chatList.filter { it.usersId.contains(userId) && !deletedChats.contains(it.chatId) }
        if (chatsForUser.isEmpty()) {
            println("Нет сообщений")
        }
        return chatsForUser
    }

    fun getUnreadChatsCount(userId: Int): Int {
// так как запись в return сложночитаема, оставляю исходник
//        for (chat in chatList) {
//            if (chat.usersId.contains(userId) && !deletedChats.contains(chat.chatId)) {//убеждаемся что Id чата нет
//            в списке удаленных и пользователь принимает участие в чате
//                if (chat.lastViewedMessage[0].userId == userId
//                && chat.lastViewedMessage[0].lastViewedMessageId != chat.message.last().messageId) {
// если Id последнего сообщения в чате не соответствует Id последнего прочитанного сообщения - значит были добавлены
// более новые сообщения, соответственно добавляет этот чат в список с непрочтенными сообщениями
//                    getChatsForUser += chat
//                }
//                if (chat.lastViewedMessage[1].userId == userId
//                && chat.lastViewedMessage[1].lastViewedMessageId != chat.message.last().messageId) {
//                    getChatsForUser += chat
//                }
//            }
//        }
//        return getChatsForUser.size
        return getChat(userId).count {
            (it.lastViewedMessage[0].userId == userId
                    && it.lastViewedMessage[0].lastViewedMessageId != it.message.last().messageId)
                    || (it.lastViewedMessage[1].userId == userId
                    && it.lastViewedMessage[1].lastViewedMessageId != it.message.last().messageId)
        }
    }

    fun getNewMessagesByChat(userId: Int, chatId: Int): List<Message> {
        var lastId: Int = 0
        if (deletedChats.contains(chatId)) {//убеждаемся, что чата нет в списке удаленных
            return listOf<Message>()
        }
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
        println(lastId)
        val listOfNewMessages =
            chatList[chatId].message.filter {
                it.messageId > lastId
                        && !deletedMessages.contains(it.messageId) && it.authorId != userId
            }
        if (listOfNewMessages.isEmpty()) {
            println("Нет новых сообщений")
        }
        return listOfNewMessages
    }

    fun deleteMessage(userId: Int, chatId: Int, messageId: Int): Boolean { //удаление сообщений
        var result = false
        try {
            var index = -1
            for (i in 0 until chatList[chatId].message.size) {//находим индекс сообщения в массиве сообщений чата.
                // Нужна подсказка: как по Id сообщения вернуть его индекс из массива сообщений чата, в который оно было
                // добавлено?Или нужно было использовать не mutableList? Я тут делаю перебором, из-за чего возникают
                // лишние строки кода.
                // Вместо этого цикла не использую лямбду намеренно, т.к. будет только одно Int значение индекса,
                // а не лист с 1 или 0 значений
                if (chatList[chatId].message[i].messageId == messageId) {
                    index = i
                    break
                }
            }
            if (chatList[chatId].message[index].authorId == userId && !deletedMessages.contains(messageId)) {
                //проверяем, что сообщение с такими параметрами существует и оно отсутствует в списке удаленных
                deletedMessages += messageId
                println("Удаление сообщения id=$messageId прошло успешно")
                result = true
            } else {
                println("Такое сообщение не найдено.")
            }
            val chatActualMsgList = chatList[chatId].message.filter { !deletedMessages.contains(it.messageId) }
            if (chatActualMsgList.isEmpty()) {
                deleteChat(userId, chatId)
            }
        } catch (e: IndexOutOfBoundsException) {
        }
        return result
    }

    fun deleteChat(userId: Int, chatId: Int): Boolean {
        try {
            if (chatList[chatId].usersId.contains(userId) && !deletedChats.contains(chatList[chatId].chatId)) {
                val messagesForDeleting = chatList[chatId].message.filter { !deletedMessages.contains(it.messageId) }
                for (message in messagesForDeleting) {//проще было сделать одним циклом с if
                    deletedMessages += message.messageId
                }

                deletedChats += chatId

                println("Удаление чата id=$chatId прошло успешно")
                return true
            }
        } catch (e: IndexOutOfBoundsException) {
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