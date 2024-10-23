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

    @Mappings(
        Mapping(source = "pk", target = "id", qualifiedByName = ["buildId"]),
        Mapping(source = "birthDate", target = "dateOfBirth"),
    )
    abstract fun toDomain(entity: StudentEntity): Student

    @Named("buildPk")
    fun buildPk(id: Long): String = StudentEntity.buildPk(id)

    @Named("buildSk")
    fun buildSk(id: Long): String = StudentEntity.buildSk(id)

    @Named("buildId")
    fun buildId(pk: String): Long = pk.split("#").last().toLong()
}
