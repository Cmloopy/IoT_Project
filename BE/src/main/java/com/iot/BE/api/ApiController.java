package com.iot.BE.api;

import com.iot.BE.entity.Device;
import com.iot.BE.entity.HistoryAction;
import com.iot.BE.entity.SensorData;
import com.iot.BE.repository.DeviceRepository;
import com.iot.BE.repository.HistoryActionRepository;
import com.iot.BE.repository.SensorDataRepository;
import com.iot.BE.service.MosquittoService;
import com.iot.BE.utils.Constant;
import com.iot.BE.utils.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class ApiController {
    @Autowired
    private SensorDataRepository sensorDataRepository;
    @Autowired
    private HistoryActionRepository actionRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private MosquittoService mosquittoService;
    @Autowired
    private HistoryActionRepository historyActionRepository;

    @GetMapping("/")
    public ResponseEntity<List<SensorData>> home() {
        // Set default pagination
        // page 0, size 20, sort DESC field date
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "time"));
        // get data pagination
        List<SensorData> ans = sensorDataRepository.findLimited(pageable);
        return ResponseEntity.ok(ans);
    }

    @GetMapping("/alldevice")
    public ResponseEntity<List<Device>> alldevice() {
        List<Device> ans = deviceRepository.findAll();
        return ResponseEntity.ok(ans);
    }

    @GetMapping("/sensordata")
    public ResponseEntity<List<SensorData>> sensordata(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(value = "field", required = false) String field,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "term", required = false) String term) {
        // term is search value
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "time"));
        List<SensorData> ans = new ArrayList<>();
        /*
         * if field equals all return filter all field
         * */
        if (field == null) {
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "time"));
        } else if (field.equals("all")) {
            ans = sensorDataRepository.filterAllFieldSensorData(pageable, term);
            return ResponseEntity.ok(ans);
        }
        // if term
        if (term == null || term.isEmpty()) {
            if (field == null || field.isEmpty()) {
                // set pageable
                pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "time"));
            } else if (!field.isEmpty()) {
                if (order == null || order.isEmpty() || order.equals("DESC")) {
                    // set pageable sort by field
                    pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, field));
                } else {
                    pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, field));
                }
            }
            // get data
            ans = sensorDataRepository.findLimited(pageable);
        } else {
            if (order == null || order.isEmpty() || order.equals("DESC")) {
                pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, field));
            } else {
                pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, field));
            }
            ans = sensorDataRepository.filterSensorData(pageable, field, term);
        }
        ans = sortDataSensor(ans, field, order);
        return ResponseEntity.ok(ans);
    }

    @GetMapping("/historyaction")
    public ResponseEntity<List<HistoryAction>> historyaction(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(value = "field", required = false) String field,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "term", required = false) String term
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timeConvert"));
        List<HistoryAction> ans = new ArrayList<>();

        if (field == null) {
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timeConvert"));
        } else if (field.equals("all")) {
            ans = actionRepository.filterAllFieldHistoryAction(pageable, term);
            return ResponseEntity.ok(ans);
        }
        if (term == null || term.isEmpty()) {
            if (field == null || field.isEmpty()) {
                pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timeConvert"));
            } else if (!field.isEmpty()) {
                if (order == null || order.isEmpty() || order.equals(Constant.DESC)) {
                    pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, field));
                } else {
                    pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, field));
                }
            }
            ans = actionRepository.findLimited(pageable);
        } else {
            if (order == null || order.isEmpty() || order.equals(Constant.DESC)) {
                pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, field));
            } else {
                pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, field));
            }
            ans = actionRepository.filterHistoryAction(pageable, field, term);
        }
        ans = sortHistoryAction(ans, field, order);
        return ResponseEntity.ok(ans);
    }

    @GetMapping("/led")
    public ResponseEntity<HistoryAction> led(@RequestParam(value = "id") int id,
                                             @RequestParam(value = "action") String action) throws InterruptedException {
        String topic = "";
        // check id device to set topic
        switch (id) {
            case 1:
                topic = Constant.LED_CONTROL;
                break;
            case 2:
                topic = Constant.FAN_CONTROL;
                break;
            case 3:
                topic = Constant.AC_CONTROL;
                break;
            default:
                break;
        }
        // create mes to pub
        String mes = action.equals("true") ? "1" : "0";
        // pub mes
        mosquittoService.publishMessage(topic, mes);
        Thread.sleep(2000);
        while (true) {
            // get last id from sharedList at MosquittoService
            System.out.println(Constant.sharedList.toString());
            int lastId = -1;
            lastId = Constant.sharedList.get(Constant.sharedList
                    .size() - 1);


            // get history action from DB by lastId

            HistoryAction historyAction = historyActionRepository.findById(lastId);
            // get device by id param
            Device device = deviceRepository.findById(id);
            device.setStatus(historyAction.getAction());

            // check condition
            // if id param equals id of history action then return history action
            if (historyAction.getDevice().getId().equals(id)) {
                deviceRepository.save(device);
                return ResponseEntity.ok(historyAction);
            } else {
                Thread.sleep(500);
            }


        }
    }


    @GetMapping("/countgreater80")
    public ResponseEntity<Long> countgreater80() throws InterruptedException {
        // get time local
        String time = Time.getTimeLocalConvert();
        // split time to
        time = time.split(" ")[0];
        Long ans = sensorDataRepository.countWindyGreaterThan80(time);
        return ResponseEntity.ok(ans);

    }

    @GetMapping("/counttimeon")
    public ResponseEntity<Long> counttimeon() throws InterruptedException {
        String time = Time.getTimeLocalConvert();
        time = time.split(" ")[0];
        long ans = historyActionRepository.countTrueStatusForFanToday(time);
        return ResponseEntity.ok(ans);
    }

    private List<SensorData> sortDataSensor(List<SensorData> list, String field, String order) {
        if (field == null) {
            return list;
        } else if (field.equals("temperature")) {

            if (order == null || order.equals(Constant.DESC)) {
                list.sort(new Comparator<SensorData>() {
                    @Override
                    public int compare(SensorData o1, SensorData o2) {
                        if (o1.getTemperature().compareTo(o2.getTemperature()) == 0) {
                            return o2.getId().compareTo(o1.getId());
                        }
                        return o2.getTemperature().compareTo(o1.getTemperature());
                    }
                });
            } else {
                list.sort(new Comparator<SensorData>() {
                    @Override
                    public int compare(SensorData o1, SensorData o2) {
                        if (o1.getTemperature().compareTo(o2.getTemperature()) == 0) {
                            return o2.getId().compareTo(o1.getId());
                        }
                        return o1.getTemperature().compareTo(o2.getTemperature());
                    }
                });
            }

        } else if (field.equals("humidity")) {
            if (order == null || order.equals(Constant.DESC)) {
                list.sort(new Comparator<SensorData>() {
                    @Override
                    public int compare(SensorData o1, SensorData o2) {
                        if (o1.getHumidity().compareTo(o2.getHumidity()) == 0) {
                            return o2.getId().compareTo(o1.getId());
                        }
                        return o2.getHumidity().compareTo(o1.getHumidity());
                    }
                });
            } else {

                list.sort(new Comparator<SensorData>() {
                    @Override
                    public int compare(SensorData o1, SensorData o2) {
                        if (o1.getHumidity().compareTo(o2.getHumidity()) == 0) {
                            return o1.getId().compareTo(o2.getId());
                        }
                        return o1.getHumidity().compareTo(o2.getHumidity());
                    }
                });
            }

        } else if (field.equals("light")) {
            if (order == null || order.equals(Constant.DESC)) {

                list.sort(new Comparator<SensorData>() {
                    @Override
                    public int compare(SensorData o1, SensorData o2) {
                        if (o1.getLight().compareTo(o2.getLight()) == 0) {
                            return o2.getId().compareTo(o1.getId());
                        }
                        return o2.getLight().compareTo(o1.getLight());
                    }
                });
            } else {
                list.sort(new Comparator<SensorData>() {
                    @Override
                    public int compare(SensorData o1, SensorData o2) {
                        if (o1.getLight().compareTo(o2.getLight()) == 0) {
                            return o1.getId().compareTo(o2.getId());
                        }
                        return o1.getLight().compareTo(o2.getLight());
                    }
                });
            }

        } else if (field.equals("time")) {
            if (order == null || order.equals(Constant.DESC)) {
                list.sort(new Comparator<SensorData>() {
                    @Override
                    public int compare(SensorData o1, SensorData o2) {
                        if (o1.getTimeConvert().compareTo(o2.getTimeConvert()) == 0) {
                            return o2.getId().compareTo(o1.getId());
                        }
                        return o2.getTimeConvert().compareTo(o1.getTimeConvert());
                    }
                });
            } else {

                list.sort(new Comparator<SensorData>() {
                    @Override
                    public int compare(SensorData o1, SensorData o2) {
                        if (o1.getTimeConvert().compareTo(o2.getTimeConvert()) == 0) {
                            return o1.getId().compareTo(o2.getId());
                        }
                        return o1.getTimeConvert().compareTo(o2.getTimeConvert());
                    }
                });
            }

        }
        return list;
    }


    private List<HistoryAction> sortHistoryAction(List<HistoryAction> list, String field, String order) {
        if (field == null) {
            return list;
        } else if (field.equals("name")) {
            if (order == null || order.equals(Constant.DESC)) {
                list.sort(new Comparator<HistoryAction>() {
                    @Override
                    public int compare(HistoryAction o1, HistoryAction o2) {
                        if (o1.getName().equals(o2.getName())) {
                            return o2.getId().compareTo(o1.getId());
                        }
                        return o2.getName().compareTo(o1.getName());
                    }
                });
            } else {

                list.sort(new Comparator<HistoryAction>() {
                    @Override
                    public int compare(HistoryAction o1, HistoryAction o2) {
                        if (o1.getName().equals(o2.getName())) {
                            return o1.getId().compareTo(o2.getId());
                        }
                        return o1.getName().compareTo(o2.getName());
                    }
                });
            }
        } else if (field.equals("time")) {
            if (order == null || order.equals(Constant.DESC)) {
                list.sort(new Comparator<HistoryAction>() {
                    @Override
                    public int compare(HistoryAction o1, HistoryAction o2) {
                        if (o1.getTimeConvert().equals(o2.getTimeConvert())) {
                            return o2.getId().compareTo(o1.getId());
                        }
                        return o2.getTimeConvert().compareTo(o1.getTimeConvert());
                    }
                });
            } else {

                list.sort(new Comparator<HistoryAction>() {

                    @Override
                    public int compare(HistoryAction o1, HistoryAction o2) {
                        if (o1.getTimeConvert().equals(o2.getTimeConvert())) {
                            return o1.getId().compareTo(o2.getId());
                        }
                        return o1.getTimeConvert().compareTo(o2.getTimeConvert());
                    }
                });
            }
        }
        return list;
    }
}
