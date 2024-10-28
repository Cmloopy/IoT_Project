package com.iot.BE.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "history_action")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column
    private String name;
    @Column
    private Boolean action;
    @Column
    private Date time;
    @Column(name = "timeconvert")
    private String timeConvert;
    @ManyToOne
    @JoinColumn(name = "device")
    private Device device;
}
