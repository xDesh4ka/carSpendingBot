package botTg

import kotlin.coroutines.*
import kotlin.let
import com.github.kotlintelegrambot.*
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import database.UserDao
import database.addSpend
import database.getAllUserIds
import database.getTotalSumById

data class Spend (
    val description : String? = null,
    val cost : Long = 0
)

data class User(val id : Long, val name : String?, var spends : MutableList<Spend>? = null)

fun main() {
    val userList = getAllUserIds()
    val dao = UserDao()
    var user : User? = null
    bot {
        token = "token"
        dispatch {
                command("start") {
                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id),
                        text = "Привет, ${message.chat.username?: "Друг"}!")
                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id),
                        text = "Чтобы добавить запись — напиши на что потратил и сумму, например \"бензин 500\"")
                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id),
                        text = """Если хочешь узнать, сколько ты потратил денег на конкретную категорию — введи /Итого""",
                        replyMarkup = KeyboardReplyMarkup(listOf(listOf(KeyboardButton("/Итого")))))
                    if(message.chat.id !in userList) {
                        dao.save(User(message.chat.id, message.chat.username?: "NonNickUser"))
                        userList.add(message.chat.id)
                    }
                }
                command("Итого"){
                    val total = getTotalSumById(message.chat.id)
                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id),
                        text = "Всего вы потратили ${total}")
                }
                text {
                    addSpend(message)
                }
        }
    }.startPolling()

}