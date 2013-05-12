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

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Wraps the message in  a FreeboardMessage which implements a json convertor
 * @author robert
 *
 */
public class AddSourceProcessor implements Processor {
    String  sourceName;
    public AddSourceProcessor(String name){
      sourceName = name;
    }
    public void process(Exchange exchange) throws Exception {
        exchange.getOut().setHeader("srcName",sourceName);
        FreeboardMessage msg = new FreeboardMessage(sourceName, (String)exchange.getIn().getBody());
        exchange.getOut().setBody(msg);
    }
}
