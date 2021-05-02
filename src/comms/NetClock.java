package comms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.joda.time.DateTime;

public class NetClock {

	
	
	
	
	public static void main(String args[]) throws UnknownHostException, IOException 
	{
		
		DateTime time = new DateTime( );
		String strtime = String.format("%04d%02d%02d%d%02d%02d",
				time.getYear(),
				time.getMonthOfYear(),
				time.getDayOfMonth(),
				time.getHourOfDay(),
				time.getMinuteOfHour(),
				time.getSecondOfMinute());
		
		
	//	String cmd = "~128~E" + strtime + "\r\r";
		
		String msg = "Hello \\gWorld";
		String cmd = "~128~f01S\\b\\s" + msg + "\\Z8\r\r\r";
		byte bb[] = cmd.getBytes();
		System.out.println("Sending: " + bb.length + " bytes :");
		for (int i = 0; i < bb.length; i++) {
			System.out.format(" %x", bb[i]);
		}
		//Socket socket = new Socket("157.160.204.50",8080);
		Socket socket = new Socket("157.160.205.108",8080);
		OutputStream out = socket.getOutputStream();
		InputStream in = socket.getInputStream();
		
		out.write(cmd.getBytes());
		
		
	}
	
}
