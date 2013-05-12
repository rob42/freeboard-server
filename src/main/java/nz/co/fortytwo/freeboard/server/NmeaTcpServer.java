/*
 * Copyright 2012,2013 Robert Huitema robert@42.co.nz
 * 
 * This file is part of FreeBoard. (http://www.42.co.nz/freeboard)
 *
 *  FreeBoard is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  FreeBoard is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with FreeBoard.  If not, see <http://www.gnu.org/licenses/>.
 */

package nz.co.fortytwo.freeboard.server;
	

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;



/**
 * Modified for FreeBoard to act as an NMEA over TCP server by R T Huitema
 * With thanks to Robert Harder for making the original code available
 * 
 * <p>A robust class for establishing a TCP server and manipulating
 * its listening port.
 * The {@link Event}s and property change events make
 * it an appropriate tool in a threaded, GUI application.
 * It is almost identical in design to the UdpServer class
 * which accompanies this one at <a href="http://iHarder.net">iHarder.net</a>.</p>
 * 
 * <p>To start a TCP server, create a new TcpServer and call start():</p>
 * 
 * <pre> TcpServer server = new TcpServer();
 * server.start();</pre>
 * 
 * <p>Of course it won't be much help unless you register as a listener
 * so you'll know when a <tt>java.net.Socket</tt> has come in:</p>
 * 
 * <pre> server.addTcpServerListener( new TcpServer.Listener(){
 *     public void socketReceived( TcpServer.Event evt ){
 *         Socket socket = evt.getSocket();
 *         ...
 *     }   // end socket received
 * });</pre>
 * 
 * <p>The server runs on one thread, and all events may be fired on that thread
 * if desired by setting the executor to null <code>server.setExecutor(null)</code>.
 * By default a cached thread pool is used (<code>Executors.newCachedThreadPool()</code>)
 * so that when you handle a socketReceived event, you are already working
 * in a dedicated thread.</p>
 * 
 * <p>The public methods are all synchronized on <tt>this</tt>, and great
 * care has been taken to avoid deadlocks and race conditions. That being said,
 * there may still be bugs (please contact the author if you find any), and
 * you certainly still have the power to introduce these problems yourself.</p>
 * 
 * <p>It's often handy to have your own class extend this one rather than
 * making an instance field to hold a TcpServer where you'd have to
 * pass along all the setPort(...) methods and so forth.</p>
 * 
 * <p>The supporting {@link Event} and {@link Listener}
 * classes are static inner classes in this file so that you have only one
 * file to copy to your project. You're welcome.</p>
 *
 * <p>Since the TcpServer.java, UdpServer.java, and NioServer.java are
 * so similar, and since lots of copying and pasting was going on among them,
 * you may find some comments that refer to TCP instead of UDP or vice versa.
 * Please feel free to let me know, so I can correct that.</p>
 * 
 * <p>This code is released into the Public Domain.
 * Since this is Public Domain, you don't need to worry about
 * licensing, and you can simply copy this TcpServer.java file
 * to your own package and use it as you like. Enjoy.
 * Please consider leaving the following statement here in this code:</p>
 * 
 * <p><em>This <tt>TcpServer</tt> class was copied to this project from its source as 
 * found at <a href="http://iharder.net" target="_blank">iHarder.net</a>.</em></p>
 *
 * @author Robert Harder
 * @author rharder@users.sourceforge.net
 * @version 0.1
 * @see NmeaTcpServer
 * @see Event
 * @see Listener
 */
public class NmeaTcpServer extends FreeboardProcessor implements Processor{
    
	private static Logger LOGGER = Logger.getLogger(InputFilterProcessor.class);
    
    /**
     * The port property <tt>port</tt> used with
     * the property change listeners and the preferences,
     * if a preferences object is given.
     */
    public final static String PORT_PROP = "port";
    private final static int PORT_DEFAULT = 5555;
    private int port = PORT_DEFAULT;
    
