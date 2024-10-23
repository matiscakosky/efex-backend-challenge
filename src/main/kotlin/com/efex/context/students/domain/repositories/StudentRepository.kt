package com.efex.context.students.domain.repositories

import com.efex.context.students.domain.entities.Student

interface StudentRepository {
    fun create(student: Student): Student

    fun findAll(): List<Student>

    fun findById(id: Long): Student?

    fun update(
        id: Long,
        student: Student,
    ): Student?
}
