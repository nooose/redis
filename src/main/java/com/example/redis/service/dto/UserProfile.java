package com.example.redis.service.dto;

public class UserProfile {
    private String name;
    private int age;

    public UserProfile(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
