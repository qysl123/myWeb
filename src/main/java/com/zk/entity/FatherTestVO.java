package com.zk.entity;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "father")
public class FatherTestVO {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id")
    private String id;

    @OneToMany(targetEntity=TestVO.class, cascade=CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "PREANT_ID")
    @Fetch(FetchMode.JOIN)
    private List<TestVO> children;

    @Column(name="name", length=50)
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TestVO> getChildren() {
        return children;
    }

    public void setChildren(List<TestVO> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "FatherTestVO{" +
                "id='" + id + '\'' +
                ", children=" + children +
                ", name='" + name + '\'' +
                '}';
    }
}
