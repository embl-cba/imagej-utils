/*-
 * #%L
 * TODO
 * %%
 * Copyright (C) 2018 - 2020 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.tables.image;

import bdv.util.RandomAccessibleIntervalSource4D;
import de.embl.cba.tables.Logger;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.io.File;

public class SourceLoader
{
	private final File file;
	private int numSpatialDimensions;

	public SourceLoader( File file )
	{
		this.file = file;
	}

	public RandomAccessibleIntervalSource4D getRandomAccessibleIntervalSource4D( )
	{
		final ImagePlus imagePlus = IJ.openImage( file.toString() );

		if ( imagePlus.getNChannels() > 1 )
		{
			Logger.error( "Only single channel image are supported.");
			return null;
		}

		RandomAccessibleInterval< RealType > wrap = ImageJFunctions.wrapReal( imagePlus );

		if ( imagePlus.getNFrames() == 1 )
		{
			// needs to be a movie
			wrap = Views.addDimension( wrap, 0, 0 );
		}


		if ( imagePlus.getNSlices() == 1 )
		{
			numSpatialDimensions = 2;
			// needs to be 3D
			wrap = Views.addDimension( wrap, 0, 0 );
			wrap = Views.permute( wrap, 2, 3 );
		}
		else
		{
			numSpatialDimensions = 2;
		}

		return new RandomAccessibleIntervalSource4D( wrap, Util.getTypeFromInterval( wrap ), imagePlus.getTitle() );
	}

	public int getNumSpatialDimensions()
	{
		return numSpatialDimensions;
	}
}
