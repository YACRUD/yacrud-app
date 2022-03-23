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

    // These fields will be used for sorting
    @SuppressWarnings("java:S115")
    public enum SortField {
        firstName, lastName, email;

        // Sorting must be applied to the "keyword" type fields
        @Override
        public String toString() {
            return name() + ".keyword";
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
            // use , "fielddata = true" to be be able to Sort on them natively
            // TODO: find a way to use the "Keyword" fields for sorting
            mainField = @Field(type = FieldType.Text, fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String firstName;

    @MultiField(
            mainField = @Field(type = FieldType.Text, fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String lastName;

    @MultiField(
            mainField = @Field(type = FieldType.Text, fielddata = true),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String email;
}
