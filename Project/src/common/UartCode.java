package common;

import java.io.IOException;

public class UartCode{
	public native int getControlPanelIntensity();
	public native void main();
	
	/*public static void main (String[] args){
		UartCode uart = new UartCode();
		uart.main();
	}*/

	static {
		System.out.println("ATTEMPTING TO LOAD UART 10 26");
		try {
			NativeUtils.loadLibraryFromJar("/libUartCode.so");
		} catch (IOException e){
			e.printStackTrace();
		}
		System.out.println("LOADED UART");
	}
}
