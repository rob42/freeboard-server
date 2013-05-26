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

import java.awt.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.zkoss.mesg.MessageConst;

/** @see http://stackoverflow.com/questions/4053090 */
public class ChartManager extends JFrame {

	private static final String title = "Select a file";
	private ProcessingPanel processingPanel = new ProcessingPanel();
	private JLabel result = new JLabel(title, JLabel.CENTER);
	private ChartFileChooser chartFileChooser = new ChartFileChooser();

	public ChartManager(String name) {
		super(name);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.addWidgets();
		this.pack();
		this.setVisible(true);
	}

	private void addWidgets() {
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Charts", "tiff", "kap", "KAP", "TIFF", "tif", "TIF");
		chartFileChooser.setFileFilter(filter);
		this.add(chartFileChooser, BorderLayout.WEST);

		this.add(processingPanel, BorderLayout.CENTER);
		this.add(result, BorderLayout.SOUTH);
		
	}

	class ChartFileChooser extends JFileChooser {

		@Override
		public void approveSelection() {
			final File f = chartFileChooser.getSelectedFile();
			
			new Thread(){

				@Override
				public void run() {
					processingPanel.process(f);
				}
				
			}.start();
		}

		@Override
		public void cancelSelection() {
			processingPanel.clear();
			result.setText(title);
		}
	}

	class ProcessingPanel extends JPanel {

		private JTextArea textArea = new JTextArea();
		private JScrollPane scrollPane;

		public ProcessingPanel() {
			this.setPreferredSize(new Dimension(500, 700));
			scrollPane = new JScrollPane(textArea);
			scrollPane.setPreferredSize(new Dimension(480, 680));
			textArea.setBorder(BorderFactory.createLineBorder(Color.black, 1));
			this.add(scrollPane);
		}

		public boolean process(File f) {
			//one at a time
			chartFileChooser.setEnabled(false);
			System.out.println("Processing " + f.getAbsolutePath());
			try {
				ChartProcessor processor = new ChartProcessor(true,textArea);
				redirectSystemStreams();
				processor.processChart(f, true);
			
				
			} catch (Exception e) {
				System.out.print(e.getMessage() + "\n");
				e.printStackTrace();
				return false;
			}finally{
				chartFileChooser.setEnabled(true);
			}
			return true;
		}

		public void clear() {
			SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
			      textArea.setText("");
			    }
			  });

		}
		private void updateTextArea(final String text) {
			 SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
			      textArea.append(text);
			    }
			  });
			}
			 
			private void redirectSystemStreams() {
			  OutputStream out = new OutputStream() {
			    @Override
			    public void write(int b) throws IOException {
			      updateTextArea(String.valueOf((char) b));
			    }
			 
			    @Override
			    public void write(byte[] b, int off, int len) throws IOException {
			      updateTextArea(new String(b, off, len));
			    }
			 
			    @Override
			    public void write(byte[] b) throws IOException {
			      write(b, 0, b.length);
			    }
			  };
			 
			  System.setOut(new PrintStream(out, true));
			  System.setErr(new PrintStream(out, true));
			}

	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				new ChartManager("Chart Manager").setVisible(true);
			}
		});
	}
}
