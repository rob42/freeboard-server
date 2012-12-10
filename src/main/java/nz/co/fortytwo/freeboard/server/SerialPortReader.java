package nz.co.fortytwo.freeboard.server;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;

/**
 * Wrapper to read serial port via rxtx, then fire messages into the camel route
 * via the seda queue.
 * 
 * @author robert
 *
 */
public class SerialPortReader {

	private ProducerTemplate producer;
	private ConsumerTemplate consumer;

	private boolean running=true;
	private Map<String, String> deviceMap = new HashMap<String, String>();
	
	public SerialPortReader() {
		super();
	}
	
	/**
	 * Opens a connection to the serial port, and starts two threads, one to read, one to write.
	 * 
	 * @param portName
	 * @throws Exception
	 */
	void connect ( String portName ) throws Exception
    {
		
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
            
            if ( commPort instanceof SerialPort )
            {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(38400,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                
                InputStream in = serialPort.getInputStream();
                OutputStream out = serialPort.getOutputStream();
                deviceMap.put(portName,null);
                (new Thread(new SerialReader(in,portName))).start();
                (new Thread(new SerialWriter(out,portName))).start();

            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }     
    }
    
    /** */
    public  class SerialReader implements Runnable 
    {
    	BufferedReader in;
    	private String portName;
    	private boolean mapped=false;
		
        
        public SerialReader ( InputStream in, String portName  )
        {
            this.in = new BufferedReader(new InputStreamReader(in));
            this.portName=portName;
        }
        
        public void run ()
        {
            
            try
            {
                while ( running )
                {
                    //System.out.print(new String(buffer,0,len));
                	String line = in.readLine();
                	if(!mapped && line.indexOf(Constants.UID)>0){
                		//add to map
                		int pos = line.indexOf(Constants.UID+4);
                		String type = line.substring(pos,line.indexOf(",",pos));
                		deviceMap.put(portName, type);
                		mapped=true;
                	}
                    producer.asyncSendBody("seda:input", in.readLine());
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }         
            try {
				producer.stop();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    /** */
    public  class SerialWriter implements Runnable 
    {
        OutputStream out;
		private String portName;
		private boolean mapped=false;
		private String type = null;
        
        public SerialWriter ( OutputStream out, String portName )
        {
            this.out = out;
            this.portName=portName;
            
        }
        
        public void run ()
        {
            try
            {                
                while ( running )
                {
                	Exchange ex = consumer.receive("seda:output?multipleConsumers=true",1000);
                	String message = ex.getIn().getBody(String.class);
                	if(!mapped ){
                		//add to map
                		type = deviceMap.get(portName);
                		if (type!=null) mapped=true;
                	}
                	//check its valid for this device
                	if(type==null || message.contains(Constants.UID+":"+type)){
                		this.out.write((message+"\n").getBytes());
                	}
                }                
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }            
        }
    }

	/**
	 * Set the camel producer, which fire the messages into camel
	 * @param producer
	 */
	public void setProducer(ProducerTemplate producer) {
		this.producer=producer;
		
	}

	/**
	 * True if the serial port read/write threads are running
	 * @return
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Set to false to stop the serial port read/write threads.
	 * You must connect() to restart. 
	 * @param running
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}

	public void setConsumer(ConsumerTemplate consumer) {
		this.consumer = consumer;
	}

}
