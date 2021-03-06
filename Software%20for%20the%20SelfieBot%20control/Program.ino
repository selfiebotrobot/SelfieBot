 //////////////////////////////////////////////////////////////////////////////////////////////////////////
// SelfieBot Base � AlSHex
// 1.0
//
// Create: 27/03/2016
// Modification: 27/03/2016
//
// Description: Software for SelfieBot control. Electronics version Base 1.0
// RU: ��������: ��������� ���������� SelfieBot � ������������ ������ Base 1.0
//////////////////////////////////////////////////////////////////////////////////////////////////////////



#include <Servo.h>



// peripherals connecting
// RU: ����������� ���������
const int Servo1 = 5; // servo 1 [PWM]
const int Servo2 = 6; // servo 2 [PWM]
const int LED_tech = 13; // technological LED [RU: ��������������� ���������]



// declaration and description of modules-functions, pinout and configuration
// RU: ���������� � �������� �������-�������, ���������� � ���������
// ===============================
// ========== Interface ==========
// ===============================
const int UART_Speed = 9600; //UART speed

void _Interf_UART(unsigned int RST, unsigned int *Data);
// RST - reset: 0= normal functioning (RU: ���������� ������); 1= reset [RU: �����]
// Data - command data, control byte array, that the module changes directly in memory
// Ru: Data - ������ �������, ������ ���� ����������, ������� ������ ������ �������� � ������
//
// Connecting to UART occurs via "Serial" Arduino library
// RU: ������������� � UART ���������� � ������� ���������� Serial � Arduino

// ===================================
// ========== Control Servo ==========
// ===================================
Servo servo1;
Servo servo2;

