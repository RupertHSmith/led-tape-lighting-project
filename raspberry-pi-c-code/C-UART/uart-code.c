/*
	UART communication on Raspberry Pi using C (WiringPi Library)
	http://www.electronicwings.com
*/
#include <jni.h>
#include "common_UartCode.h"

#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <stdbool.h>
#include <stdint.h>
#include <inttypes.h>

#include <wiringPi.h>
#include <wiringSerial.h>

#define UART_LOAD_MODE 'l'
#define UART_INTENSITY_MODE 's'

/* this must be jint for JNI to function properly */
volatile jint control_panel_intensity = 0;
volatile int serial_port;

void processString(char* stringToProcess)
{
	if (stringToProcess[0] == 'i')
	{
		// then this is an intensity command so set global control_panel_intensity to this val
		char* endPntr = stringToProcess;
        stringToProcess[4] = '\0';
	    control_panel_intensity = strtoumax(stringToProcess + 1, &endPntr, 10);

	/*	printf("Intensity: %d%%\n", control_panel_intensity);
		fflush(stdout);*/

	}
}

JNIEXPORT void JNICALL Java_common_UartCode_setControlPanelIntensity(JNIEnv * env, jobject javaobj, jint intensityVal)
{


	if (serial_port >= 0)
	{
		control_panel_intensity = intensityVal;
		serialPrintf(serial_port,"<<<i%03d>>>",intensityVal);
	}


}

JNIEXPORT void JNICALL Java_common_UartCode_setControlPanelPageIntensity(JNIEnv * env, jobject javaobj, jint intensityVal)
{


	if (serial_port >= 0)
	{
		serialPrintf(serial_port, "<<<%c>>>", UART_INTENSITY_MODE);
		control_panel_intensity = intensityVal;
		serialPrintf(serial_port,"<<<i%03d>>>",intensityVal);
	}


}

JNIEXPORT void JNICALL Java_common_UartCode_setControlPanelPageLoad(JNIEnv * env, jobject javaobj)
{


	if (serial_port >= 0)
	{
		serialPrintf(serial_port,"<<<%c>>>", UART_LOAD_MODE);
	}


}



JNIEXPORT jint JNICALL Java_common_UartCode_getControlPanelIntensity(JNIEnv *env, jobject javaobj)
{
	return control_panel_intensity;
}

JNIEXPORT void JNICALL Java_common_UartCode_main(JNIEnv * env, jobject javaobj)
{
	control_panel_intensity = 0;
  printf("BEGINNING");
  char dat;
  if ((serial_port = serialOpen("/dev/ttyAMA0", 9600)) < 0)		/* open serial port */
  {
    fprintf(stderr, "Unable to open serial device: %s\n", strerror(errno));
    return;
  }

  if (wiringPiSetup() == -1)							/* initializes wiringPi setup */
  {
    fprintf(stdout, "Unable to start wiringPi: %s", strerror(errno));
    return;
  }

	char inputBuffer[10];
	uint8_t bufferPos = 0;

	bool readingString = false;
	bool readingStartChars = true;
	bool readingEndChars = false;

	uint8_t receivedStartChars = 0;
	uint8_t receivedEndChars = 0;


		while(1)
	{
		if(serialDataAvail(serial_port))
		{ 
			dat = serialGetchar(serial_port);		/* receive character serially*/	
		
			if (readingStartChars)
		{
			if (dat == '<')
			{
				receivedStartChars += 1;
				if (receivedStartChars == 3)
				{
					/* we've received all the start chars so switch to read string mode */
					readingStartChars = false;
					readingString = true;
					bufferPos = 0;
				}
			}
			else 
			{
				receivedStartChars = 0;
			}
		}
		else if (readingEndChars)
		{
			if (dat == '>')
			{
				receivedEndChars += 1;
				if (receivedEndChars == 3)
				{
					/* Then we accept this string!! */

					processString(inputBuffer);
					readingStartChars = true;
					readingEndChars = false;
					readingString = false;
					receivedStartChars = 0;
					receivedEndChars = 0;
					bufferPos = 0;
					/* Now reset */
				}
			}
			else 
			{
				//Otherwise we reset all vars and counters and start reading string again
				readingStartChars = true;
				readingEndChars = false;
				readingString = false;
				receivedStartChars = 0;
				receivedEndChars = 0;
				bufferPos = 0;
			}

		}
		else if (readingString)
		{
			if (dat == '>')		/* Then we've read the string - now begin counting end chars */
			{
				readingString = false;
				readingEndChars = true;
				receivedEndChars = 1;
			}
			else 
			{
				inputBuffer[bufferPos] = dat;
				bufferPos++;
			}
		}


		}

		
		
	}

}

