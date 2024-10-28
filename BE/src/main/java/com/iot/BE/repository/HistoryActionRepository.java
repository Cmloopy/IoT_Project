package com.iot.BE.repository;

import com.iot.BE.entity.HistoryAction;
import com.iot.BE.entity.SensorData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryActionRepository extends JpaRepository<HistoryAction, Integer> {
    // Query use for DB
    HistoryAction findById(int id);

    @Query("select  data from HistoryAction data")
    List<HistoryAction> findLimited(Pageable pageable);

    // filter name or date
    @Query("SELECT d FROM HistoryAction d WHERE " +
            "(:field = 'name' AND d.name = :term ) OR " +
            "(:field = 'date' AND d.timeConvert = :term )")
    List<HistoryAction> filterHistoryAction(Pageable pageable,@Param("field") String field,@Param("term") String term);

    // filter all field
    @Query("SELECT d FROM HistoryAction d WHERE " +
            "d.name = :term " +
            "AND d.timeConvert = :term ")
    List<HistoryAction> filterAllFieldHistoryAction(Pageable pageable,@Param("term") String term);

    // count turn on fan
    @Query("SELECT COUNT(d) FROM HistoryAction d WHERE d.name = 'FAN' "
            + "  AND d.action = true AND STR(d.timeConvert) LIKE CONCAT(:date, '%')")
    long countTrueStatusForFanToday(@Param("date") String date);
}
