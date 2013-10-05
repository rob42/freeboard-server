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

/** based on http://www.java-examples.com/create-zip-file-directory-recursively-using-zipoutputstream-example */
package nz.co.fortytwo.freeboard.server.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class ZipUtils {
	private static Logger logger = Logger.getLogger(ZipUtils.class);

	public static void zip(File sourceDir, File zipFile) {

		try {
			// create object of FileOutputStream
			FileOutputStream fout = new FileOutputStream(zipFile);

			// create object of ZipOutputStream from FileOutputStream
			ZipOutputStream zout = new ZipOutputStream(fout);
			addDirectory(zout, sourceDir, sourceDir);

			// close the ZipOutputStream
			zout.close();

			logger.info("Zip file has been created!");

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	/**
	 * Add the directory recursively into a zip file
	 * @param zout
	 * @param fileSource
	 * @param sourceDir
	 */
	private static void addDirectory(ZipOutputStream zout, File fileSource, File sourceDir) {

		// get sub-folder/files list
		File[] files = fileSource.listFiles();

		logger.debug("Adding directory " + fileSource.getName());

		for (int i = 0; i < files.length; i++) {
			try {
				String name = files[i].getAbsolutePath();
				name = name.substring((int) sourceDir.getAbsolutePath().length());
				// if the file is directory, call the function recursively
				if (files[i].isDirectory()) {
					addDirectory(zout, files[i], sourceDir);
					continue;
				}

				/*
				 * we are here means, its file and not directory, so
				 * add it to the zip file
				 */

				logger.debug("Adding file " + files[i].getName());

				// create object of FileInputStream
				FileInputStream fin = new FileInputStream(files[i]);

				zout.putNextEntry(new ZipEntry(name));

				IOUtils.copy(fin, zout);
				
				zout.closeEntry();

				// close the InputStream
				fin.close();

			} catch (IOException ioe) {
				logger.error(ioe.getMessage(), ioe);
			}
		}

	}

	/**
	 * Unzip a zipFile into a directory
	 * @param targetDir
	 * @param zipFile
	 * @throws ZipException
	 * @throws IOException
	 */
	public static void unzip(File targetDir,File zipFile) throws ZipException, IOException{
		ZipFile zip = new ZipFile(zipFile);
		
		@SuppressWarnings("unchecked")
		Enumeration<ZipEntry> z = (Enumeration<ZipEntry>) zip.entries();
		while(z.hasMoreElements()){
			ZipEntry entry = z.nextElement();
			File f = new File(targetDir, entry.getName());
			if(f.isDirectory()){
				if(!f.exists()){
					f.mkdirs();
				}
			}else{
				f.getParentFile().mkdirs();
				InputStream in = zip.getInputStream(entry);
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
				IOUtils.copy(in, out);
				in.close();
				out.flush();
				out.close();
			}
			
		}
		zip.close();
	}
}