package com.tiantian.springintejms.entity;

import java.io.Serializable;

/**
 * Created by wangling on 2018/1/10.
 */

public class TestMqBean implements Serializable {
    private Integer age;
    private String name;
    public Integer getAge() {
        return age;
    }
    public void setAge(Integer age) {
        this.age = age;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
