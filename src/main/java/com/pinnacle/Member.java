package com.pinnacle;

public class Member {
    private int studentId;
    private String name;
    private String department;
    private String phone;

    public Member(int studentId, String name, String department, String phone) {
        this.studentId = studentId;
        this.name = name;
        this.department = department;
        this.phone = phone;
    }

    public int getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public String getPhone() { return phone; }
}