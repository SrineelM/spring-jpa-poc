package com.example.demo.domain;

import jakarta.persistence.*;
import java.util.*;

@Entity
public class Group extends BaseEntity {

    private String name;

    @ManyToMany(mappedBy = "groups")
    private Set<User> users = new HashSet<>();

    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public Set<User> getUsers() { return users; }
}
