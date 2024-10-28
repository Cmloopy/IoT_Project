#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include "DHT.h"

#define DHTPIN D7      // Chân kết nối với DATA của DHT11 (GPIO2)
#define DHTTYPE DHT11  // Sử dụng DHT11
#define LIGHT_PIN A0   // Chân analog duy nhất trên ESP8266
#define LED_FAN D1     // Chân LED quạt (GPIO5)
#define LED_AC D2      // Chân LED điều hòa (GPIO4)
#define LED_BULB D3    // Chân LED bóng đèn (GPIO0)

//const char* ssid = "Mi 11";        // Tên mạng Wi-Fi
//const char* password = "111111111"; // Mật khẩu mạng Wi-Fi
const char* ssid = "TP-LINK_DB92";
const char* password = "96063231";

// Thông tin MQTT Broker (Mosquitto)
//const char* mqtt_server = "192.168.6.69";
const char* mqtt_server = "192.168.0.111";
//"192.168.6.69"; // Địa chỉ IP của MQTT broker
const char* mqtt_user = "B21DCCN140";      // Username MQTT nếu có
const char* mqtt_password = "1111";        // Password MQTT nếu có

WiFiClient espClient;
PubSubClient client(espClient);
DHT dht(DHTPIN, DHTTYPE);

void setup() {
  Serial.begin(115200);
  WiFi.begin(ssid, password);
  
  Serial.println("Connecting to Wi-Fi...");
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print(".");
  }
  Serial.println("\nConnected to Wi-Fi");
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());

  client.setServer(mqtt_server, 1885);
  client.setCallback(callback);

  pinMode(LED_FAN, OUTPUT);
  pinMode(LED_AC, OUTPUT);
  pinMode(LED_BULB, OUTPUT);
  dht.begin(); // Khởi động cảm biến DHT
  delay(1000); // Đợi cảm biến ổn định
}

void reconnect() {
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    if (client.connect("ESP8266Client", mqtt_user, mqtt_password)) {
      Serial.println("connected");
      // client.subscribe("control/led");
      client.subscribe("LED_CONTROL");
      client.subscribe("FAN_CONTROL");
      client.subscribe("AC_CONTROL");
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      delay(5000);
    }
  }
}

void callback(char* topic, byte* payload, unsigned int length) {
  String message;
  for (int i = 0; i < length; i++) {
    message += (char)payload[i];
  }

  if (strcmp(topic, "LED_CONTROL") == 0) {
    digitalWrite(LED_BULB, message.equals("1") ? HIGH : LOW);
    client.publish("LED_RESPONSE",  message.equals("1") ? "HIGH" : "LOW");
  } 
  else if (strcmp(topic, "FAN_CONTROL") == 0) {
    digitalWrite(LED_FAN, message.equals("1") ? HIGH : LOW);
    client.publish("FAN_RESPONSE",  message.equals("1") ? "HIGH" : "LOW");
  } 
  else if (strcmp(topic, "AC_CONTROL") == 0) {
    digitalWrite(LED_AC, message.equals("1") ? HIGH : LOW);
    client.publish("AC_RESPONSE",  message.equals("1") ? "HIGH" : "LOW");
  }

  // payload[length] = '\0'; // Kết thúc chuỗi
  // String message = String((char*)payload);

  // controlLEDs(message);
}

// void controlLEDs(String message) {
//   if (message == "fan_on") {
//     digitalWrite(LED_FAN, HIGH);
//     Serial.println("Quạt đã bật");
//   } else if (message == "fan_off") {
//     digitalWrite(LED_FAN, LOW);
//     Serial.println("Quạt đã tắt");
//   } else if (message == "ac_on") {
//     digitalWrite(LED_AC, HIGH);
//     Serial.println("Điều hòa đã bật");
//   } else if (message == "ac_off") {
//     digitalWrite(LED_AC, LOW);
//     Serial.println("Điều hòa đã tắt");
//   } else if (message == "bulb_on") {
//     digitalWrite(LED_BULB, HIGH);
//     Serial.println("Bóng đèn đã bật");
//   } else if (message == "bulb_off") {
//     digitalWrite(LED_BULB, LOW);
//     Serial.println("Bóng đèn đã tắt");
//   } else if (message == "all_on") {
//     digitalWrite(LED_FAN, HIGH);
//     digitalWrite(LED_AC, HIGH);
//     digitalWrite(LED_BULB, HIGH);
//     Serial.println("Đã bật tất cả!");
//   } else if (message == "all_off") {
//     digitalWrite(LED_FAN, LOW);
//     digitalWrite(LED_AC, LOW);
//     digitalWrite(LED_BULB, LOW);
//     Serial.println("Đã tắt tất cả!");
//   }
// }

void loop() {
  if (!client.connected()) {
    reconnect();
  }
  client.loop();

  // Đọc dữ liệu từ cảm biến DHT
  float h = dht.readHumidity();
  float t = dht.readTemperature();
  float f = dht.readTemperature(true);
  int lightValue = analogRead(LIGHT_PIN);
  // cái này để sau thêm dữ liệu random từ phần cứng
  int rand = random(1, 101); 
  // Kiểm tra xem việc đọc có lỗi không
  if (isnan(h) || isnan(t) || isnan(lightValue)) {
    Serial.println("Lỗi đọc dữ liệu từ DHT11");
  } else {
    // In kết quả ra Serial Monitor
    Serial.print("Độ ẩm: ");
    Serial.print(h);
    Serial.print(" %\t");
    Serial.print("Nhiệt độ: ");
    Serial.print(t);
    Serial.print(" *C\t");
    Serial.print("Nhiệt độ (F): ");
    Serial.print(f);
    Serial.print("\t");
    Serial.print("Giá trị ánh sáng: ");
    Serial.println(lightValue);

    // Gửi dữ liệu lên MQTT
    String payload = String(t) + " "  + String(h) + " " + String(lightValue);
    // Khi yêu cầu thêm data random thì mở ra và đóng cái trên lại
    // String payload = String(t) + " "  + String(h) + " " + String(lightValue) + " " + String(rand);
    client.publish("SENSOR/DATA", String(payload).c_str());
  }

  delay(2000); // Đợi 2 giây trước khi đọc lại
}
