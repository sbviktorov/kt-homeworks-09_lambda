package ru.netology

data class Chat(
    val chatId: Int,
    val usersId: List<Int>,
    val message: MutableList<Message>,
    val lastViewedMessage: MutableList<LastViewedMessage>,
) {
    override fun toString(): String {
        return "Чат(id=$chatId, содержит ${message.size} сообщений (с учетом удаленных)) между пользователями ${usersId[0]} и ${usersId[1]}"
    }
}