void _Control_Servo1(unsigned int Rst, unsigned int Rst_mode, unsigned int Ctrl, unsigned int Mode); // servo for horizontal rotation [RU: ����������� ��������������� ��������]
void _Control_Servo2(unsigned int Rst, unsigned int Rst_mode, unsigned int Ctrl, unsigned int Mode); // servo for vertical rotation [RU: ����������� ������������� ��������]
// external control [RU: ������� ����������]
// RST - reset: 0= normal functioning (RU: ���������� ������); 1= reset [RU: �����]
// Rst_mode - servo installation type in predetermined position (default): 0 = fast installation (sends the default angle to servo); 1 = smooth installation (adjustable angle is growing slowly at the rate specified in _Control_Servo module parameter) [RU: ��� ��������� ������������ � �������� ��������� �� ���������: 0=������� ��������� (�� ����������� ����� ���� �� ���������); 1= ������� ��������� (��������������� ���� ��������� �������� �� ���������, �������� � ��������� ������ _Control_Servo)]
// Ctrl - servo control data (command, the execution of which depends on the mode of operation Mode) [RU: Ctrl - ������ ���������� �������������� (�������, ���������� ������� ������� �� ������ ������ Mode)]
// Mode - mode: 0 = execution of the command considering timer command execution [RU: Mode - ����� ����������: 0= ���������� ������� � ������ ������� ������� ���������� �������]
//
// Internal settings [RU: ���������� ���������]
// - command values to change (increase or decrease) the angles of rotation and to stop rotation [RU: - �������� ������ ��� ��������� (���������� ��� ����������) ����� �������� � ��������� ��������]
const byte Servo1_Cmd_incr = byte('A'); // command to increase angle of rotation [RU: ������� ���������� ���� ��������]
const byte Servo1_Cmd_decr = byte('D'); // command to decrease angle of rotation [RU: ������� ���������� ���� ��������]
const byte Servo1_Cmd_stop = byte('x'); // stops rotation [RU: ��������� ��������]
const byte Servo1_Cmd_def = byte('R'); // command to setup default angle Pg_default [RU: ������� ��������� ���� �� ��������� Deg_default]
const byte Servo2_Cmd_incr = byte('W'); // increasing angle of rotation [RU: ���������� ���� ��������]
const byte Servo2_Cmd_decr = byte('S'); // decreasing angle of rotation [RU: ���������� ���� ��������]
const byte Servo2_Cmd_stop = Servo1_Cmd_stop; // stops rotation [RU: ��������� ��������]
const byte Servo2_Cmd_def = Servo1_Cmd_def; // command to setup default angle Pg_default [RU: ������� ��������� ���� �� ��������� Deg_default]
// - maximum and minimum values of the angles, the angle of rotation set by default when the module is reset [RU: - ������������ � ����������� �������� �����, ���� �������� ������������ �� ��������� ��� ������ ������]
const int Servo1_Deg_default = 90; // the angle of rotation by default  [RU: ���� �������� �� ���������]
const int Servo1_Deg_min = 0+5; // limiting minimum angle of rotation [RU: ���������� ����������� ���� ��������]
const int Servo1_Deg_max = 180-5; // limiting maximum angle of rotation [RU: ���������� ������������ ���� ��������]
const int Servo2_Deg_default = Servo1_Deg_default; // the angle of rotation by default  [RU: ���� �������� �� ���������]
const int Servo2_Deg_min = Servo1_Deg_min; // limiting minimum angle of rotation [RU: ���������� ����������� ���� ��������]
const int Servo2_Deg_max = Servo1_Deg_max; // limiting maximum angle of rotation [RU: ���������� ������������ ���� ��������]
// rotation speed: non-blocking delay between changes in angles of 1 degree during the rotation, if = 0 - the delay is absent [ms> = 0] [RU: - �������� ��������: ������������� �������� ����� ����������� ����� �� 1 ������ ��� ��������, ���� =0 - �������� �� �������� [��, >=0]]
const long Servo1_Time_rotate_deg1 = 0;
const long Servo2_Time_rotate_deg1 = Servo1_Time_rotate_deg1;
// rotation speed control at reset: the blocking delay, i.e. until a predetermined angle is set, the rest of the program will not operate [ms> 0] [RU: - ��������� �������� �������� ��� ������: ����������� ��������, �.�. ���� �� ����� ���������� �������� ���� ������ �� ���� �������� ��������� ����� ��������� [��, >0]]
const int Servo1_Time_rst = Servo1_Time_rotate_deg1;
const int Servo2_Time_rst = Servo1_Time_rotate_deg1;
// - relevance period for the rotation command (with Mode = 0): after receiving a single command rotation will be stopped after a specified period or after next command is obtained [ms> 0] [RU: - ����� ������������ ������� �� ������� (��� Mode=0): ����� ��������� ����������� ������� ������� ����� ���������� ����� �������� ����� ��� �� ��������� ������ ������� [��, >0]]
const long Servo1_Time_rotate = 2;
const long Servo2_Time_rotate = Servo1_Time_rotate;



// ===============================
// ========== MAIN ===============
// ===============================
unsigned int CMD[1] = {0}; // interface's data, 1 byte [RU: ������ �� ����������, 1 ����]



void setup() {

  // initial configuration of available pins [RU: ��������� ��������� ��������� �����]
  pinMode(Servo1, OUTPUT); analogWrite(Servo1, 0);
  pinMode(Servo2, OUTPUT); analogWrite(Servo2, 0);
  pinMode(LED_tech, OUTPUT); digitalWrite(LED_tech, LOW);

  
  // module's initialization [RU: ������������� �������]
  Serial.begin(UART_Speed);
  servo1.attach(Servo1);
  servo2.attach(Servo2);

  digitalWrite(LED_tech, HIGH); // initialization's indication - begin [RU: ��������� ������������� - ������]
  _Control_Servo1(1, 1, CMD[0], 0); // servos are installed to the default position [RU: ��������� ������������� � ��������� �� ���������]
  _Control_Servo2(1, 1, CMD[0], 0);
  delay(1000);  
  digitalWrite(LED_tech, LOW); // initialization's indication - end [RU: ��������� ������������� - �����]

}



