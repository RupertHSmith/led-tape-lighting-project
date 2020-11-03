# led-tape-lighting-project
This project is made up of two main components, a Java and Python program that runs on a Raspberry PI and an Android application. The Raspberry Pi controls some connected SMD5050 RGB LED tape and the application is connected to a Google Firebase database. This allows the lights to be controlled from the mobile application.

The MOSFET transistors switching the 12V SMD5050 RGB tape are connected as follows; RED: 17, GREEN: 22, BLUE: 24. This could be reconfigured if necessary.

The GPIO pins are controlled by the PIGPIOD socket interface directly from Java. The PIGPIOD service is started within the Java program (it may be running already before the application is started).

Pins 17, 22, 24 are configured to run PWM at 100Hz with a dutycycle resolution of 10 Bit (i.e. 0-1023). Within the Java program the control of each RGB channel is 8 Bit since due to the exponential nature of the dutycycle against apparent luminosity of the LEDs, the 8 Bit can be translated via a function such that fine control at lower brightnesses is not lost. (This compromises the fine control at higher brightnesses however this is not perceivable to the eye). This translation curve can be found in the documentation.


![image](https://github.com/RupertHSmith/led-tape-lighting-project/blob/master/img/device/IMG_20201103_173257.jpg)
![image](https://github.com/RupertHSmith/led-tape-lighting-project/blob/master/img/device/IMG_20201103_173328.jpg)

<img src="https://github.com/RupertHSmith/led-tape-lighting-project/blob/master/img/app/Screenshot_20201103-173508.jpg" width="200"><img src="https://github.com/RupertHSmith/led-tape-lighting-project/blob/master/img/app/Screenshot_20201103-173520.jpg" width="200"><img src="https://github.com/RupertHSmith/led-tape-lighting-project/blob/master/img/app/Screenshot_20201103-173533.jpg" width="200"><img src="https://github.com/RupertHSmith/led-tape-lighting-project/blob/master/img/app/Screenshot_20201103-173539.jpg" width="200"><img src="https://github.com/RupertHSmith/led-tape-lighting-project/blob/master/img/app/Screenshot_20201103-173545.jpg" width="200">
