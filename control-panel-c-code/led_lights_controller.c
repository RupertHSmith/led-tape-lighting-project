/* COMP2215 Task 5---SKELETON */

#include "os.h"
#include "ui_functions.h"
#include <stdbool.h>
#include <stdlib.h>

#define UART_LOAD_MODE 'l'
#define UART_INTENSITY_MODE 's'
#define UART_SET_INTENSITY 'i'

void usart_init_interrupts();
int read_uart(int);
void process_input(char*);
bool process_char(char);
int update_dial(int);
int collect_delta(int);
int check_switches(int);


FIL File;                   /* FAT File */

volatile bool positionChanged = true;
volatile int position = 0;

volatile char inputBuffer[4];
volatile uint8_t bufferPos;

volatile bool readingString;
volatile bool readingStartChars;
volatile bool readingEndChars;

volatile uint8_t receivedStartChars;
volatile uint8_t receivedEndChars;



void main(void) {
    os_init();


	bufferPos = 0;
	readingString = false;
	readingStartChars = true;
	readingEndChars = false;
	receivedStartChars = 0;
	receivedEndChars = 0;

	init_ui_functions();
	usart_init_interrupts();
    /* Process UART char every ms */
	os_add_task( collect_delta,   15, 1);
    os_add_task( check_switches,  90, 1);	

    sei();
    for(;;){}

}

/* MOVE THIS TO ANOTHER FILE AT SOME POINT */

void usart_init_interrupts()
{
	UCSR1B |= _BV(RXCIE1);
}

ISR(USART1_RX_vect)
{ 
	/* Disable interrupts */
	cli();

	/* UDR1 must always be read or the ISR will be immediately called after terminating */
	uint8_t received_char = UDR1;

	if(process_char(received_char))
	{
		/* Then we've read a complete input so process this input buffer */
		process_input(inputBuffer);
	}	

	/* enable interrupts */
	sei();
}

void process_input(char* string)
{
	if (string[0] == UART_SET_INTENSITY )
	{
		/* Then it is an intensity request so parse next 3 chars as int */
		string[4] = '\0'; 			/* we must terminate the string */
		int intensityVal = atoi(string + 1);
		if (intensityVal >= 0 && intensityVal <= 100)
		{
			position = intensityVal;
			positionChanged = true;
		}
	}
	else if (string[0] == UART_LOAD_MODE)
	{
		set_ui_page(Load);
	}
	else if (string[0] == UART_INTENSITY_MODE)
	{
		set_ui_page(Intensity);
	}
}

bool process_char(char dat)
{
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
				/* Then we accept this string so set vars and return true */
				readingStartChars = true;
				readingEndChars = false;
				readingString = false;
				receivedStartChars = 0;
				receivedEndChars = 0;
				bufferPos = 0;
				return true;
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
	/* We have not read a string */
	return false;
}


int collect_delta(int state) {
	int delta = os_enc_delta();
	if (delta)
	{	
		position += delta;
		if (position > 100)
			position = 100;
		else if (position < 0)
			position = 0;

		positionChanged = true;

		//output to UART
		printf("<<<i%03d>>>\n", position);
	}

	if (positionChanged)
	{
		//Now set display
		set_intensity_display(position);
		positionChanged = false;	
	}

	return state;
}


int check_switches(int state) {

	if (get_switch_press(_BV(SWN))) {

	}

	if (get_switch_press(_BV(SWE))) {

	}

	if (get_switch_press(_BV(SWS))) {

	}

	if (get_switch_press(_BV(SWW))) {

	}

	if (get_switch_long(_BV(SWC))) {
		if (position)
		{
			position = 0;
			printf("<<<i%03d>>>\n", position);
			positionChanged = true;
		}
		else 
		{
			//otherwise lights already off so set intensity to max
			position = 100;
			printf("<<<i%03d>>>\n", position);
			positionChanged = true;
		}

		//set_intensity_display(57);
		/*f_mount(&FatFs, "", 0);
		if (f_open(&File, "myfile.txt", FA_WRITE | FA_OPEN_ALWAYS) == FR_OK) {
			f_lseek(&File, f_size(&File));
			f_printf(&File, "Encoder position is: %d \r\n", position);
			f_close(&File);
			display_string("Wrote position\n");
		} else {
			display_string("Can't write file! \n");
		}*/

	}

	if (get_switch_short(_BV(SWC))) {

	}

	if (get_switch_rpt(_BV(SWN))) {

	}

	if (get_switch_rpt(_BV(SWE))) {

	}

	if (get_switch_rpt(_BV(SWS))) {

	}

	if (get_switch_rpt(_BV(SWW))) {

	}

	if (get_switch_rpt(SWN)) {

	}


	if (get_switch_long(_BV(OS_CD))) {

	}

	return state;
}
