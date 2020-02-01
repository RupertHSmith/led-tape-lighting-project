This script receives updates from the Firebase server and then sends them via two socket connections to the Java program (One socket for alarm requests and one socket for device state updates). It is started simultaneously with the main Java program on the Pi.

The Firebase admin SDK is not supported on Raspberry Pi in Java due to missing and incompatible dependencies.
