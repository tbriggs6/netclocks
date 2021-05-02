package comms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class SignBoard {

	
	
	public static void main(String args[]) throws IOException
	{
		
		
		JSSCComms comms = new JSSCComms();
		
		String ports[] = comms.getSerialPorts();
		System.out.println("PORTS: ");
		for (String port : ports) { System.out.println(port); }
		
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter com: ");
		String port = sc.next();
		
		comms.setDeviceName(port);
		comms.open();
		
		InputStream is = comms.getInputStream();
		OutputStream os = comms.getOutputStream();
		
		System.out.println("Setting clock");
		
		byte buff[] = { 
				0x00, 0x00, 0x00, 0x00, 0x00,
				0x01, (byte) 0xff, 0x00, 0x02, 'W', 
				'A', 
				'2', '0', '0', '4',
				'0', '4', '0', '2',
				'1', '2', '3', '6', 
				'2', '3', '5',
				0x03,
				'0', '3', '8', 'F',
				0x04
		};
				
				
		os.write(buff);;
		
		while(true) {
			int read = is.read( );
			if ((read < 0) || (read == 0x04)) break;
			System.out.format("%x\n", is.read());
			
		}
		
		
	}
}
