package com.apkscanner.gui.install;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;

public class DeviceTablePanel extends JPanel implements ComponentListener{
	JTable table;
	JTextArea Logtext;	
	ActionListener Mainlistener;
	JLabel Hinttext;
	DeviceListModel tableModel;
	static final String TEXT_SELECT_DEVICE = "Select Devices!! ";
	static final String TEXT_CHECK_DEVICE_STATUS = "Check deivce status( OFFLINE )";
	static final String TEXT_CONNECT_DEVICE = "Connect Device!!";
	Boolean selectAll = false;
	
	
	public DeviceTablePanel(ActionListener listener) {
		setLayout(new BorderLayout());
		Mainlistener = listener;
		
		
		
        tableModel = new DeviceListModel();
        table = new JTable();
        table.setModel(tableModel);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.removeColumn(table.getColumnModel().getColumn(1));               
        
        final ListSelectionModel selectionModel = table.getSelectionModel();
        
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {            	
                if (e.getValueIsAdjusting())
                    return;
                
                int index = table.getSelectedRow();
                //Log.d("selected : "+index);
                if(index <0) {
                	repaint();
                	return;
                }
                if(tableModel.getValueAt(index,4).equals("ONLINE")) {                	
                	//Log.d("selected : ONLINE");                	
                	Hinttext.setText(TEXT_SELECT_DEVICE);
                	repaint();
                } else {
                	//Log.d("selected : OFFLINE");
                	Hinttext.setText(TEXT_CHECK_DEVICE_STATUS);
                	repaint();
                }
            }
        });
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        for( int i=1;i<table.getColumnCount(); i++) {
        	table.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
        }
        
        
        this.addComponentListener(this);
        JScrollPane targetDevicesPane = new JScrollPane(table);
        Logtext = new JTextArea(10,0);
		JScrollPane textViewscrollPane = new JScrollPane(Logtext);
		
		JPanel dummypanel = new JPanel(new BorderLayout());
		JPanel dummydevicePanepanel = new JPanel(new BorderLayout());
		
		
		Hinttext = new JLabel(TEXT_CONNECT_DEVICE);
        Font font = new Font("Courier", Font.BOLD,20);
        Hinttext.setFont(font);
        Hinttext.setHorizontalAlignment(SwingConstants.RIGHT);
		
        JButton selectall = new JButton("Select all");
        JPanel buttonpanel = new JPanel(new BorderLayout());
        
        selectall.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				selectAll = selectAll ? false : true;
				
				for(int i=0;i< tableModel.getTableDataList().size(); i++) {
					DeviceDO device = tableModel.getTableDataList().get(i);
					device.setchecked(selectAll);					
				}
				
				tableModel.fireTableDataChanged();
			}
		});
        
        buttonpanel.add(selectall, BorderLayout.EAST);        
        
        dummydevicePanepanel.setBorder(BorderFactory.createTitledBorder("Device List"));
        
        dummydevicePanepanel.add(targetDevicesPane,BorderLayout.CENTER);
        dummydevicePanepanel.add(buttonpanel,BorderLayout.SOUTH);
        
		dummypanel.add(textViewscrollPane,BorderLayout.CENTER);
		dummypanel.add(Hinttext,BorderLayout.SOUTH);
		
		//add(table.getTableHeader(), BorderLayout.NORTH);
        add(dummydevicePanepanel, BorderLayout.CENTER);
        add(dummypanel, BorderLayout.SOUTH);
        initADBInit();
        AndroidDebugBridge.addDeviceChangeListener(new IDeviceChangeListener() {			
			@Override
			public void deviceDisconnected(IDevice device) {
				// TODO Auto-generated method stub
				Log.d("deviceDisconnected device state : " + device.getSerialNumber() + " : " + device.getState());
				writeLog("Disconnected : " + device.getSerialNumber());
				
				//textLog = textLog + "deviceDisconnected device state : " + device.getSerialNumber() + " : " + device.getState() + "\n";				
				//Logtext.setText(textLog);
				tableModel.removeSerialValue(device.getSerialNumber());
				if(tableModel.getRowCount() > 0) { 
					table.addRowSelectionInterval(0, 0);
				} else {
                	Hinttext.setText(TEXT_CONNECT_DEVICE);
                	repaint();
				}
			}
			
			@Override
			public void deviceConnected(IDevice device) {
				//System.out.println(String.format("%s connected", device.getSerialNumber()));
				
				Log.d("" + device.getState());
				

				
				Log.d("deviceConnected device state : " + device.getSerialNumber() + " : " + device.getState());
				writeLog("Connected : " + device.getSerialNumber() + " : " + device.getState());
				
				DeviceDO devicestate = new DeviceDO();

				
				//devicestate.setName(getDeviceName(device));
				getDeviceName(device, devicestate);
				
				devicestate.setSerial(device.getSerialNumber());
				devicestate.setStatus(device.getState().toString());
				devicestate.setDevice(device);
				tableModel.addRow(devicestate);
				
				//if(tableModel.getTableDataList().size() > 0) {				
					table.addRowSelectionInterval(0, 0);
					selectionModel.setValueIsAdjusting(true);	
				//}
			}
			
			@Override
			public void deviceChanged(IDevice device, int arg1) {
				// TODO Auto-generated method stub
				Log.d("change device state : " + device.getSerialNumber() + " : " + device.getState());
				writeLog("Changed : " + device.getSerialNumber() + " : " + device.getState());				

				//tableModel.setStatusName(device.getSerialNumber(), ));
				
				getDeviceName(device, tableModel.getDeviceDC(device.getSerialNumber()));
				
				tableModel.setStatusValue(device.getSerialNumber(), device.getState().toString());
				if(tableModel.getRowCount() > 0) { 
					table.addRowSelectionInterval(0, 0);
				}
			}
		});;        
	}
	
	public void getDeviceName(IDevice device, final DeviceDO DO) {
		try 
		{
		final String DeviceName = null;
		if("ONLINE".equals(device.getState().toString())) {
				device.executeShellCommand("getprop ro.product.model", new MultiLineReceiver() {
					String temp;
					    @Override
					    public void processNewLines(String[] lines) {					        
					        	if(lines[0].length() >0) {
					        		temp = lines[0];
					        		//DeviceName = temp;
					        		DO.setName(temp);
					        		Log.d(temp);
					        		return;
					        	}
					        
					    }
					    @Override
					    public boolean isCancelled() {
					        return false;
					    }
					});
				
			} else {
				DO.setName(device.getName());
			}
		
			return;
			
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
				| IOException e) {
			e.printStackTrace();
		}
		return;
	}
	public void writeLog(String str) {
		Logtext.append(str + "\r\n");
		Logtext.setCaretPosition(Logtext.getDocument().getLength());
		this.repaint();
		this.revalidate();		
	}
	
	public class DeviceDO {

		private Boolean checked;
	    private int number;
	    private String name;
	    private String Serial;
	    private String Status;
	    private IDevice device;
	    
	    public Boolean getchecked() {
	    	return checked;
	    }
	    
	    public IDevice getDevice() {
			// TODO Auto-generated method stub
			return device;
		}
		public int getNumber() {
			// TODO Auto-generated method stub
			return number;
		}
		public Object getName() {
			// TODO Auto-generated method stub
			return name;
		}
		public Object getSerial() {
			// TODO Auto-generated method stub
			return Serial;
		}
		public Object getStatus() {
			// TODO Auto-generated method stub
			return Status;
		}
		
	    public void setchecked(Boolean check) {
	    	this.checked = check;
	    }
		
		public void setNumber(Integer integer) {
			// TODO Auto-generated method stub
			this.number = integer;
		}
		public void setName(String object) {
			// TODO Auto-generated method stub
			this.name = object;
		}
		public void setSerial(String object) {
			// TODO Auto-generated method stub
			this.Serial = object;
		}
		public void setStatus(String object) {
			// TODO Auto-generated method stub
			this.Status = object;
		}
		public void setDevice(IDevice device) {
			// TODO Auto-generated method stub
			this.device = device;
		}
	    
	        // Add getter's and setter's
	}
	
	private void initADBInit() {
		AndroidDebugBridge.init(false);
        AndroidDebugBridge debugBridge = AndroidDebugBridge.createBridge(Resource.BIN_ADB.getPath(), true);
        if (debugBridge == null) {
            System.err.println("Invalid ADB  location.");
            System.exit(1);
        }
	}
	
	private void setJTableColumnsWidth(JTable table, int tablePreferredWidth,
			double... percentages) {
			double total = 0;
			for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			total += percentages[i];
			}
			
			for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			TableColumn column = table.getColumnModel().getColumn(i);		        
			column.setPreferredWidth((int)(tablePreferredWidth * (percentages[i] / total)));
			}
		}
	
	public class DeviceListModel extends AbstractTableModel {

	    private static final long serialVersionUID = 1L;

	    private ArrayList<DeviceDO> data;
	    private ArrayList<String> columnNames;

	    public DeviceListModel() {
	        data = initdataList();
	        columnNames = getColumnNamesList();
	    }
	    
	    private ArrayList<DeviceDO> initdataList() {
	    	ArrayList<DeviceDO> temp = new ArrayList<DeviceDO>();
	    	return temp;
	    }

	    @Override
	    public Class<?> getColumnClass(int columnIndex) {
	        switch (columnIndex) {
	        case 0:
	            return Boolean.class;
	        case 1:
	            return Integer.class;
	        case 2: 
	            return String.class;
	        case 3: 
	            return String.class;
	        case 4: 
	            return String.class;
	        default:
	            return String.class;
	        }
	    }

	    @Override
	    public boolean isCellEditable(int rowIndex, int columnIndex) {
	        return columnIndex == 0 ? true : false;
	    }

	    @Override
	    public String getColumnName(int column) {
	        return columnNames.get(column);
	    }

	    @Override
	    public int getRowCount() {
	        return data.size();
	    }

	    @Override
	    public int getColumnCount() {
	        return columnNames.size();
	    }

	    @Override
	    public Object getValueAt(int rowIndex, int columnIndex) {
	        switch (columnIndex) {
	        case 0:
	            return data.get(rowIndex).getchecked();
	        case 1:
	            return data.get(rowIndex).getNumber();
	        case 2:
	            return data.get(rowIndex).getName();
	        case 3:
	            return data.get(rowIndex).getSerial();
	        case 4:
	            return data.get(rowIndex).getStatus();
	        case 5:
	            return data.get(rowIndex).getDevice();
	        default:
	            return null;
	        }
	    }

	    @Override
	    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	    	
	    	switch (columnIndex) {
	    	case 0:
	    		data.get(rowIndex).setchecked((Boolean)aValue);
	    		break;
	        case 1:
	            //data.get(rowIndex).setSelect((Boolean) aValue);
	            data.get(rowIndex).setNumber(aValue != null ? Integer.parseInt(aValue.toString()) : null);
	            break;
	        case 2:
	            data.get(rowIndex).setName(aValue != null ? aValue.toString() : null);
	            break;
	        case 3:
	            data.get(rowIndex).setSerial(aValue != null ? aValue.toString() : null);
	            break;
	        case 4:
	            data.get(rowIndex).setStatus(aValue != null ? aValue.toString() : null);
	            break;
	        default:
	            break;
	        }
	    }
		public DeviceDO getDeviceDC(String serial) {
	    	for(int rowIndex = data.size() - 1; rowIndex >= 0; rowIndex--) {
	            if(data.get(rowIndex).getSerial() == serial) {
	                return data.get(rowIndex);
	            }
	        }
	    	return null;
		}
	    
	    public void setStatusValue(String serial, String status) {
	    	for(int rowIndex = data.size() - 1; rowIndex >= 0; rowIndex--) {
	            if(data.get(rowIndex).getSerial() == serial) {
	                data.get(rowIndex).setStatus(status);
	            }
	        }
	    	fireTableDataChanged();	    	
	    }
	    
	    public void setStatusName(String serial, String name) {
	    	for(int rowIndex = data.size() - 1; rowIndex >= 0; rowIndex--) {
	            if(data.get(rowIndex).getSerial() == serial) {
	                data.get(rowIndex).setName(name);
	            }
	        }
	    	fireTableDataChanged();	    	
	    }
	    
	    public void removeSerialValue(String serial) {
	    	for(int rowIndex = data.size() - 1; rowIndex >= 0; rowIndex--) {
	            if(data.get(rowIndex).getSerial() == serial) {
	                data.remove(rowIndex);
	            }
	        }
	    	for(int rowIndex = 0 ; rowIndex < data.size(); rowIndex++) {
	    		data.get(rowIndex).setNumber(rowIndex);
	    	}
	    	
	    	fireTableDataChanged();
	    }	    
	    
	    public void addRow(DeviceDO do1) {
	    	boolean isexist = false;
	    	
	    	do1.setNumber(data.size());
	    	do1.setchecked(new Boolean(false));	
	        for(int rowIndex = data.size() - 1; rowIndex >= 0; rowIndex--) {
	            if(data.get(rowIndex).getSerial().equals(do1.getSerial())) {
	            	data.set(rowIndex, do1);
	            	//fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
	            	fireTableDataChanged();
	            	return ;
	            }
	        }
	        
	        data.add(do1);
	        
	        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
	    }

	    public void deleteRow(int number) {
	        for(int rowIndex = data.size() - 1; rowIndex >= 0; rowIndex--) {
	            if(data.get(rowIndex).getNumber() == number) {
	                data.remove(rowIndex);
	            }
	        }

	        fireTableDataChanged();
	    }

	    private ArrayList<DeviceDO> getTableDataList() {
	        return data;
	    }

	    private ArrayList<String> getColumnNamesList() {
	    	ArrayList<String> names = new ArrayList<String>();
	        
	        names.add("Select");
	        names.add("No");
	        names.add("Name");
	        names.add("Serial");
	        names.add("Status");
	        return names;
	    }
	}

	@Override
	public void componentResized(ComponentEvent e) {
		setJTableColumnsWidth(table,e.getComponent().getWidth(),60,200,350,150);		
	}
	@Override
	public void componentMoved(ComponentEvent e) {	}

	@Override
	public void componentShown(ComponentEvent e) {}

	@Override
	public void componentHidden(ComponentEvent e) {}
	
	public List<DeviceDO> getDeviceList() {
		return tableModel.getTableDataList();
	}
	
}
