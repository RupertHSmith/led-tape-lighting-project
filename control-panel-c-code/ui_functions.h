#include "os.h"

typedef enum { Load, Intensity } CurrentPage;

typedef struct {
    CurrentPage page;
} ui_mode;

void init_ui_functions();
ui_mode get_ui_mode();
void set_ui_page(CurrentPage page);
void set_intensity_display(uint8_t intensity);
void init_intensity_display();