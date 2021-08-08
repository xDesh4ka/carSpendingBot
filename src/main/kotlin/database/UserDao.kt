package database

import botTg.*
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

val url = "jdbc:mysql://localhost:3306/tgbotTest?verifyServerCertificate=false&useSSL=false&serverTimezone=UTC"


fun ResultSet.getSpend(id : Int) : MutableList<Spend> {
    val buf = mutableListOf<Spend>()
    Class.forName("com.mysql.cj.jdbc.Driver")
    val connection  = DriverManager.getConnection(url, "root", "")
    val stmt: PreparedStatement = connection.prepareStatement("select * from spend where user_id=?")
    stmt.setInt(1, id)
    val rs = stmt.executeQuery()
    while (rs.next())
    {
        buf.add(Spend(rs.getString(1), rs.getLong(2)))
    }
    return buf
}

fun PreparedStatement.setSpend(saveOrUpdate : Boolean, user : User) : Unit
{
    Class.forName("com.mysql.cj.jdbc.Driver")
    val connection  = DriverManager.getConnection(url, "root", "")
    val stmt: PreparedStatement = if (saveOrUpdate) {
        connection.prepareStatement(
            "insert into spend (description, cost, user_id) values (? , ?, ?) "
        )
    }
    else {
        val newstmt  = connection.prepareStatement("delete from spend where user_id = ${user.id}")
        newstmt.executeUpdate()
        connection.prepareStatement(
            "insert into spend (description, cost, user_id) values (? , ?, ?)"
        )
    };
    user.spends?.forEach {
        stmt.setString(1, it.description)
        stmt.setLong(2, it.cost)
        stmt.setLong(3, user.id)
        stmt.executeUpdate()
    }
}

class UserDao
{
    fun getById(id: Int) : User?
    {
        Class.forName("com.mysql.cj.jdbc.Driver")
        val connection  = DriverManager.getConnection(url, "root", "")

        val stmt: PreparedStatement = connection.prepareStatement("select * from user where id=? limit 1")
        stmt.setInt(1, id)
        val rs = stmt.executeQuery()

        var  ret : User? = null

        while (rs.next())
        {
            ret = User(
                rs.getLong(1),
                rs.getString(2),
                rs.getSpend(id)
            )
        }
        return ret
    }

    fun save(user: User) : Unit {
        Class.forName("com.mysql.cj.jdbc.Driver")
        val connection  = DriverManager.getConnection(url, "root", "")
        val stmt: PreparedStatement = connection.prepareStatement(
            "insert into user (id, name) values (? , ?) "
        );
        stmt.setLong(1, user.id)
        stmt.setString(2, user.name ?: "NonNickUser")
        stmt.executeUpdate()
        stmt.setSpend(true, user)
    }

    fun update(user: User) : Unit {
        Class.forName("com.mysql.cj.jdbc.Driver")
        val connection  = DriverManager.getConnection(url, "root", "")
        val stmt: PreparedStatement = connection.prepareStatement(
            "update user set name =? where id= ?"
        )
        stmt.setString(1, user.name?: "NonNickUser")
        stmt.setLong(2, user.id)
        stmt.setSpend(false, user)
        stmt.executeUpdate()
    }
}
