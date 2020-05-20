#include "ui_functions.h"

uint8_t intensity_display_digit_1;
uint8_t intensity_display_digit_2;
uint8_t intensity_display_digit_3;

ui_mode current_ui_mode;

void init_ui_functions()
{
    current_ui_mode.page = Intensity;
    set_ui_page(current_ui_mode.page);
}

ui_mode get_ui_mode()
{
    return current_ui_mode;
}

void set_ui_page(CurrentPage page)
{
    if (page == Load)
    {
        clear_screen();
    }
    else if (page == Intensity)
    {
        clear_screen();
        init_intensity_display();
    }
}

void init_intensity_display()
{
    intensity_display_digit_1 = 5;
    intensity_display_digit_2 = 5;
    intensity_display_digit_3 = 5;
    set_intensity_display(0);
}

void set_intensity_display(uint8_t intensity)
{    
    if (current_ui_mode.page == Intensity)
    {

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
}