void loop() {

  // reading the interface, the result will be available in CMD [RU: ����� ����������, ��������� ����� �������� � CMD]
  _Interf_UART(0, CMD);

  // execution of control schemes [RU: ���������� ���� ����������]
  _Control_Servo1(0, 1, CMD[0], 0);
  _Control_Servo2(0, 1, CMD[0], 0);

}



// ========== Interface module ==========
void _Interf_UART(unsigned int Rst, unsigned int *Data) { 

unsigned int data_uart;
static unsigned int cnt_byte;

  if (Rst == 0) {
      if (Serial.available() != 0) {
        data_uart = Serial.read();

        switch (cnt_byte) { // check the integrity of the package, if there is at least one failure - reset the reception and begin the search for a new packet's header [RU: �������� ����������� ������, ���� ���� ���� ���� - ���������� ���� � �������� ����� ��������� ������ ������]
          case 0:
            if (data_uart == byte('c')) { cnt_byte++; } else { cnt_byte = 0; }
            Data = 0;
            break;

          case 1:
            if (data_uart == byte('o')) { cnt_byte++; } else { cnt_byte = 0; }
            Data = 0;
            break;
          
          case 2:
            if (data_uart == byte('m')) { cnt_byte++; } else { cnt_byte = 0; }
            Data = 0;
            break;
            
          case 3:
            if (data_uart == byte('=')) { cnt_byte++; } else { cnt_byte = 0; }
            Data = 0;
            break;
            
          case 4: // if the end is reached, then the packet is correct and you may send the command [RU: ���� ����� �� �����, �� ����� ����� � ����� ������ �������]
            cnt_byte = 0;
            Data[0] = data_uart;
            break;
            
          default:
            cnt_byte = 0;
            break;            
        }
     } else {
       Data[0] = 0;
     }
  } else {
    cnt_byte = 0;
    Data[0] = 0;
  }
}



