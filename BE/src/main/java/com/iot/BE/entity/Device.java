package com.iot.BE.entity;



import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "device")
@Data // setter getter
@AllArgsConstructor // all param constructor
@NoArgsConstructor // no param constructor
public class Device {
    // column id
    @Id
    // generate auto increment
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    // column name in DB
    @Column
    private String name;
    @Column
    private Boolean status;
}
