// This file is part of the K6BEZ Antenna Analyzer project.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, ord
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.

//
// NOTES FOR KN4SFX FIRMWARE
//
// This version of the firmware is based on the original standard_firmware.ino
// published here: https://github.com/HamRadio360/Antenna-Analyzer
//
// It is the required version of the firmware for use with the K6BEZ
// Antenna Analyzer program available here: https://github.com/NotoriousDBA/Antenna-Analyzer
//
// The functionality of this version is essentially identical to the
// standard firmware, with two major and two minor exceptions:
//
// 1) All data written to, or read from, a PC is done using null-terminated
//    strings. 
// 2) The format of the data returned to a PC during a scan has been changed.
// 3) The lag between pressing the band button and the analyzer switching
//    bands has been eliminated.
// 4) I removed the command that sets the analyzer to a single frequency.
//
// Beyond that, the rest of the changes were just to clean up and streamline
// the code, and make it a little friendlier. The author of these changes
// is me, Chris Mathes (KN4SFX). You can reach me at chris@urbanjaguar.org.
//

// The library we'll use for the LCD.
#include <LiquidCrystal.h>

//
// Macros to associate meaningful names with the pins we're going to use.
//

// Pins for controlling the DDS module.
#define DDS_FQ_UD 4
#define DDS_SDAT 3
#define DDS_SCLK 5
#define DDS_RESET 2

// Pins for reading the forward and reverse voltages on the transmission line.
#define FWD_V A1
#define REV_V A0

// Pins tied to the mode and band buttons on the PCB.
// We don't actually use the mode button in this version of the firmware.
#define MODE_BUTTON A2
#define BAND_BUTTON A3

// The number of milliseconds to delay after detecting a button press.
// This allows the button to settle down, otherwise what the user
// perceives as a single press might actually trigger multiple presses
// on the analyzer.
#define BUTTON_DELAY (300)

// The default number of steps to take through a band.
#define DEFAULT_STEPS 500

// Enum to define our modes of operation.
enum mode {STAND_ALONE, PC};

//
// Reference data for the various bands.
//

// The total number of bands we're defining.
#define NUM_BANDS 11

// Enum for the set of bands we can run sweeps on.
enum band {
  BAND_HF, BAND_160M, BAND_80M, BAND_60M, BAND_40M, BAND_30M,
  BAND_20M, BAND_17M, BAND_15M, BAND_12M, BAND_10M
};

// The starting frequency of each band.
const long band_freq_low[NUM_BANDS] = {
  1000000, 1500000, 2000000, 5000000, 6000000, 9000000,
  13000000, 17000000, 20000000, 24000000, 28000000
};

// The ending frequency of each band.
const long band_freq_high[NUM_BANDS] = {
  30000000, 2300000, 5000000, 6000000, 8000000, 11000000,
  16000000, 19000000, 23000000, 26000000, 30000000
};

// The label to display on the screen for each band.
const char *band_label[NUM_BANDS] = {
  "1-30 MHz", "160m", "80m", "60m", "40m", "30m",
  "20m", "17m", "15m", "12m", "10m"
};

//
// Set up our LCD display, specifying which interface pins it's connected to.
//
// * LCD RS pin to digital pin 8
// * LCD Enable pin to digital pin 9
// * LCD D4 pin to digital pin 15
// * LCD D5 pin to digital pin 14
// * LCD D6 pin to digital pin 16
// * LCD D7 pin to digital pin 10
//
LiquidCrystal lcd(8, 9, 15, 14, 16, 10);

//
// Global variables
//
// These are variables we need to maintain the state of between calls to loop().
//
band selected_band; // The currently selected band.
mode current_mode; // The current mode; i.e. stand-alone or connected to a PC.
byte band_pressed; // Whether the band button has been pressed.
long num_steps; // The number of steps to take across the selected band.
long freq_low, freq_high; // To hold the low and high frequencies of the range we're going to sweep.

//
// Now the code...
//

// Set the DDS module to output the specified frequency.
void set_dds_freq(long freq_hz) {
  // Calculate the DDS word (from AD9850 Datasheet).
  int32_t f = freq_hz * 4294967295 / 125000000;

  // Bit bang the DDS word over the SPI bus.
  for (int bit = 0; bit < 40; bit++) {
    // Set data bit on output pin.
    // The first 32 bits come from f. The last 8 are all 0,
    digitalWrite(DDS_SDAT, bit < 32 ? bitRead(f, bit) : 0);
    // Strobe the clock pin after each bit.
    digitalWrite(DDS_SCLK, HIGH);
    digitalWrite(DDS_SCLK, LOW);
  }

  // Strobe the update pin to tell the DDS to use the new values.
  digitalWrite(DDS_FQ_UD, HIGH);
  delay(1);
  digitalWrite(DDS_FQ_UD, LOW);
  delay(10); // Give the DDS time to settle.
}

// Function to reset the DDS module.
void reset_dds() {
  digitalWrite(DDS_RESET, HIGH);
  delay(1);
  digitalWrite(DDS_RESET, LOW);
  delay(10);

  // For some reason that I'm unaware of, the first time the DDS is set to a
  // frequency after a reset, we have to do it twice. Otherwise the first
  // reading nearly always returns a VSWR of 1:1. In order to keep the rest
  // of the code clean, we'll prime the DDS here at the end of the reset routine.
  // The frequency we're using here (10MHz) is purely arbitrary.
  set_dds_freq(10000000);
}

