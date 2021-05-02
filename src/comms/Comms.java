package comms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Comms {

	public void open( ) throws IOException;
	public void close( ) throws IOException;
	public OutputStream getOutputStream( ) throws IOException;
	public InputStream getInputStream( ) throws IOException;
	void setDeviceName(String name);
	public String[] getSerialPorts( ) throws IOException;
	int inputAvailable( ) throws IOException;
	void flush( ) throws IOException;
}
