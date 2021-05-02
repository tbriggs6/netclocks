package comms;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeSet;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class JSSCComms implements Comms {

	String portName;
	SerialPort port;
	OutputStream debugOut;

	JSSCInputStream jis;
	JSSCOutputStream jos;

	private final static boolean DEBUG_COMMS = true;

	@Override
	public void open() throws IOException {
		port = new SerialPort(portName);

		jis = new JSSCInputStream();
		jos = new JSSCOutputStream();

		try {

			port.openPort();

			port.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			port.addEventListener(jis);

			if (DEBUG_COMMS) {
				setDebug(System.err);
				System.err
						.println("----------- JSSCComms I/O stream tracing enable ---------------");
			}
		} catch (SerialPortException e) {
			throw new IOException(e);
		}
	}

	public void setDebug(OutputStream out) {
		this.debugOut = out;
	}

	@Override
	public void close() throws IOException {
		try {
			port.closePort();
		} catch (SerialPortException e) {
			throw new IOException(e);
		}
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return this.jos;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return this.jis;
	}

	@Override
	public void setDeviceName(String name) {
		portName = name;
	}

	 private static String[] getMacOSXPortNames() {
	        String[] returnArray = new String[]{};
	        File dir = new File("/dev");
	        if(dir.exists() && dir.isDirectory()){
	            File[] files = dir.listFiles();
	            if(files.length > 0){
	                TreeSet<String> portsTree = new TreeSet<String>();
	                ArrayList<String> portsList = new ArrayList<String>();
	                for(File file : files){
	                    if(!file.isDirectory() && !file.isFile() && file.getName().matches("(cu|tty).(serial.*|usbserial.*|usbmodem.*)")){
	                        portsTree.add("/dev/" + file.getName());
	                    }
	                }
	                for(String portName : portsTree){
	                    portsList.add(portName);
	                }
	                returnArray = portsList.toArray(returnArray);
	            }
	        }
	        return returnArray;
	    }

	
	@Override
	public String[] getSerialPorts() throws IOException {
		
		if (System.getProperty("os.name").startsWith("Mac OS X"))
			return getMacOSXPortNames();
		else
			return SerialPortList.getPortNames();
	}

	private class JSSCInputStream extends java.io.InputStream implements
			SerialPortEventListener {

		LinkedList<Byte> buff;

		public JSSCInputStream() {
			buff = new LinkedList<Byte>();
		}

		@Override
		public int read() throws IOException {
			int value;

			synchronized (buff) {
				while (buff.size() == 0)
					try {
						buff.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				value = buff.removeFirst();
				return value;
			}
		}

		@Override
		public int available() throws IOException {
			synchronized (buff) {
				return buff.size();
			}
		}

		@Override
		public int read(byte[] arg0, int arg1, int arg2) throws IOException {
			assert (arg0 != null);
			assert (arg1 < arg0.length);
			assert (arg1 + arg2 < arg0.length);
			int i = 0;

			synchronized (buff) {
			
				if (buff.size() == 0) {
					try {
						buff.wait();
					}
					catch(InterruptedException E)
					{
						;
					}
				}
				
				i = 0;
				while ((buff.size() > 0) && (i < arg2)) {
					arg0[arg1 + i] = buff.removeFirst();
					i++;
				}
			}

			return i;
		}

		@Override
		public int read(byte[] arg0) throws IOException {
			assert (arg0 != null);

			int i = 0;
			

			synchronized (buff) {
			
				if (buff.size() == 0) {
					try {
						buff.wait();
					}
					catch(InterruptedException E)
					{
						;
					}
				}
				
				while ((buff.size() > 0) && (i < arg0.length))
					arg0[i++] = buff.removeFirst();
				
			}
			return i;
		}

		@Override
		public void serialEvent(SerialPortEvent arg0) {
			if (arg0.isRXCHAR()) {
				try {
					int n = arg0.getEventValue();
					byte data[] = port.readBytes(n);
					synchronized (buff) {

						for (int i = 0; i < n; i++) {
							buff.add(data[i]);
							if (debugOut != null)
								debugOut.write(data[i]);
						}

						buff.notifyAll();

					}
				} catch (SerialPortException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

		}

	}

	private class JSSCOutputStream extends OutputStream {
		@Override
		public void write(int b) throws IOException {
			try {
				port.writeByte((byte) b);
				if (debugOut != null) {
					debugOut.write(b);
					debugOut.flush();
				}
			} catch (SerialPortException e) {
				throw new IOException(e);
			}
		}

	}

	@Override
	public int inputAvailable() throws IOException {
		return jis.available();
	}

	@Override
	public void flush() throws IOException {
		synchronized (jis.buff) {
			jis.buff.clear();
		}

		jos.flush();
	}

}
