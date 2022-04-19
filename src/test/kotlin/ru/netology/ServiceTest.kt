package ru.netology

import org.junit.After
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import ru.netology.exceptions.QuantityOfChatsExceeded

class ServiceTest {
    @Before
    fun reset() {
        Service.reset()
    }

    private fun addMessages() {
        //чат[0] между 101 и 202
        val msg1 = Service.send(101, 202, "Ghbdtn")
        Thread.sleep(30)
        val msg2 = Service.send(101, 202, "Привет")
        Thread.sleep(30)
        val msg3 = Service.send(202, 101, "Ну, привет")
        Thread.sleep(30)
        val msg4 = Service.send(101, 202, "Как дела?")
        Thread.sleep(30)
        //чат[1] между 101 и 303
        val msg5 = Service.send(101, 303, "Здарова")
        Thread.sleep(30)
        //чат[2] между 303 и 202
        val msg6 = Service.send(303, 202, "Hello")
        Thread.sleep(30)
        val msg7 = Service.send(202, 303, "Hi")
        Thread.sleep(30)
        val msg8 = Service.send(202, 303, "How are you?")
        Thread.sleep(30)
        val msg9 = Service.send(202, 303, "I'm fine")
    }

    @Test
    fun send() {
        addMessages()
        val expectedChatListSize = 3
        val expectedChat0Size = 4
        val expectedChat1Size = 1
        val expectedChat2Size = 4
        val expectedMsgId = 9 //9 т.к. новый Id вычисляется после присвоения предыдущего
        assertTrue(
            Service.chatList[2].usersId.contains(202) && Service.chatList[2].usersId.contains(303)
                    && !Service.deletedChats.contains(
                Service.chatList[2].chatId
            )
        )
        assertEquals(expectedChatListSize, Service.chatList.size)
        assertEquals(expectedChat0Size, Service.chatList[0].message.size)
        assertEquals(expectedChat1Size, Service.chatList[1].message.size)
        assertEquals(expectedChat2Size, Service.chatList[2].message.size)
        assertEquals(expectedMsgId, Service.messageId)
    }

    @Test(expected = QuantityOfChatsExceeded::class)
    fun sendThrowException() {
        addMessages()
        Service.deleteChat(101, 0)
        Service.send(101, 202, "Создается новый чат после удаления предыдущего")
        Service.deletedChats =
            listOf() //обнуляем список удаленных чатов, теперь для пользователей 101 и 202 найдется 2 активных чата,
        // что должно вызвать ошибку. Вообще это нереальный исход и больше созадн для самопроверки кода.
        Service.send(101, 202, "Сообщение")//создаем сообщение для выова ошибки
    }

    @Test
    fun getChat() {
        addMessages()
        Service.deleteChat(202, 2) // для пользователей 202 и 303 должен находиться только один чат
        val expectedChat0Id = 0
        val expectedChat1Id = 1
        val expectedChat2Id = 2
        val chatsFor101 = Service.getChat(101)
        val chatsFor202 = Service.getChat(202)
        val chatsFor303 = Service.getChat(303)
        val expectedChatsFor101Size = 2
        val expectedChatsFor202Size = 1
        val expectedChatsFor303Size = 1
        assertEquals(expectedChatsFor101Size, chatsFor101.size)
        assertEquals(expectedChat0Id, chatsFor101[0].chatId)
        assertEquals(expectedChat1Id, chatsFor101[1].chatId)
        assertEquals(expectedChatsFor202Size, chatsFor202.size)
        assertEquals(expectedChat0Id, chatsFor202[0].chatId)
        assertEquals(expectedChatsFor303Size, chatsFor303.size)
        assertEquals(expectedChat1Id, chatsFor303[0].chatId)
    }

    @Test
    fun getUnreadChatsCount() {
        addMessages()
        val expectedUnreadChatsCountFor101 = 0
        val expectedUnreadChatsCountFor202 = 1
        val expectedUnreadChatsCountFor303 = 2
        assertEquals(expectedUnreadChatsCountFor101, Service.getUnreadChatsCount(101))
        assertEquals(expectedUnreadChatsCountFor202, Service.getUnreadChatsCount(202))
        assertEquals(expectedUnreadChatsCountFor303, Service.getUnreadChatsCount(303))
        Service.send(303, 101, "new message")
        Service.send(505, 101, "new message")
        val newExpectedUnreadChatsCountFor101 = 2
        assertEquals(newExpectedUnreadChatsCountFor101, Service.getUnreadChatsCount(101))

    }

