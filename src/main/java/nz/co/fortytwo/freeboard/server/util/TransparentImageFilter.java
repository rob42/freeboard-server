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

import java.awt.image.RGBImageFilter;

/**
 * Make white transparent, allow 10% fuzzy
 * @author robert
 *
 */
public class TransparentImageFilter extends RGBImageFilter {
	// the color we are looking for... Alpha bits are set to opaque
	//private int markerRGB = Color.white.getRGB() | 0xFF000000;


	public final int filterRGB(int x, int y, int rgb) {

		//if ((rgb | 0xFF000000) == markerRGB) {
		if(((rgb >> 16) & 0xFF) > 250
				&& ((rgb >> 8) & 0xFF) > 250
				&& ((rgb >> 0) & 0xFF) > 250){
			// Mark the alpha bits as zero - transparent
			return 0x00FFFFFF & rgb;
		} else {
			// nothing to do
			return rgb;
		}
	}

}
