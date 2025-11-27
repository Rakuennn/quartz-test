package com.codewithpot.store.json.model;

import lombok.Data;

import java.util.List;

@Data
public class MockData {

    private String fullName;
    private Integer age;
    private Double averageScore;

    private Boolean isOnline;
    private Boolean emailVerified;

    private Object extraInfo;

    private List<Integer> numberList;

    private List<Object> mixedList;

    private MainUser mainUser;

    private List<TempModel> users;

    private SystemSettings systemSettings;

    private List<Object> emptyList;
    private Object emptyObject;
}