    @Test
    fun getNewMessagesByChat() {
        addMessages()
        val expectedCountOfNewMessagesFor101InChat0 = 0
        val expectedCountOfNewMessagesFor202InChat0 = 1
        val expectedCountOfNewMessagesFor101InChat1 = 0
        val expectedCountOfNewMessagesFor303InChat1 = 1
        val expectedCountOfNewMessagesFor202InChat2 = 0
        val expectedCountOfNewMessagesFor303InChat2 = 3
        assertEquals(expectedCountOfNewMessagesFor101InChat0, Service.getNewMessagesByChat(101, 0).size)
        assertEquals(expectedCountOfNewMessagesFor202InChat0, Service.getNewMessagesByChat(202, 0).size)
        assertEquals(expectedCountOfNewMessagesFor101InChat1, Service.getNewMessagesByChat(101, 1).size)
        assertEquals(expectedCountOfNewMessagesFor303InChat1, Service.getNewMessagesByChat(303, 1).size)
        assertEquals(expectedCountOfNewMessagesFor202InChat2, Service.getNewMessagesByChat(202, 2).size)
        assertEquals(expectedCountOfNewMessagesFor303InChat2, Service.getNewMessagesByChat(303, 2).size)
        val newExpectedCountOfNewMessagesFor303InChat1 =
            0//после предыдущей загрузки новых сообщений список должен быть пуст
        assertEquals(newExpectedCountOfNewMessagesFor303InChat1, Service.getNewMessagesByChat(303, 1).size)
    }

    @Test
    fun getNewMessagesByDeletedChat() {
        addMessages()
        Service.deleteChat(101,0)
        val expectedCountOfNewMessagesFor101InChat0 = 0
        val expectedCountOfNewMessagesFor202InChat0 = 0
        assertEquals(expectedCountOfNewMessagesFor101InChat0, Service.getNewMessagesByChat(101, 0).size)
        assertEquals(expectedCountOfNewMessagesFor202InChat0, Service.getNewMessagesByChat(202, 0).size)
    }

    @Test
    fun deleteMessage() {
        addMessages()
        val expectedSizeOfChatsFor202 = 2
        val actualSizeOfChatsFor202 = Service.getChat(202).size
        assertEquals(expectedSizeOfChatsFor202, actualSizeOfChatsFor202)

        Service.deleteMessage(202, 2, 8)
        Service.deleteMessage(202, 2, 7)
        Service.deleteMessage(202, 2, 6)
        Service.deleteMessage(303, 2, 5)
        Service.deleteMessage(101, 0, 3)
        Service.deleteMessage(202, 0, 2)
        Service.deleteMessage(101, 0, 1)
        Service.deleteMessage(101, 0, 0)
        val expectedSizeOfChatsFor202Upd = 0
        val actualSizeOfChatsFor202Upd = Service.getChat(202).size
        assertEquals(expectedSizeOfChatsFor202Upd, actualSizeOfChatsFor202Upd)

        Service.send(101, 202, "сообщение")
        Service.send(202, 909, "сообщение")
        Service.send(303, 202, "сообщение")
        val expectedSizeOfChatsFor202Upd1 = 3
        val actualSizeOfChatsFor202Upd1 = Service.getChat(202).size
        assertEquals(expectedSizeOfChatsFor202Upd1, actualSizeOfChatsFor202Upd1)
    }

    @Test
    fun deleteMessageWithoutException() {
        addMessages()
        assertFalse(Service.deleteMessage(101, 5, 0))
        assertFalse(Service.deleteMessage(111, 0, 0))
        assertFalse(Service.deleteMessage(101, 0, 999))
    }

    @Test
    fun deleteChat() {
        addMessages()
        Service.deleteChat(101, 0)
        val expectedChatsFor101 = 1
        assertEquals(expectedChatsFor101, Service.getChat(101).size)
        val expectedChatsFor101Upd1 = 0
        Service.deleteChat(303, 1)
        assertEquals(expectedChatsFor101Upd1, Service.getChat(101).size)
    }

    @Test
    fun deleteChatWithoutException() {
        addMessages()
        assertFalse(Service.deleteChat(101, 2))
        assertFalse(Service.deleteChat(505, 222))
    }
}