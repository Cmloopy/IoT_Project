package com.iot.BE.repository;

import com.iot.BE.entity.SensorData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Integer> {

    @Query("select  data from SensorData data")
    List<SensorData> findLimited(Pageable pageable);

    //  filter 1 field
    @Query("SELECT d FROM SensorData d WHERE " +
            "(:field = 'temperature' AND STR(d.temperature) = :term ) OR " +
            "(:field = 'humidity' AND STR(d.humidity) = :term ) OR " +
            "(:field = 'light' AND STR(d.light) = :term ) OR " +
            "(:field = 'date' AND STR(d.timeConvert) = :term )")
    List<SensorData> filterSensorData(Pageable pageable,@Param("field") String field,@Param("term") String term);

    // filter all fields
    @Query("SELECT d FROM SensorData d WHERE " +
            "STR(d.temperature) = :term " +
            "AND STR(d.humidity) = :term " +
            "AND STR(d.light) = :term " +
            "AND STR( d.timeConvert) = :term ")
    List<SensorData> filterAllFieldSensorData(Pageable pageable,@Param("term") String term);


    // count other pass over 80
    @Query("SELECT COUNT(d) FROM SensorData d WHERE d.other >= 80 AND STR(d.timeConvert) LIKE CONCAT(:date, '%')")
    long countWindyGreaterThan80(@Param("date") String date);
}