    /**
     * The Executor property <tt>executor</tt> used with
     * the property change listeners and the preferences,
     * if a preferences object is given.
     */
    public final static String EXECUTOR_PROP = "executor";
    private final static Executor EXECUTOR_DEFAULT = Executors.newCachedThreadPool();
    private Executor executor = EXECUTOR_DEFAULT;
    
    
    /**
     * <p>One of four possible states for the server to be in:</p>
     * 
     * <ul>
     *  <li>STARTING</li>
     *  <li>STARTED</li>
     *  <li>STOPPING</li>
     *  <li>STOPPED</li>
     * </ul>
     */
    public static enum State { STARTING, STARTED, STOPPING, STOPPED };
    private State currentState = State.STOPPED;
    public final static String STATE_PROP = "state";
    
    
    private Collection<Listener> listeners = new LinkedList<Listener>();                // Event listeners
    private Event event = new Event(this);                                              // Shared event
    private PropertyChangeSupport propSupport = new PropertyChangeSupport(this);        // Properties
    
    private NmeaTcpServer This = this;                                                      // To aid in synchronizing
    private ThreadFactory threadFactory;                                                // Optional thread factory
    private Thread ioThread;                                                            // Performs IO
    private ServerSocket tcpServer;                                                     // The server
    private Socket socket;
    private Map<Socket,BufferedOutputStream> sockets=new HashMap<Socket,BufferedOutputStream>();
    

    public final static String LAST_EXCEPTION_PROP = "lastException";
    private Throwable lastException;
    
    
/* ********  C O N S T R U C T O R S  ******** */
    
    
    /**
     * Constructs a new TcpServer that will listen on the default port 5555
     * (but not until {@link #start} is called).
     * The I/O thread will not be in daemon mode.
     */
    public NmeaTcpServer(){
    	init();
    }
    
    

	/**
     * Constructs a new TcpServer that will listen on the given port 
     * (but not until {@link #start} is called).
     * The I/O thread will not be in daemon mode.
     * @param port the port on which to listen
     */
    public NmeaTcpServer( int port ){
        this.port = port;
    }
    
    /**
     * Constructs a new TcpServer that will listen on the given port 
     * (but not until {@link #start} is called). The provided
     * ThreadFactory will be used when starting and running the server.
     * @param port the port to listen to
     * @param factory for creating the io thread
     */
    public NmeaTcpServer( int port, ThreadFactory factory ){
        this.port = port;
        this.threadFactory = factory;
    }
    
