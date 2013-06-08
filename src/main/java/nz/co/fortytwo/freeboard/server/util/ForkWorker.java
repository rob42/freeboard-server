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

package nz.co.fortytwo.freeboard.server.util;



import java.io.InputStream;
import java.util.Iterator;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
 
class ForkWorker extends SwingWorker<String,String> {
  
  private JTextArea output; // Where to redirect STDERR & STDOUT to
  private ProcessBuilder builder;
  private Process process;
  
  public ForkWorker(JTextArea output, ProcessBuilder builder) {
    this.output=output;
    this.builder= builder;
  }
  
//  protected void process(java.util.List<String> chunks) {
//    // Done on the event thread
//    Iterator<String> it = chunks.iterator();
//    while (it.hasNext()) {
//      output.append(it.next());
//    }
//  }
  
  public String doInBackground() {
    //Process process;
    try {
      process = builder.start();
      InputStream res = process.getInputStream();
      byte[] buffer = new byte[1];
      int len;
      while ( (len=res.read(buffer,0,buffer.length))!=-1) {
       // publish(new String(buffer,0,len));
        System.out.print(new String(buffer,0,len));
        if (isCancelled()) {
          process.destroy();
          return "";
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return "";  // Don't care
  }
  
  protected void done() {
    // Done on the swing event thread
	  SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	output.append("\nOK\n");  
		    }
		  });
    
  }
}

