package com.github.jetnet.yacrud.entity;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Data
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
@ToString
@FieldNameConstants
@Document(indexName = "persons")
public class Person {

    public static final String KEYWORD_FIELD_SUFFIX = "keyword";

    // These fields will be used for sorting
    @SuppressWarnings(value = "java:S115")
    public enum SortField {
        firstName, lastName, email;

        // As enabling "fielddata" didn't work properly for sorting (terms were tokenized),
        // using the following workaround:
        // Sorting must be applied to the "keyword" type fields
        @Override
        public String toString() {
            return name() + "." + KEYWORD_FIELD_SUFFIX;
        }
    }

    public Person() {
    }

    public Person(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    @Id
    private String id;

    @MultiField(
            // TODO: find a way to use the "Keyword" fields for sorting
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = KEYWORD_FIELD_SUFFIX, type = FieldType.Keyword)
            }
    )
    private String firstName;

    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = KEYWORD_FIELD_SUFFIX, type = FieldType.Keyword)
            }
    )
    private String lastName;

    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = KEYWORD_FIELD_SUFFIX, type = FieldType.Keyword)
            }
    )
    private String email;
}