// Sweep a range of frequencies, measuring the VSWR at each step.
byte perform_sweep(long freq_low, long freq_high, long num_steps, mode current_mode) {
  long freq_step = (freq_high - freq_low) / num_steps;
  int fwd_voltage;
  int rev_voltage;
  double vswr;
  double lowest_vswr = 999;
  long lowest_vswr_freq = freq_low;
  byte band_pressed = 0;
  byte pc_data = 0;

  // Reset the DDS module.
  reset_dds();

  for (long current_freq = freq_low;
    current_freq <= freq_high && band_pressed == 0 && pc_data == 0;
    current_freq += freq_step)
  {
    // Set the DDS to the current frequency.
    set_dds_freq(current_freq);

    // Has the band button been pressed?
    if (digitalRead(BAND_BUTTON) == LOW) {
      // It has. This will end the loop.
      band_pressed = 1;
      delay(BUTTON_DELAY); // Give the button time to settle.
    } else if (Serial.available() > 0) {
      // Also stop if there's data coming from a PC.
      pc_data = 1;
    }
    
    // Read the forward and reverse voltages.
    fwd_voltage = analogRead(FWD_V);
    rev_voltage = analogRead(REV_V);

    if (rev_voltage >= fwd_voltage) {
      // To avoid a divide by zero or negative VSWR then set to max 999.
      vswr = 999;
    } else {
      // Calculate VSWR.
      vswr = ((double)fwd_voltage + (double)rev_voltage) / ((double)fwd_voltage - (double)rev_voltage);
    }

    // Did we find a new lowest value for VSWR in the band?
    if (vswr <= lowest_vswr) {
      // We did. Record it, and its associated frequency.
      lowest_vswr = vswr;
      lowest_vswr_freq = current_freq;
    }

    if (current_mode == PC) {
      // Send current reading back to the PC over the serial line.
      // VSWR is multiplied by 1000 to make the system compatible with the PIC version.
      Serial.print(String(current_freq) + '|' + String(long(vswr * 1000)) + '\0');
    }
  }

  // Send "End" to the PC to indicate the end of the sweep.
  if (current_mode == PC) {
    Serial.print("End\0");
    Serial.flush();
  }

  if (band_pressed == 0 && pc_data == 0) {
    // We weren't interrupted, so write the results of the sweep to the display.
    lcd.setCursor(0, 1);
    lcd.print(lowest_vswr_freq);
    lcd.print(",");
    lcd.print(lowest_vswr);
    lcd.print(":1    ");
  }

  // Let the caller know whether we were interrupted by a button press.
  return band_pressed;
}

// The initialization function for our firmware.
void setup() {
  // Set up the LCD's number of columns and rows:
  lcd.begin(16, 2);

  // Print a message to the LCD.
  lcd.print("Antenna Analyzer");

  // Configure DDS control pins for digital output.
  pinMode(DDS_FQ_UD, OUTPUT);
  pinMode(DDS_SCLK, OUTPUT);
  pinMode(DDS_SDAT, OUTPUT);
  pinMode(DDS_RESET, OUTPUT);

  // Set up the forward and reverse voltage input pins as analog inputs referenced to internal voltage.
  pinMode(FWD_V, INPUT);
  pinMode(REV_V, INPUT);
  analogReference(DEFAULT);

  // Initialize serial communication at 115200 baud.
  Serial.begin(115200);

  // Reset the DDS module.
  reset_dds();

  // Initialize some of our global variables.
  selected_band = BAND_HF;
  current_mode = STAND_ALONE;
  band_pressed = 0;
  num_steps = DEFAULT_STEPS;

  // Wait for a few seconds, then clear the screen.
  delay(5000);
  lcd.clear();
}

// The main execution loop of our firmware.
void loop() {
  String message;
  char command;
  long argument;

  // Are we receiving input from a PC?
  if (Serial.available() > 0) {
    // Yes.
    current_mode = PC;
    band_pressed = 0;

    lcd.clear();
    lcd.setCursor(14, 0);
    lcd.print("PC");

    // Read a block of text terminated with a null character.
    message = String(Serial.readStringUntil('\0'));
    message.toUpperCase();
    message.trim();

    // The last character of the message will be the command.
    command = message.charAt(message.length() - 1);

    // The first part of the message, if any, will be an integer argument.
    if (message.length() > 1) {
      // Even though the function is called toInt(), it really returns a long.
      argument = message.substring(0, message.length() - 1).toInt();
    }

    // Carry out the command.
    switch(command) {
      case 'A':
        // Use the argument as the frequency for the bottom of the band.
        freq_low = argument;
        break;
      case 'B':
        // Use the argument as the frequency for the top of the band.
        freq_high = argument;
        break;
      case 'N':
        // Use the argument to set the number of steps in the sweep.
        num_steps = argument;
        break;
      case 'S':
        // Perform the sweep.
        band_pressed = perform_sweep(freq_low, freq_high, num_steps, current_mode);
        break;
      case '?':
        // Report current configuration to PC
        Serial.print(String(freq_low) + '|' + String(freq_high) + '|' + String(num_steps) + '\0');
        Serial.flush();
        break;
    }
  } else {
    // No serial data was received.
    if (current_mode == STAND_ALONE) {
      lcd.setCursor(0, 0);
      lcd.print(band_label[selected_band]);

      // Run a sweep in the currently-selected band.
      band_pressed = perform_sweep(band_freq_low[selected_band], band_freq_high[selected_band], num_steps,
        current_mode);
    }
  }

  if ((digitalRead(BAND_BUTTON) == LOW) or (band_pressed == 1)) {
    band_pressed = 0;
    delay(BUTTON_DELAY); // Give the button time to settle.

    // Revert to stand-alone mode, and go to the next band in the list.
    current_mode = STAND_ALONE;
    selected_band = (selected_band + 1) % NUM_BANDS;
    num_steps = DEFAULT_STEPS;
    lcd.clear();
  }
}
