/*
	UART communication on Raspberry Pi using C (WiringPi Library)
	http://www.electronicwings.com
*/
#include "uart-code.h"

#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <stdbool.h>
#include <stdint.h>
#include <inttypes.h>

#include <wiringPi.h>
#include <wiringSerial.h>

volatile uint8_t control_panel_intensity = 0;

void processString(char* stringToProcess)
{
	if (stringToProcess[0] == 'i')
	{
		// then this is an intensity command so set global control_panel_intensity to this val
		char* endPntr = stringToProcess + 3;
		control_panel_intensity = strtoumax(stringToProcess + 1, &endPntr, 10);

		printf("Intensity: %d%%\n", control_panel_intensity);
		fflush(stdout);

	}
}

int getControlPanelIntensity()
{
	return control_panel_intensity;
}

int main()
{
  printf("BEGINNING");
  int serial_port;
  char dat;
  if ((serial_port = serialOpen("/dev/ttyAMA0", 9600)) < 0)		/* open serial port */
  {
    fprintf(stderr, "Unable to open serial device: %s\n", strerror(errno));
    return 1;
  }

  if (wiringPiSetup() == -1)							/* initializes wiringPi setup */
  {
    fprintf(stdout, "Unable to start wiringPi: %s", strerror(errno));
    return 1;
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

