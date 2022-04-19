package ru.netology.exceptions

class QuantityOfChatsExceeded(quantityOfChats: Int): Exception("Количество чатов $quantityOfChats > 1") {
}