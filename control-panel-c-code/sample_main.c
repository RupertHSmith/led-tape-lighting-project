/* COMP2215 Task 5---SKELETON */

#include "os.h"
#include "ui_functions.h"


int blink(int);
int update_dial(int);
int collect_delta(int);
int check_switches(int);


FIL File;                   /* FAT File */

int position = 0;



void main(void) {
    os_init();

	init_ui_functions();
    os_add_task( blink,            30, 1);
    os_add_task( collect_delta,   100, 1);
    os_add_task( check_switches,  100, 1);	

    sei();
    for(;;){}

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

		//Now set display
		set_intensity_display(position);	

		//output to UART
		printf("<<<i%d>>>\n", position);


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
			printf("<<<i%d>>>\n", position);
			set_intensity_display(position);
		}
		else 
		{
			//otherwise lights already off so set intensity to max
			position = 100;
			printf("<<<i%d>>>\n", position);
			set_intensity_display(position);
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




int blink(int state) {
	static int light = 0;
	uint8_t level;

	if (light < -120) {
		state = 1;
	} else if (light > 254) {
		state = -20;
	}


	/* Compensate somewhat for nonlinear LED
           output and eye sensitivity:
        */
	if (state > 0) {
		if (light > 40) {
			state = 2;
		}
		if (light > 100) {
			state = 5;
		}
	} else {
		if (light < 180) {
			state = -10;
		}
		if (light < 30) {
			state = -5;
		}
	}
	light += state;

	if (light < 0) {
		level = 0;
	} else if (light > 255) {
		level = 255;
	} else {
		level = light;
	}

	os_led_brightness(level);
	return state;
}
