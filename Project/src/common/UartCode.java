package common;

import java.io.IOException;

public class UartCode{
	public native void setControlPanelIntensity(int intensity);
	public native void setControlPanelPageLoad();
	public native void setControlPanelPageIntensity(int intensity);
	public native int getControlPanelIntensity();
	public native void main();

	static {
		System.out.println("ATTEMPTING TO LOAD UART LIBRARY 1201");
		try {
			NativeUtils.loadLibraryFromJar("/libUartCode.so");
		} catch (IOException e){
			e.printStackTrace();
		}
	}
}
