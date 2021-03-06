package com.zk.entity;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;


@Entity
@Table(name = "test")
public class TestVO {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id")
    private String id;

    @Column(name="name", length=50)
    private String name;

    @Cascade(value = CascadeType.SAVE_UPDATE)
    @ManyToOne(targetEntity = FatherTestVO.class, fetch = FetchType.LAZY)
    @JoinColumn(name="PREANT_ID",updatable=false)
    @Fetch(FetchMode.JOIN)
    private FatherTestVO father;

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

    public FatherTestVO getFather() {
        return father;
    }

    public void setFather(FatherTestVO father) {
        this.father = father;
    }

    @Override
    public String toString() {
        return "TestVO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", father={id='"+father.getId()+"', name='"+father.getName()+"'}"+
                '}';
    }
}