// ========== Control Servo1 module ==========
void _Control_Servo1(unsigned int Rst, unsigned int Rst_mode, unsigned int Cmd, unsigned int Mode) {
// External control [RU: ������� ����������]
// RST - reset: 0= normal functioning (RU: ���������� ������); 1= reset [RU: �����]
// Rst_mode - servo installation type in predetermined position (default): 0 = fast installation (sends the default angle to servo); 1 = smooth installation (adjustable angle is growing slowly at the rate specified in _Control_Servo module parameter) [RU: ��� ��������� ������������ � �������� ��������� �� ���������: 0=������� ��������� (�� ����������� ����� ���� �� ���������); 1= ������� ��������� (��������������� ���� ��������� �������� �� ���������, �������� � ��������� ������ _Control_Servo)]
// Ctrl - servo control data (command, the execution of which depends on the mode of operation Mode) [RU: Ctrl - ������ ���������� �������������� (�������, ���������� ������� ������� �� ������ ������ Mode)]
// Mode - mode: 0 = execution of the command considering timer command execution [RU: Mode - ����� ����������: 0= ���������� ������� � ������ ������� ������� ���������� �������]

// Internal settings [RU: ���������� ���������]
// - command values to change (increase or decrease) the angles of rotation and to stop rotation [RU: - �������� ������ ��� ��������� (���������� ��� ����������) ����� �������� � ��������� ��������]
const byte Cmd_incr = Servo1_Cmd_incr; // ������� ���������� ���� ��������
const byte Cmd_decr = Servo1_Cmd_decr; // ������� ���������� ���� ��������
const byte Cmd_stop = Servo1_Cmd_stop; // ������� ��������� ��������
const byte Cmd_def = Servo1_Cmd_def; // ������� ��������� ���� �� ��������� Deg_default
// - ������������ � ����������� �������� �����, ���� �������� ������������ �� ��������� ��� ������ ������
const int Deg_default = Servo1_Deg_default; // ���� �������� �� ���������
const int Deg_min = Servo1_Deg_min; // ���������� ����������� ���� ��������
const int Deg_max = Servo1_Deg_max; // ���������� ������������ ���� ��������
// - �������� ��������: ������������� �������� ����� ����������� ����� �� 1 ������ ��� ��������, ���� =0 - �������� �� �������� [��, >=0]
const long Time_rotate_deg1 = Servo1_Time_rotate_deg1;
// - ��������� �������� �������� ��� ������: ����������� ��������, �.�. ���� �� ����� ���������� �������� ���� ������ �� ���� �������� ��������� ����� ��������� [��, >0]
const int Time_rst = Servo1_Time_rst;
// - ����� ������������ ������� �� ������� (��� Mode=0): ����� ��������� ����������� ������� ������� ����� ���������� ����� �������� ����� ��� �� ��������� ������ ������� [��, >0]
const long Time_rotate = Servo1_Time_rotate;

static signed int deg; // ���� �������� �� �����������
static unsigned int incr, decr; // ����������� ������� ��� ��������� ���������� ��������
static unsigned long time_cmd_detect, time_rotate_detect = 0; // �������� ������� ��������� ������� � ������� ���������� ��������� ���� ��������

  if (Rst == 0 & Cmd != Cmd_def) {
 
    if (Mode == 0) { // ���������� ������� � ������ ������� ������� ���������� �������
      if (Cmd == Cmd_incr) {
        incr = 1;
        decr = 0;
        time_cmd_detect = millis();
      } else if (Cmd == Cmd_decr) {
        incr = 0;
        decr = 1;
        time_cmd_detect = millis();
      } else if (Cmd == Cmd_stop) {
        incr = 0;
        decr = 0;      
      }

      if (millis() - time_cmd_detect > Time_rotate) {
        incr = 0;
        decr = 0;
      }
    }

    if (Time_rotate_deg1 == 0) { // rotation algorithm [RU: �������� ��������]
        if (incr == 1) {
          if (deg < Deg_max) { deg++; } // rotate towards 180 [RU: ������������ � 180]
        } else if (decr == 1) { 
          if (deg > Deg_min) { deg--; } // rotate towards 0 [RU: ������������ � 0]
        }
        servo1.write(deg);
    } else {
      if (millis() - time_rotate_detect > Time_rotate_deg1) { // Delay while rotating - rotation speed control [RU: �������� ��� �������� - ������������� �������� ��������]
        if (incr == 1) {
          if (deg < Deg_max) { // rotate towards 180 [RU: ������������ � 180]
            deg++;
            time_rotate_detect = millis();
          }
        } else if (decr == 1) { // rotate towards 0 [RU: ������������ � 0]
          if (deg > Deg_min) {
            deg--;
            time_rotate_detect = millis();
          }
        }
        servo1.write(deg);
      }
    }
    
  } else {

    // default settings [RU: ��������� �� ���������]
    while (deg != Deg_default) {
      if (Rst_mode == 0) {
        servo1.write(Deg_default);
        deg = Deg_default;
      } else {
        if (deg < Deg_default) { deg++; } else { deg--; }
        servo1.write(deg);
        delay(Time_rst);
      }
    }

    incr = 0;
    decr = 0;
  }
}



