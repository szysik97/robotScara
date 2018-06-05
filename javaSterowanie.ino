const int chwytakGora = 2;
const int chwytakDol = 3;
const int joystickRamie1 = 0;
const int joystickRamie2 = 1;

void setup() {
  Serial.begin(9600);
  pinMode(chwytakGora, INPUT_PULLUP);
  pinMode(chwytakDol, INPUT_PULLUP);
}

void loop() {

  int obrotRamie1 = map(analogRead(joystickRamie1), 0, 1023, 0, 2);
  int obrotRamie2 = map(analogRead(joystickRamie2), 0, 1023, 0, 2);

  if(obrotRamie1 == 0)
    Serial.print(1);
  
  else if(obrotRamie1 == 2)
    Serial.print(2);

  else if(obrotRamie2 == 0)
    Serial.print(3);
  
  else if(obrotRamie2 == 2)
    Serial.print(4);

  else if(!digitalRead(chwytakGora))
    Serial.print(5);
  
  else if(!digitalRead(chwytakDol))
    Serial.print(6);

  delay(75);
}
