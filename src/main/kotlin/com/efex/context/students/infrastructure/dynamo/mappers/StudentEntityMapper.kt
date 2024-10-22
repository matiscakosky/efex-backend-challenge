package com.efex.context.students.infrastructure.dynamo.mappers

import com.efex.context.students.domain.entities.Student
import com.efex.context.students.infrastructure.dynamo.entities.StudentEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.Named

@Mapper(componentModel = "jsr330")
abstract class StudentEntityMapper {

    @Mappings(
        Mapping(source = "id", target = "pk", qualifiedByName = ["buildPk"]),
        Mapping(source = "id", target = "sk", qualifiedByName = ["buildSk"]),
        Mapping(source = "lastName", target = "lastName"),
        Mapping(source = "firstName", target = "firstName"),
        Mapping(source = "dateOfBirth", target = "birthDate"),
        Mapping(source = "grade", target = "grade"),
        Mapping(source = "phone", target = "phone"),
        Mapping(source = "email", target = "email"),
    )
    abstract fun toEntity(domain: Student): StudentEntity

    abstract fun toDomain(entity: StudentEntity): Student

    @Named("buildPk")
    fun buildPk(id: Long): String {
        return StudentEntity.buildPk(id.toString())
    }

    @Named("buildSk")
    fun buildSk(id: Long): String {
        return StudentEntity.buildSk(id.toString())
    }
}