// ========== Control Servo2 module ==========
void _Control_Servo2(unsigned int Rst, unsigned int Rst_mode, unsigned int Cmd, unsigned int Mode) {
// external control [RU: ������� ����������]
// RST - reset: 0= normal functioning (RU: ���������� ������); 1= reset [RU: �����]
// Rst_mode - ��� ��������� ������������ � �������� ��������� �� ���������: 0=������� ��������� (�� ����������� ����� ���� �� ���������); 1= ������� ��������� (��������������� ���� ��������� �������� �� ���������, �������� � ��������� ������ _Control_Servo)
// Ctrl - ������ ���������� �������������� (�������, ���������� ������� ������� �� ������ ������ Mode)
// Mode - ����� ����������: 0= ���������� ������� � ������ ������� ������� ���������� �������

// ���������� ���������
// - �������� ������ ��� ��������� (���������� ��� ����������) ����� �������� � ��������� ��������
const byte Cmd_incr = Servo2_Cmd_incr; // ������� ���������� ���� ��������
const byte Cmd_decr = Servo2_Cmd_decr; // ������� ���������� ���� ��������
const byte Cmd_stop = Servo2_Cmd_stop; // ������� ��������� ��������
const byte Cmd_def = Servo2_Cmd_def; // ������� ��������� ���� �� ��������� Deg_default
// - ������������ � ����������� �������� �����, ���� �������� ������������ �� ��������� ��� ������ ������
const int Deg_default = Servo2_Deg_default; // ���� �������� �� ���������
const int Deg_min = Servo2_Deg_min; // ���������� ����������� ���� ��������
const int Deg_max = Servo2_Deg_max; // ���������� ������������ ���� ��������
// - �������� ��������: ������������� �������� ����� ����������� ����� �� 1 ������ ��� ��������, ���� =0 - �������� �� �������� [��, >=0]
const long Time_rotate_deg1 = Servo2_Time_rotate_deg1;
// - ��������� �������� �������� ��� ������: ����������� ��������, �.�. ���� �� ����� ���������� �������� ���� ������ �� ���� �������� ��������� ����� ��������� [��, >0]
const int Time_rst = Servo2_Time_rst;
// - ����� ������������ ������� �� ������� (��� Mode=0): ����� ��������� ����������� ������� ������� ����� ���������� ����� �������� ����� ��� �� ��������� ������ ������� [��, >0]
const long Time_rotate = Servo2_Time_rotate;

static unsigned int deg; // ���� �������� �� �����������
static unsigned int incr, decr; // ����������� ������� ��� ��������� ���������� ��������
static unsigned long time_cmd_detect, time_rotate_detect = 0; // �������� ������� ��������� ������� � ������� ���������� ��������� ���� ��������

  if (Rst == 0 & Cmd != Cmd_def) {
 
    if (Mode == 0) { // ���������� ������� � ������ ������� ������� ���������� �������
      if (Cmd == Cmd_incr) {
        incr = 1;
        decr = 0;
        time_cmd_detect = millis();
      } else if (Cmd == Cmd_decr) {
        incr = 0;
        decr = 1;
        time_cmd_detect = millis();
      } else if (Cmd == Cmd_stop) {
        incr = 0;
        decr = 0;
      }

      if (millis() - time_cmd_detect > Time_rotate) {
        incr = 0;
        decr = 0;
      }
    }

    if (Time_rotate_deg1 == 0) { // rotation algorithm [RU: �������� ��������]
        if (incr == 1) {
          if (deg < Deg_max) { deg++; } // rotate towards 180 [RU: ������������ � 180]
        } else if (decr == 1) { 
          if (deg > Deg_min) { deg--; } // rotate towards 0 [RU: ������������ � 0]
        }
        servo2.write(deg);
    } else {
      if (millis() - time_rotate_detect > Time_rotate_deg1) { // Delay while rotating - rotation speed control [RU: �������� ��� �������� - ������������� �������� ��������]
        if (incr == 1) {
          if (deg < Deg_max) { // rotate towards 180 [RU: ������������ � 180]
            deg++;
            time_rotate_detect = millis();
          }
        } else if (decr == 1) { // rotate towards 0 [RU: ������������ � 0]
          if (deg > Deg_min) {
            deg--;
            time_rotate_detect = millis();
          }
        }
        servo2.write(deg);
      }
    }
    
  } else {

    // default settings [RU: ��������� �� ���������]
    while (deg != Deg_default) {
      if (Rst_mode == 0) {
        servo2.write(Deg_default);
        deg = Deg_default;
      } else {
        if (deg < Deg_default) { deg++; } else { deg--; }
        servo2.write(deg);
        delay(Time_rst);
      }
    }

    incr = 0;
    decr = 0;
  }
}
