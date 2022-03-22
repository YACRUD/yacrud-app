package com.github.jetnet.yacrud.util;

import com.github.jetnet.yacrud.entity.Person;
import com.github.jetnet.yacrud.dto.PersonDto;

public class EntityDtoUtil {

    private EntityDtoUtil() {
    }

    public static PersonDto toDto(Person person){
        PersonDto dto = new PersonDto();
        dto.setId(person.getId());
        dto.setFirstName(person.getFirstName());
        dto.setLastName(person.getLastName());
        dto.setEmail(person.getEmail());
        return dto;
    }

    public static Person toEntity(PersonDto dto){
        Person person = new Person();
        person.setId(dto.getId());
        person.setFirstName(dto.getFirstName());
        person.setLastName(dto.getLastName());
        person.setEmail(dto.getEmail());
        return person;
    }
}