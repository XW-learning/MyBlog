package com.personalblog.model;

import java.sql.Timestamp;

public class Category {
    private Long id;
    private String name;
    private Timestamp createTime;

    public Category() {
    }

    public Category(Long id, String name, Timestamp createTime) {
        this.id = id;
        this.name = name;
        this.createTime = createTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
}