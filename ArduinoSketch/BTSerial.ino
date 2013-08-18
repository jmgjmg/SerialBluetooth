#include <SoftwareSerial.h>
SoftwareSerial myBTSerial(7, 8); // RX, TX

void setup() { 

    Serial.begin(9600);
    myBTSerial.begin(9600);

    Serial.println("Setup done"); 
}

void loop() { 
  if (myBTSerial.available()){
    Serial.write(myBTSerial.read());
  }
  if (Serial.available())
    myBTSerial.write(Serial.read());
}
