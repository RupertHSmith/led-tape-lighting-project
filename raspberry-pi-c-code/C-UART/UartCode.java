class UartCode{
	private native int getControlPanelIntensity();
	private native void main();
	
	public static void main (String[] args){
		UartCode uart = new UartCode();
		uart.main();
	}
	static {
		System.loadLibrary("UartCode");
	}
}
