#include "ui_functions.h"

uint8_t intensity_display_digit_1;
uint8_t intensity_display_digit_2;
uint8_t intensity_display_digit_3;

void init_ui_functions()
{
    clear_screen();
    intensity_display_digit_1 = 0;
    intensity_display_digit_2 = 0;
    intensity_display_digit_3 = 0;
    display_digit(0,16,56);
    display_digit(0,112,56);
    display_digit(0, 208, 56);
}

void set_intensity_display(uint8_t intensity)
{
    //will change this for efficiency
    //clear_screen();

    uint8_t new_display_digit_1;
    uint8_t new_display_digit_2;
    uint8_t new_display_digit_3;

    //display char 1
    if (intensity == 100)
    {
        new_display_digit_1 = 1;
        new_display_digit_2 = 0;
        new_display_digit_3 = 0;
    }
    else
    {
        new_display_digit_1 = 0;
        new_display_digit_2 = intensity / 10;
        new_display_digit_3 = intensity % 10;
    }

    //now set digits if different to save re-drawing

    if (new_display_digit_1 != intensity_display_digit_1)
    {
        display_digit(new_display_digit_1,16,56);
        intensity_display_digit_1 = new_display_digit_1;
    }

    if (new_display_digit_2 != intensity_display_digit_2)
    {
        display_digit(new_display_digit_2,112,56);
        intensity_display_digit_2 = new_display_digit_2;
    }

    if (new_display_digit_3 != intensity_display_digit_3)
    {
        display_digit(new_display_digit_3, 208, 56);
        intensity_display_digit_3 = new_display_digit_3;
    }
}