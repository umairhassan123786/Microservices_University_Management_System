package com.university.Entities;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "students")
public class StudentES {

    @Id
    private String id;  

    @Field(type = FieldType.Long)
    private Long studentId;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Keyword)
    private String email;

    @Field(type = FieldType.Keyword)
    private String rollNumber;

    @Field(type = FieldType.Keyword)
    private String department;

    @Field(type = FieldType.Keyword)
    private String semester;

    @Field(type = FieldType.Boolean)
    private Boolean active;

    public StudentES(Long id, Long id1, String name, String email, String rollNumber, String department, String semester, Boolean active) {
    }
}