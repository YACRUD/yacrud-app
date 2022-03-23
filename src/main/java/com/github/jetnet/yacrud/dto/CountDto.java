package com.github.jetnet.yacrud.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CountDto {
    @JsonProperty("Total count")
    private long value;

    public CountDto(Long value) {
        this.value = value;
    }
}
