package com.example.open_sourcepart2.database.repository

import com.example.open_sourcepart2.database.dao.UserDao
import com.example.open_sourcepart2.database.entities.User

class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(name: String, email: String, age: Int, password: String): Long {
        val user = User(name = name, email = email, age = age, password = password)
        return userDao.insertUser(user)
    }

    suspend fun login(email: String, password: String): User? {
        return userDao.login(email, password)
    }

    suspend fun isEmailRegistered(email: String): Boolean {
        return userDao.getUserByEmail(email) != null
    }
}