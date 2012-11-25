package nz.co.fortytwo.freeboard.server;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

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

	private boolean running=true;
	
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
                
                (new Thread(new SerialReader(in))).start();
                (new Thread(new SerialWriter(out))).start();

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
		
        
        public SerialReader ( InputStream in )
        {
            this.in = new BufferedReader(new InputStreamReader(in));
        }
        
        public void run ()
        {
            
            try
            {
                while ( running )
                {
                    //System.out.print(new String(buffer,0,len));
                    producer.asyncSendBody("seda:input?multipleConsumers=true", in.readLine());
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
        
        public SerialWriter ( OutputStream out )
        {
            this.out = out;
        }
        
        public void run ()
        {
            try
            {                
                int c = 0;
                while ( running && ( c = System.in.read()) > -1 )
                {
                    this.out.write(c);
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

}
