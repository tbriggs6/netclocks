package comms;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.joda.time.DateTime;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.FormSpecs;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.JList;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.event.ActionEvent;
import javax.swing.JSeparator;
import java.awt.Font;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

public class Dialog extends JFrame {

	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private JList list;
	private JComboBox color;
	private JComboBox mode;
	private JComboBox timeMode;
	private JComboBox dateMode;
	private JComboBox comboBox;
	private JCheckBox chckbxAudibleAlert;
	private JComboBox fontBox;
	
	final String colors[] = { "Red", "Orange", "Yellow", "Green", "Rainbow", "Layered" };
	final String colorCodes[] = { "b", "d", "f", "h", "j", "i" };

	final String fonts[] = { "5x6 Short", "5x11 Wide", "7x6 Default", "7x11 Wide", "7x9", "7x17 Wide", "Small" };
	final String fontCodes[] = { "q", "r", "s", "t", "u", "v", "w" };
	
	final String modes[] = { "Center", "Cyclic", "Immediate", "Scroll Up", "Flash", "Shoot", "Pac Man" };
	final String modeCodes[] = { "J", "A", "B", "K", "V", "U", "S" };
	
	final String timeModes[] = { "12 hour, no sec", "12 hour am/pm", "24 hour, no sec" , "none"};
	final String timeCodes[] = { "D", "E", "F", "" };
	
	final String dateModes[] = { "none", "mm/dd/yy", "yy/mm/dd", "mm-dd-yyyy", "dd/mm/yyyy" };
	final String dateCodes[] = { "", "A", "B", "C", "a" };
	
	final Timer timer = new Timer( );
	
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			Dialog dialog = new Dialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	void display_time( )
	{
		display_time( getDateCode(), getTimeCode(), getModeCode( ), getColorCode(), getFontCode());
		
	}
	
	void display_time(String dateCode, String timeCode, String modeCode, String colorCode, String fontCode)
	{
		
		String cmd = "~128~f01" + modeCode + "\\" + colorCode + "\\" + fontCode;
		
		
		if ((!dateCode.equals("")) && (!timeCode.equals(""))) {
			cmd += "\\" + colorCode + "\\Z8\\Z8^" + timeCode + "\\Z8\\Z8";
			cmd += "\r" + modeCode + "\\" + colorCode + "\\" + fontCode + "\\Z8^" + dateCode; 
		}
		else if (!dateCode.equals("")) {
			cmd = cmd + "^" + dateCode;
			
		}
		else if (!timeCode.equals("")) {
			cmd = cmd + "^" + timeCode;
		}
		
		cmd += "\r\r\r";

		System.out.println(cmd);
		
		send_string(cmd);
	}
	
	void set_time( )
	{
		System.out.println("Setting time");
		DateTime time = new DateTime( );
		
		String strtime = String.format("%1d0%02d%02d%02d%d%02d%02d",
				time.getDayOfWeek(),
				time.getYear() % 100,
				time.getMonthOfYear(),
				time.getDayOfMonth(),
				time.getHourOfDay(),
				time.getMinuteOfHour(),
				time.getSecondOfDay());
		
		System.out.println("Setting time " + strtime);
		String cmd = "~128~E" + strtime + "\r\r";
		send_string(cmd);
	}
	
	void send_message( )
	{
		send_message(getTextMessage().getText(), getModeCode( ), getColorCode( ), getFontCode( ), isAudibleAlert());
	}
	
	void send_message(String message, String modeCode, String colorCode, String fontCode, boolean audible)
	{
		  String cmd = "~128~f01" + modeCode + "\\" + colorCode + "\\" + fontCode;
		
		if (audible)
			cmd += "^q\\Z1";
		else 
			cmd += "\\Z8";
		
		cmd += message + "\r\r\r";
		
		System.out.println("Sending message " + message);
		send_string(cmd);
	}
	
	
	
