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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Performance tweak, we send the hash directly to the handlers, rather than via the Exchange which probably serializes it
 * 
 * @author robert
 *
 */
public class CombinedProcessor extends FreeboardProcessor implements Processor{

	private List<FreeboardHandler> handlers =new ArrayList<FreeboardHandler>();
	@Override
	public void process(Exchange exchange) throws Exception {
		if (exchange.getIn().getBody() == null) {
			return;
		}
		@SuppressWarnings("unchecked")
		HashMap<String, Object> map = exchange.getIn().getBody(HashMap.class);
		for(FreeboardHandler handler:handlers){
			map=handler.handle(map);
		}
		exchange.getIn().setBody(map);
	}

	public void addHandler(FreeboardHandler handler) {
		handlers.add(handler);
		
	}

	

}
