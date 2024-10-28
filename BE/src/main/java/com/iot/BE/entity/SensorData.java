package com.iot.BE.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "sensor_data")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SensorData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column
    private Double temperature;
    @Column
    private Double humidity;
    @Column
    private Double light;
    // field for after request
    @Column
    private Double other;
    @Column
    private Date time;
    @Column(name = "timeconvert")
    private String timeConvert;
}