	void send_string(String bytes)
	{
		byte bb[] = bytes.getBytes();
		System.out.println("Sending: " + bb.length + " bytes :");
		for (int i = 0; i < bb.length; i++) {
			System.out.format(" %x", bb[i]);
		}
		
		Object[] selected = getList().getSelectedValues();
		final JFrame parent = this;
		
		LinkedList<Object> deadKids = new LinkedList<Object>( );
		
		LinkedList<Thread> threads = new LinkedList<Thread>( );
		for (Object O : selected) {
			
			Thread T = new Thread( new Runnable() {
				
				@Override
				public void run() {
					
					String addr = (String) O;
					try {
						System.out.println("Sending to " + addr);
						Socket socket = new Socket(addr,8080);
						OutputStream out = socket.getOutputStream();
						
						out.write(bytes.getBytes());
						out.flush();
						out.close( );
						socket.close();
						
					}
					catch(IOException E)
					{
						deadKids.add(O);
					}
					
				}
			});
			
			T.start();
			threads.add(T);
		}
		System.out.println("Done");

		for (Thread T : threads) { 
			
			try {
				T.join(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (deadKids.size() >0) {
			
			if (deadKids.size() < 16) {
				StringBuffer buff = new StringBuffer("The following nodes did not respond: ");
				for (Object O : deadKids) { buff.append(" "); buff.append(O); }
				JOptionPane.showMessageDialog(null, buff.toString(), "Error", JOptionPane.ERROR_MESSAGE);
			}
			else {
				JOptionPane.showMessageDialog(null, "More than 16 nodes failed to respond.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else {
			JOptionPane.showMessageDialog(null, "Updated " + selected.length + "signs", "OK", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	/**
	 * Create the dialog.
	 */
	public Dialog() {
		setBounds(100, 100, 690, 331);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("left:default"),
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("left:max(70dlu;default)"),
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("29dlu"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		{

			String addrs[] = null;
			try {
				addrs = getAddresses( );
			}
			catch(IOException E) {
				JOptionPane.showMessageDialog(this, "Error Loading Addresses");
			}
		}
		{
			{
				JLabel lblNewLabel = new JLabel("Networked Signage");
				lblNewLabel.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 18));
				contentPanel.add(lblNewLabel, "4, 2, 5, 1");
			}
			{
				
				String addrs[] = null;
				try {
					addrs = getAddresses();
				}
				catch(IOException E) {
					JOptionPane.showMessageDialog(null,"Error: could not read clocks.txt");
				}
				{
					JLabel lblDevices = new JLabel("Devices");
					contentPanel.add(lblDevices, "12, 2");
				}
				
				{
					textField = new JTextField();
					contentPanel.add(textField, "4, 4, 7, 1, fill, default");
					textField.setColumns(10);
				}
				list = new JList(addrs);
				list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				list.setSelectionInterval(0, addrs.length-1);
				
				contentPanel.add(list, "12, 4, 11, 16, fill, fill");
			}
			JButton btnNewButton = new JButton("Send Message");
			btnNewButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					
					send_message( );
				}
			});
			contentPanel.add(btnNewButton, "4, 6, left, default");
			{
				{
					chckbxAudibleAlert = new JCheckBox("Audible Alert?");
					chckbxAudibleAlert.setHorizontalAlignment(SwingConstants.LEFT);
					contentPanel.add(chckbxAudibleAlert, "8, 6, left, default");
				}
			}
			{
				JButton btnShelterInPlace = new JButton("Shelter in Place");
				btnShelterInPlace.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						send_message("Shelter In Place", "V", "b", "s", true);
					}
				});
				contentPanel.add(btnShelterInPlace, "4, 8");
			}
			JButton btnOk = new JButton("All Clear");
			btnOk.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					send_message("All Clear", "V", "h", "s", true);
					int rc = JOptionPane.showConfirmDialog(null, "Schedule normal time display?","Auto Schedule", JOptionPane.YES_NO_CANCEL_OPTION);
					if (rc == JOptionPane.YES_OPTION) {
						timer.schedule(new TimerTask( ) {
	
							@Override
							public void run() {
								
								display_time("","D","B","h","s");
							}
							
						}, 5000);
					}
				}
					
			});
			{
				JButton btnTestAlert = new JButton("Test Alert");
				btnTestAlert.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						send_message("1...2...3... Test Alert!!!", "v", "b", "s", true);
					}
				});
				contentPanel.add(btnTestAlert, "8, 8");
			}
			contentPanel.add(btnOk, "10, 8");
			{
				JLabel lblColor = new JLabel("Color:");
				contentPanel.add(lblColor, "4, 10");
			}
			{
				JLabel lblMode = new JLabel("Mode:");
				contentPanel.add(lblMode, "8, 10");
			}
			{
				JLabel lblFont = new JLabel("Font");
				contentPanel.add(lblFont, "10, 10");
			}
			{
				
				color = new JComboBox(colors);
				contentPanel.add(color, "4, 12, left, default");
			}
			{
				mode = new JComboBox(modes);
				contentPanel.add(mode, "8, 12, left, center");
			}
		}
		{
			JButton btnDisplayTime = new JButton("Display Time");
			btnDisplayTime.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					display_time();
				}
			});
			{
				fontBox = new JComboBox(fonts);
				fontBox.setSelectedItem(fonts[2]);
				contentPanel.add(fontBox, "10, 12, fill, default");
				
			}
			{
				timeMode = new JComboBox(timeModes);
				contentPanel.add(timeMode, "4, 16, left, default");
			}
			{
				dateMode = new JComboBox(dateModes);
				contentPanel.add(dateMode, "8, 16, left, default");
			}
			contentPanel.add(btnDisplayTime, "4, 18");
		}
		JButton btnNewButton_1 = new JButton("Set Time");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				set_time( );
			}
		});
		contentPanel.add(btnNewButton_1, "8, 18");
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
		}
	}

	public JTextField getTextMessage() {
		return textField;
	}
	public JList getList() {
		return list;
	}
	
	public String getColorCode() {
		for (int i =0; i < colors.length; i++) {
			if (color.getSelectedItem().equals(colors[i])) return colorCodes[i];
		}
		return colorCodes[0];
	}
	
	public String getModeCode() {
		for (int i =0; i < modes.length; i++) {
			if (mode.getSelectedItem().equals(modes[i])) return modeCodes[i];
		}
		return modeCodes[0];
	}
	public String getDateCode() {
		for (int i =0; i < dateCodes.length; i++) {
			if (dateMode.getSelectedItem().equals(dateModes[i])) return dateCodes[i];
		}
		return dateCodes[0];
	}
	
	public String getTimeCode() {
		System.out.println("Time code: " + timeMode.getSelectedItem());
		for (int i =0; i < timeCodes.length; i++) {
			if (timeMode.getSelectedItem().equals(timeModes[i])) {
				return timeCodes[i];
			}
		}
		return timeCodes[0];
	}
	
	public boolean isAudibleAlert() {
		return chckbxAudibleAlert.isSelected();
	}
	
	public String[] getAddresses() throws IOException
	{
		FileInputStream fis = new FileInputStream("clocks.txt");
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader in = new BufferedReader( isr );
		
		LinkedList<String> addrs = new LinkedList<String>( );
		String addr;
		while ( (addr = in.readLine()) != null) {
			addrs.add(addr.trim());
		}
		in.close();
		isr.close();
		fis.close();
		
		java.util.Collections.sort(addrs);
		
		String aa[] = new String[ addrs.size()];
		int i = 0;
		for (String A : addrs) {
			aa[i++] = A;
		}
		
		return aa;
	}
	
	public int getFontIndex() {
		return fontBox.getSelectedIndex();
	}
	
	public String getFontCode( ) {
		return fontCodes[ getFontIndex( ) ];
	}
}
