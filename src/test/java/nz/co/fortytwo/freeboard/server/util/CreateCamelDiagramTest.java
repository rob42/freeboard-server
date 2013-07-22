/*
 * Copyright 2012,2013 Robert Huitema robert@42.co.nz
 * 
 * This file is part of FreeBoard. (http://www.42.co.nz/freeboard)
 * 
 * FreeBoard is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FreeBoard is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with FreeBoard. If not, see <http://www.gnu.org/licenses/>.
 */

package nz.co.fortytwo.freeboard.server.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.co.fortytwo.freeboard.server.CamelContextFactory;
import nz.co.fortytwo.freeboard.server.NavDataWebSocketRoute;

import org.apache.camel.model.FromDefinition;
import org.apache.camel.model.IdempotentConsumerDefinition;
import org.apache.camel.model.MarshalDefinition;
import org.apache.camel.model.MulticastDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.ToDefinition;
import org.apache.camel.model.UnmarshalDefinition;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * Prints out a graphviz dot file that will create a pretty picture of the
 * configured routes (www.graphviz.org). You must install graphviz to convert
 * the dot file to png.
 * <p>
 * The command line for graphviz is 'dot -o routes.png -T png routes.dot', executed in the src/test/resources dir.
 * 
 * @author huitemar
 * 
 */

public class CreateCamelDiagramTest {

	private static final Logger LOG = Logger.getLogger(CreateCamelDiagramTest.class);

	private static String DOT_FILE_NAME = "src/test/resources/routes.dot";

	private Map<String, String> toQueues = new HashMap<String, String>();
	private Map<String, String> fromQueues = new HashMap<String, String>();
	private int z=0;
	
	@Test
	public void shouldPrintRouteBuilder() throws Exception {

		// context.addRoutes(createIntegrationTestRoute());

		StringBuffer buffer = new StringBuffer();
		System.out.println("Start");
		buffer.append("\ndigraph simple {\n");
		NavDataWebSocketRoute freeboardRoute = new NavDataWebSocketRoute(Util.getConfig(null));
		CamelContextFactory.setContext(freeboardRoute);
		freeboardRoute.configure();
		List<RouteDefinition> routes = freeboardRoute.getRouteCollection().getRoutes();
		//List<RouteDefinition> routes = freeboardRoute.getContext().getRouteDefinitions();
		System.out.println("  Routes:" + routes.size());
		int c = 0;
		String node = "start";
		for (RouteDefinition route : routes) {
			FromDefinition from = route.getInputs().get(0);
			System.out.println("LOG:   From " + route.toString());
			node = route.getShortName() + c;
			c++;
			node = node.replaceAll("-", "");
			fromQueues.put(from.getLabel(), node);
			String shape = "shape=invhouse, ";
			String fill = "style=filled, fillcolor=seagreen, ";
			buffer.append(node + " [" + shape + fill + "label = \"from " + from.getLabel() + "\"];\n");
			recurseRoute(route, node, buffer);

		}
		// now join the routes
		System.out.println("Froms:" + fromQueues.keySet());
		System.out.println("Tos:" + toQueues.keySet());

		for (String label : toQueues.keySet()) {
			if (fromQueues.containsKey(label)) {

				String link = toQueues.get(label) + " -> " + fromQueues.get(label) + ";\n";
				if (buffer.indexOf(link) < 0) {
					buffer.append(link);
				}
			}
		}
		buffer.append("}");
		// line wrap crudely
		String dotFileStr = buffer.toString();
		dotFileStr = StringUtils.replace(dotFileStr, "?", "\\n");
		dotFileStr = StringUtils.replace(dotFileStr, "&", "\\n");
		System.out.println(dotFileStr);
		File dotFile = new File(DOT_FILE_NAME);
		System.out.println("Write dot file:" + dotFile.getAbsolutePath());
		FileUtils.writeStringToFile(dotFile, dotFileStr);

	}

	private void recurseRoute(ProcessorDefinition<?> mp, String node, StringBuffer buffer) {

		String lastNode = node;
		node = null;
		for (ProcessorDefinition<?> p : mp.getOutputs()) {
			System.out.println(p.getLabel());
			node = p.getId();
			if (node == null)
				node = p.getShortName();
			node = node.replaceAll("-", "");
			// add unique definition
			addNodeAndLink(buffer, p, node, lastNode, getLabel(p));

			// multicast, we split, not chain
			if (p instanceof MulticastDefinition) {
				MulticastDefinition m = (MulticastDefinition) p;
				for (ProcessorDefinition<?> pd : m.getOutputs()) {
					System.out.println("   multicast recurse to: " + pd.getLabel() + " : " + pd.getClass());
					String id = p.getId();
					if(id==null){
							id=p.getShortName()+z;
							z++;
					}
					if (pd instanceof ToDefinition) {
						ToDefinition t = (ToDefinition) pd;
						toQueues.put(t.getLabel(), id);
						System.out.println("   Added to: " + t.getLabel());
					}
					addNodeAndLink(buffer, pd, id, node, pd.getLabel());

				}

			} else {
				lastNode = node;
				recurseRoute(p, lastNode, buffer);
			}

		}
	}

	/**
	 * Mangle awkward labels
	 * 
	 * @param p
	 * @return
	 */
	private String getLabel(ProcessorDefinition<?> p) {
		String label = p.getLabel();
		if (p instanceof UnmarshalDefinition) {
			UnmarshalDefinition m = (UnmarshalDefinition) p;
			label = "unmarshall\\n[" + m.getDataFormatType().getDataFormat().getClass().getSimpleName() + "]";
		}
		if (p instanceof MarshalDefinition) {
			MarshalDefinition m = (MarshalDefinition) p;
			label = "marshall\\n[" + m.getDataFormatType().getDataFormat().getClass().getSimpleName() + "]";
		}
		if (p instanceof IdempotentConsumerDefinition) {
			if (((IdempotentConsumerDefinition) p) != null) {
				label = "idempotentConsumer";
				if (((IdempotentConsumerDefinition) p).isEager()) {
					label = label + "\\neager";
				}
				if (((IdempotentConsumerDefinition) p).isSkipDuplicate()) {
					label = label + "\\nnoDuplicate";
				}
			}
		}
		return label;
	}

	private void addNodeAndLink(StringBuffer buffer, ProcessorDefinition<?> p, String id, String lastNode, String label) {
		// add unique definition
		String shape = "shape=invhouse, ";
		String fill = "style=filled, fillcolor=khaki, ";
		if (p instanceof ToDefinition) {
			shape = "shape=invhouse, ";
			fill = "style=filled, fillcolor=darkkhaki, ";
		}
		
		buffer.append(id + " [" + shape + fill + " label = \"" + label + "\"];\n");
		String link = lastNode + " -> " + id + ";\n";
		if (buffer.indexOf(link) < 0) {
			buffer.append(link);
		}
	}
}
