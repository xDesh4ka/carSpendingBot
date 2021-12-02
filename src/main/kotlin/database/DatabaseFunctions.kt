package database

import botTg.*
import com.github.kotlintelegrambot.entities.Message
import java.sql.DriverManager
import java.sql.PreparedStatement


fun getAllUserIds() : MutableSet<Long>
{
    val userIds = mutableSetOf<Long>()
    Class.forName("com.mysql.cj.jdbc.Driver")
    val connection  = DriverManager.getConnection(url, "root", "")
    val stmt: PreparedStatement = connection.prepareStatement("select * from user")
    val rs = stmt.executeQuery()
    while(rs.next())
    {
        userIds.add(rs.getLong(1))
    }
    return userIds;
}

fun getTotalSumById(id : Long) : Long
{
    var ret : Long = 0
    Class.forName("com.mysql.cj.jdbc.Driver")
    val connection  = DriverManager.getConnection(url, "root", "")
    val stmt: PreparedStatement = connection.prepareStatement("select sum(cost) from spend where user_id = ?")
    stmt.setLong(1, id)
    val rs = stmt.executeQuery()
    while(rs.next())
    {
        ret = rs.getLong(1)
    }
    return ret
}

fun addSpend(message : Message) : Unit {
    Class.forName("com.mysql.cj.jdbc.Driver")
    val connection  = DriverManager.getConnection(url, "root", "")
    val text = message.text!!
    val spend = Spend(
        (text.split(" ") - text.split(" ").last()).joinToString(" "),
        text.split("""(.*) """.toRegex()).last().toLong()
    )
    val stmt: PreparedStatement = connection.prepareStatement(
        "insert into spend (description, cost, user_id) values (? , ?, ?)"
    )
    stmt.setString(1, spend.description)
    stmt.setLong(2, spend.cost)
    stmt.setLong(3, message.chat.id)
    stmt.executeUpdate()
}