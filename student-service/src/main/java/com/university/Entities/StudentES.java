package com.university.Entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

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

    // ✅ Default Constructor
    public StudentES() {}

    // ✅ Fixed Constructor - All fields
    public StudentES(String id, Long studentId, Long userId, String name, String email,
                     String rollNumber, String department, String semester, Boolean active) {
        this.id = id;
        this.studentId = studentId;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.rollNumber = rollNumber;
        this.department = department;
        this.semester = semester;
        this.active = active;
    }

    public StudentES(Long id, Long userId, String name, String email, String rollNumber, String department, String semester, Boolean active) {
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}