package com.iot.BE.repository;

import com.iot.BE.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Integer> {
    List<Device> findAll();

    Device findById(int id);
}