    /**
     * Add an internal listener, so we can echo to all attached clients
     */
    private void init() {
		addTcpServerListener(new NmeaTcpServer.Listener(){

			@Override
			public void socketReceived(Event evt) {
				//we get rid of any that have closed.
				//we do this here so we dont have to waste time checking on every message in process() method
				//List<Socket> closedSockets=new ArrayList<Socket>();
				for(Socket socket:sockets.keySet()){
					if(socket==null ||socket.isClosed()){
						sockets.remove(socket);
					}
				}
				//sockets.removeAll(closedSockets);
				//add the new one
				try {
					sockets.put(evt.getSocket(),new BufferedOutputStream(evt.getSocket().getOutputStream()));
				
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
		
	}
    
    
/* ********  R U N N I N G  ******** */
    
    
    /**
     * Attempts to start the server listening and returns immediately.
     * Listen for start events to know if the server was
     * successfully started.
     * 
     * @see Listener
     */
    public synchronized void start(){
        if( this.currentState == State.STOPPED ){           // Only if we're stopped now
            assert ioThread == null : ioThread;             // Shouldn't have a thread

            Runnable run = new Runnable() {
                @Override
                public void run() {
                    runServer();                            // This runs for a long time
                    ioThread = null;          
                    setState( State.STOPPED );              // Clear thread
                }   // end run
            };  // end runnable
            
            if( this.threadFactory != null ){               // User-specified threads
                this.ioThread = this.threadFactory.newThread(run);
                
            } else {                                        // Our own threads
                this.ioThread = new Thread( run, this.getClass().getName() );   // Named
            }

            setState( State.STARTING );                     // Update state
            this.ioThread.start();                          // Start thread
        }   // end if: currently stopped
    }   // end start
    
    
    /**
     * Attempts to stop the server, if the server is in
     * the STARTED state, and returns immediately.
     * Be sure to listen for stop events to know if the server was
     * successfully stopped.
     * 
     * @see Listener
     */
    public synchronized void stop(){
        if( this.currentState == State.STARTED ){   // Only if already STARTED
            setState( State.STOPPING );             // Mark as STOPPING
            //close all sockets
            for(Socket socket:sockets.keySet()){
            	try {
					socket.close();
				} catch (IOException e) {
					LOGGER.error(e.getMessage(),e);
				}
            }
            if( this.tcpServer != null ){           // 
                try{
                    this.tcpServer.close();
                } catch( IOException exc ){
                    LOGGER.error( 
                      "An error occurred while closing the TCP server. " +
                      "This may have left the server in an undefined state.",
                      exc );
                    fireExceptionNotification(exc);
                }
            }   // end if: not null
        }   // end if: already STARTED
    }   // end stop
    
    
    
    
    /**
     * Returns the current state of the server, one of
     * STOPPED, STARTING, or STARTED.
     * @return state of the server
     */
    public synchronized State getState(){
        return this.currentState;
    }
    
    
    /**
     * Sets the state and fires an event. This method
     * does not change what the server is doing, only
     * what is reflected by the currentState variable.
     * @param state the new server state
     */
    protected synchronized void setState( State state ){
        State oldVal = this.currentState;
        this.currentState = state;
        firePropertyChange(STATE_PROP,oldVal,state);
    }
    
    
    /**
     * Fires an event declaring the current state of the server.
     * This may encourage lazy programming on your part, but it's
     * handy to set yourself up as a listener and then fire an
     * event in order to initialize this or that.
     */
    //public synchronized void fireState(){
    //    fireTcpServerStateChanged();
    //}
    
    
    /**
     * Resets the server, if it is running, otherwise does nothing.
     * This is accomplished by registering as a listener, stopping
     * the server, detecting the stop, unregistering, and starting
     * the server again. It's a useful design pattern, and you may
     * want to look at the source code for this method to check it out.
     */
    public synchronized void reset(){
        switch( this.currentState ){
            case STARTED:
                this.addPropertyChangeListener(STATE_PROP, new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        State newState = (State)evt.getNewValue();
                        if( newState == State.STOPPED ){
                            NmeaTcpServer server = (NmeaTcpServer)evt.getSource();
                            server.removePropertyChangeListener(STATE_PROP,this);
                            server.start();
                        }   // end if: stopped
                    }   // end prop change
                });
                stop();
                break;
        }   // end switch
    }
    
    
    /**
     * This method starts up and listens indefinitely
     * for TCP packets. On entering this method,
     * the state is assumed to be STARTING. Upon exiting
     * this method, the state will be STOPPING.
     */
    protected void runServer(){
        try{
            this.tcpServer = new ServerSocket( getPort() );                 // Create server
            setState( State.STARTED );                                      // Mark as started
            LOGGER.info("TCP Server established on port " + getPort() );
            
            while( !this.tcpServer.isClosed() ){
                synchronized( this ){
                    if( this.currentState == State.STOPPING ){
                        LOGGER.info( "Stopping TCP Server by request." );
                        this.tcpServer.close();
                    }   // end if: stopping
                }   // end sync
                
                if( !this.tcpServer.isClosed() ){
                    
                    ////////  B L O C K I N G
                    this.socket = this.tcpServer.accept();
                    ////////  B L O C K I N G
                    
                    LOGGER.info( "TCP Server incoming socket: " + socket );
                    
                    fireTcpServerSocketReceived();
                    
                }   //end if: not closed
            }   // end while: keepGoing
            
        } catch( Exception exc ){
            synchronized( this ){
                if( this.currentState == State.STOPPING ){  // User asked to stop
                    try{
                        this.tcpServer.close();
                        LOGGER.info( "TCP Server closed normally." );
                    } catch( IOException exc2 ){
                        LOGGER.error(
                          "An error occurred while closing the TCP server. " +
                          "This may have left the server in an undefined state.",
                          exc2 );
                        fireExceptionNotification(exc2);
                    }   // end catch IOException
                } else {
                    LOGGER.warn("Server closed unexpectedly: " + exc.getMessage(), exc );
                }   // end else
            }   // end sync
            fireExceptionNotification(exc);
        } finally {
            setState( State.STOPPING );
            if( this.tcpServer != null ){
                try{
                    this.tcpServer.close();
                    LOGGER.info( "TCP Server closed normally." );
                } catch( IOException exc2 ){
                    LOGGER.error(
                      "An error occurred while closing the TCP server. " +
                      "This may have left the server in an undefined state.",
                      exc2 );
                    fireExceptionNotification(exc2);
                }   // end catch IOException
            }   // end if: not null
            this.tcpServer = null;
        }
    }
    
/* ********  S O C K E T  ******** */    
    
    /**
     * Returns the last Socket received.
     * @return the socket just received
     */
    public synchronized Socket getSocket(){
        return this.socket;
    }
    
    
    
/* ********  P O R T  ******** */
    
    /**
     * Returns the port on which the server is or will be listening.
     * @return The port for listening.
     */
    public synchronized int getPort(){
        return this.port;
    }
    
    /**
     * Sets the new port on which the server will attempt to listen.
     * If the server is already listening, then it will attempt to
     * restart on the new port, generating start and stop events.
     * If the old port and new port are the same, events will be
     * fired, but the server will not actually reset.
     * @param port the new port for listening
     * @throws IllegalArgumentException if port is outside 0..65535
     */
    public synchronized void setPort( int port ){
        if( port < 0 || port > 65535 ){
            throw new IllegalArgumentException( "Cannot set port outside range 0..65535: " + port );
        }   // end if: port outside range
        
            
        int oldVal = this.port;
        this.port = port;
        if( getState() == State.STARTED && oldVal != port ){
            reset();
        }   // end if: is running

        firePropertyChange( PORT_PROP, oldVal, port  );
    }   
    
    
/* ********  E X E C U T O R  ******** */
    
    
    /**
     * Returns the Executor (or null if none is set)
     * that is used to execute the event firing.
     * @return Executor used for event firing or null
     */
    public synchronized Executor getExecutor(){
        return this.executor;
    }
    
    /**
     * <p>Sets (or clears, if null) the Executor used to 
     * fire events. If an Executor is set, then for each
     * event, all listeners of that event are called in
     * seqentially on a thread generated by the Executor.</p>
     * 
     * <p>Take the following example:</p>
     * 
     * <code>import java.util.concurrent.*;
     * ...
     * server.setExecutor( Executors.newCachedThreadPool() );</code>
     * 
     * <p>Let's say three objects are registered to listen for
     * events from the TcpServer. When the server state changes,
     * the three objects will be called sequentially on the same
     * thread, generated by the Cached Thread Pool. Say one of those
     * objects takes a long time to respond, and a new incoming
     * connection is established while waiting. Those three objects
     * will sequentially be notified of the new connection on a
     * different thread, generated by the Cached Thread Pool.</p>
     * 
     * @param exec the new Executor or null if no executor is to be used
     */
    public synchronized void setExecutor( Executor exec ){
        Executor oldVal = this.executor;
        this.executor = exec;

        firePropertyChange( EXECUTOR_PROP, oldVal, exec  );
    }   
    
    
    
    
/* ********  E V E N T S  ******** */
    
    

    /** 
     * Adds a {@link Listener}.
     * @param l the listener
     */
    public synchronized void addTcpServerListener(NmeaTcpServer.Listener l) {
        listeners.add(l);
    }

    
    /** 
     * Removes a {@link Listener}.
     * @param l the listener
     */
    public synchronized void removeTcpServerListener(NmeaTcpServer.Listener l) {
        listeners.remove(l);
    }
    
    
    /** Fires event when a socket is received */
    protected synchronized void fireTcpServerSocketReceived() {
        
        final NmeaTcpServer.Listener[] ll = listeners.toArray(new NmeaTcpServer.Listener[ listeners.size() ] );
        
        // Make a Runnable object to execute the calls to listeners.
        // In the event we don't have an Executor, this results in
        // an unnecessary object instantiation, but it also makes
        // the code more maintainable.
        Runnable r = new Runnable(){
            public void run(){
            for( Listener l : ll ){
                try{
                    l.socketReceived(event);
                } catch( Exception exc ){
                    LOGGER.warn("TcpServer.Listener " + l + " threw an exception: " + exc.getMessage() );
                    fireExceptionNotification(exc);
                }   // end catch
            }   // end for: each listener
            }   // end run
        };
        
        if( this.executor == null ){
            r.run();
        } else {
            try{
                this.executor.execute( r ); 
            } catch( Exception exc ){
                LOGGER.warn("Supplied Executor " + this.executor + " threw an exception: " + exc.getMessage() );
                fireExceptionNotification(exc);
            }   // end catch
        }   // end else: other thread
     }  // end fireTcpServerPacketReceived
    
    
    
    
/* ********  P R O P E R T Y   C H A N G E  ******** */
    
    
    /**
     * Fires property chagne events for all current values
     * setting the old value to null and new value to the current.
     */
    public synchronized void fireProperties(){
        firePropertyChange( PORT_PROP, null, getPort()  );      // Port
        firePropertyChange( STATE_PROP, null, getState()  );      // State
    }
    

    /**
     * Fire a property change event on the current thread.
     *
     * @param prop      name of property
     * @param oldVal    old value
     * @param newVal    new value
     */
    protected synchronized void firePropertyChange( final String prop, final Object oldVal, final Object newVal ){
        try{
            propSupport.firePropertyChange(prop,oldVal,newVal);
        } catch( Exception exc ){
            LOGGER.warn(
                    "A property change listener threw an exception: " + exc.getMessage()
                    ,exc);
            fireExceptionNotification(exc);
        }   // end catch
    }   // end fire



    /**
     * Add a property listener.
     * @param listener the property change listener
     */
    public synchronized void addPropertyChangeListener( PropertyChangeListener listener ){
        propSupport.addPropertyChangeListener(listener);
    }


    /**
     * Add a property listener for the named property.
     * @param property the sole property name for which to register
     * @param listener the property change listener
     */
    public synchronized void addPropertyChangeListener( String property, PropertyChangeListener listener ){
        propSupport.addPropertyChangeListener(property,listener);
    }


    /**
     * Remove a property listener.
     * @param listener the property change listener
     */
    public synchronized void removePropertyChangeListener( PropertyChangeListener listener ){
        propSupport.removePropertyChangeListener(listener);
    }


    /**
     * Remove a property listener for the named property.
     * @param property the sole property name for which to stop receiving events
     * @param listener the property change listener
     */
    public synchronized void removePropertyChangeListener( String property, PropertyChangeListener listener ){
        propSupport.removePropertyChangeListener(property,listener);
    }

    


/* ********  E X C E P T I O N S  ******** */


    /**
     * Returns the last exception (Throwable, actually)
     * that the server encountered.
     * @return last exception
     */
    public synchronized Throwable getLastException(){
        return this.lastException;
    }

    /**
     * Fires a property change event with the new exception.
     * @param t
     */
    protected void fireExceptionNotification( Throwable t ){
        Throwable oldVal = this.lastException;
        this.lastException = t;
        firePropertyChange( LAST_EXCEPTION_PROP, oldVal, t );
    }

    
    
    
    
/* ********                                                          ******** */
/* ********                                                          ******** */    
/* ********   S T A T I C   I N N E R   C L A S S   L I S T E N E R  ******** */
/* ********                                                          ******** */
/* ********                                                          ******** */    
    
    
    
    
    
    /**
     * An interface for listening to events from a {@link NmeaTcpServer}.
     * A single {@link Event} is shared for all invocations
     * of these methods.
     * 
     * <p>This code is released into the Public Domain.
     * Since this is Public Domain, you don't need to worry about
     * licensing, and you can simply copy this TcpServer.java file
     * to your own package and use it as you like. Enjoy.
     * Please consider leaving the following statement here in this code:</p>
     * 
     * <p><em>This <tt>TcpServer</tt> class was copied to this project from its source as 
     * found at <a href="http://iharder.net" target="_blank">iHarder.net</a>.</em></p>
     *
     * @author Robert Harder
     * @author rharder@users.sourceforge.net
     * @version 0.1
     * @see NmeaTcpServer
     * @see Event
     */
    public static interface Listener extends java.util.EventListener {

        /**
         * Called when a packet is received. This is called on the IO thread,
         * so don't take too long, and if you want to offload the processing
         * to another thread, be sure to copy the data out of the datagram
         * since it will be clobbered the next time around.
         * 
         * @param evt the event
         */
        public abstract void socketReceived( Event evt );


    }   // end inner static class Listener

    
    

/* ********                                                        ******** */
/* ********                                                        ******** */    
/* ********   S T A T I C   I N N E R   C L A S S   A D A P T E R  ******** */
/* ********                                                        ******** */
/* ********                                                        ******** */    
    



    /**
     * A helper class that implements all methods of the
     * {@link TcpServer.Listener} interface with empty methods.
     * 
     * <p>This code is released into the Public Domain.
     * Since this is Public Domain, you don't need to worry about
     * licensing, and you can simply copy this TcpServer.java file
     * to your own package and use it as you like. Enjoy.
     * Please consider leaving the following statement here in this code:</p>
     * 
     * <p><em>This <tt>TcpServer</tt> class was copied to this project from its source as 
     * found at <a href="http://iharder.net" target="_blank">iHarder.net</a>.</em></p>
     *
     * @author Robert Harder
     * @author rharder@users.sourceforge.net
     * @version 0.1
     * @see TcpServer
     * @see Listener
     * @see Event
     */
//    public class Adapter implements Listener {

        /**
         * Empty call for {@link TcpServer.Listener#tcpServerStateChanged}.
         * @param evt the event
         */
      //  public void tcpServerStateChanged(Event evt) {}


        /**
         * Empty call for {@link TcpServer.Listener#socketReceived}.
         * @param evt the event
         */
//        public void socketReceived(Event evt) {}

//    }   // end static inner class Adapter
    
    
/* ********                                                    ******** */
/* ********                                                    ******** */    
/* ********   S T A T I C   I N N E R   C L A S S   E V E N T  ******** */
/* ********                                                    ******** */
/* ********                                                    ******** */    
    
    

    /**
     * An event representing activity by a {@link NmeaTcpServer}.
     * 
     * <p>This code is released into the Public Domain.
     * Since this is Public Domain, you don't need to worry about
     * licensing, and you can simply copy this TcpServer.java file
     * to your own package and use it as you like. Enjoy.
     * Please consider leaving the following statement here in this code:</p>
     * 
     * <p><em>This <tt>TcpServer</tt> class was copied to this project from its source as 
     * found at <a href="http://iharder.net" target="_blank">iHarder.net</a>.</em></p>
     *
     * @author Robert Harder
     * @author rharder@users.sourceforge.net
     * @version 0.1
     * @see NmeaTcpServer
     * @see Listener
     */
    public static class Event extends java.util.EventObject {

        private final static long serialVersionUID = 1;

        /**
         * Creates a Event based on the given {@link NmeaTcpServer}.
         * @param src the source of the event
         */
        public Event( NmeaTcpServer src ){
            super(src);
        }

        /**
         * Returns the source of the event, a {@link NmeaTcpServer}.
         * Shorthand for <tt>(TcpServer)getSource()</tt>.
         * @return the server
         */
        public NmeaTcpServer getTcpServer(){
            return (NmeaTcpServer)getSource();
        }

        /**
         * Shorthand for <tt>getTcpServer().getState()</tt>.
         * @return the state of the server
         * @see NmeaTcpServer.State
         */
        public NmeaTcpServer.State getState(){
            return getTcpServer().getState();
        }


        /**
         * Returns the most recent datagram packet received
         * by the {@link NmeaTcpServer}. Shorthand for
         * <tt>getTcpServer().getPacket()</tt>.
         * @return the most recent datagram
         */
        public Socket getSocket(){
            return getTcpServer().getSocket();
        }


    }   // end static inner class Event




	/* (non-Javadoc)
	 * Implement the camel processor interface so we can just fire messages directly
	 * 
	 * @see org.apache.camel.Processor#process(org.apache.camel.Exchange)
	 */
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			String nmea = exchange.getIn().getBody(String.class)+"\n";
			for(Socket socket:sockets.keySet()){
				try{
					if(socket.isClosed()){
						//for efficiency they get removed later when the next socket attaches.
						continue;
					}
					OutputStream out = sockets.get(socket);
					out.write(nmea.getBytes());
					out.flush();
				}catch(Exception e){
					LOGGER.info(e.getMessage());
					if(socket!=null){
						socket.close();
					}
				}
			}
		}catch(Exception e){
			LOGGER.error(e.getMessage(),e);
		}
		
	}

    

}   // end class TcpServer


