package com.iot.BE.service;

import com.iot.BE.entity.Device;
import com.iot.BE.entity.HistoryAction;
import com.iot.BE.entity.SensorData;
import com.iot.BE.repository.DeviceRepository;
import com.iot.BE.repository.HistoryActionRepository;
import com.iot.BE.repository.SensorDataRepository;
import com.iot.BE.utils.Constant;
import com.iot.BE.utils.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.eclipse.paho.client.mqttv3.*;

import java.util.Arrays;

@Service
public class MosquittoService {
    private MqttClient client;

    // dependency injection
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private HistoryActionRepository historyActionRepository;
    @Autowired
    private SensorDataRepository sensorDataRepository;

    public MosquittoService()  {
        try {
            client = new MqttClient(Constant.BROKER, Constant.CLIENT_ID);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            // Connect to the MQTT broker
            System.out.println("Connecting to broker: " + Constant.BROKER);
            client.connect(options);
            System.out.println("Connected");

            // Set up a callback to handle messages
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("Connection lost! " + cause.getMessage());
                    // Retry connection
                    while (!client.isConnected()) {
                        try {
                            System.out.println("Attempting to reconnect...");
                            client.connect(options);
                            // Resubscribe to topics
                            client.subscribe(Constant.DATA_SENSOR);
                            client.subscribe(Constant.LED_RESPONSE);
                            client.subscribe(Constant.FAN_RESPONSE);
                            client.subscribe(Constant.AC_RESPONSE);
                            System.out.println("Reconnected to MQTT broker");
                        } catch (MqttException e) {
                            System.out.println("Reconnection failed, will retry in 2 seconds.");
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // handle
                    handleMessage(topic,message);
                    System.out.println("Topic: " + topic + ", Message: " + new String(message.getPayload()) );
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    System.out.println("Delivery complete");
                }
            });

            // Subscribe to topics
            client.subscribe(Constant.DATA_SENSOR);
            client.subscribe(Constant.LED_RESPONSE);
            client.subscribe(Constant.FAN_RESPONSE);
            client.subscribe(Constant.AC_RESPONSE);


        } catch (MqttException  e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(String topic, MqttMessage message) {
        if (Constant.DATA_SENSOR.equals(topic)) {
            // get data from message
            String data = new String(message.getPayload());
            SensorData sensorData = new SensorData();
            // split data to double array
            // 0 - Temperature
            // 1 - Humidity
            // 2 - Light
            Double [] arrayData = Arrays.stream(data.split(" "))
                    .map(Double::valueOf)
                    .toArray(Double[]::new);
            // Set value to fields
            sensorData.setTemperature(arrayData[0]);
            sensorData.setHumidity(arrayData[1]);
            sensorData.setLight(1024 - arrayData[2]);
            sensorData.setTime((Time.getTimeLocal()));
//            sensorData.setWindy(arrayData[3]);
            sensorData.setTimeConvert((Time.getTimeLocalConvert()));
            sensorDataRepository.save(sensorData);

        } else if (Constant.LED_RESPONSE.equals(topic)) {
            // Give id of device and message
            // Aim: Create history action data -> save DB
            System.out.println("this is mes "+ message);
            HistoryAction historyAction = getResponseMQTT(1, message);
            // Save history action
            HistoryAction data = historyActionRepository.save(historyAction);
            System.out.println();
            // Add id of history action to list
            // Aim: compare the last id with the last id of history action
            Constant.sharedList.add(data.getId());

        } else if (Constant.FAN_RESPONSE.equals(topic)) {
            HistoryAction dataDevice = getResponseMQTT(2, message);
            HistoryAction data = historyActionRepository.save(dataDevice);
            Constant.sharedList.add(data.getId());

        } else if (Constant.AC_RESPONSE.equals(topic)) {
            HistoryAction dataDevice = getResponseMQTT(3, message);
            HistoryAction data = historyActionRepository.save(dataDevice);
            Constant.sharedList.add(data.getId());
        }
    }

    // publish mes to topic
    public void publishMessage(String topic, String messageContent) {
        try {
            // create message mqtt
            MqttMessage message = new MqttMessage(messageContent.getBytes());
            // quality of service = 2
            message.setQos(2);
            // send mes to topic
            client.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private HistoryAction getResponseMQTT(int id, MqttMessage message) {
        // get data String from mqtt
        String data = new String(message.getPayload());
        HistoryAction historyAction = new HistoryAction();
        Device device = deviceRepository.findById(id);
        historyAction.setDevice(device);
        historyAction.setTime(Time.getTimeLocal());
        historyAction.setName(device.getName());
        // if data == "HIGH" ? true : false
        historyAction.setAction(data.equals("HIGH"));
        return historyAction;
    }
}
