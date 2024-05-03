/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2024 EMBL
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
package de.embl.cba.tables;

import bdv.BigDataViewer;
import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import de.embl.cba.bdv.utils.BdvUtils;
import ij.ImagePlus;
import mpicbg.spim.data.SpimData;
import net.imglib2.util.Intervals;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Utils
{
	public static void removeCalibration( ImagePlus labelImage )
	{
		labelImage.getCalibration().setUnit( "pixel" );
		labelImage.getCalibration().pixelHeight = 1;
		labelImage.getCalibration().pixelWidth = 1;
		labelImage.getCalibration().pixelDepth = 1;
	}

	public static ArrayList< double[] > getVoxelSpacings( Source< ? > labelsSource )
	{
		final ArrayList< double[] > voxelSpacings = new ArrayList<>();
		final int numMipmapLevels = labelsSource.getNumMipmapLevels();
		for ( int level = 0; level < numMipmapLevels; ++level )
			voxelSpacings.add( BdvUtils.getCalibration( labelsSource, level ) );

		return voxelSpacings;
	}

	public static Integer getLevel( Source< ? > source, long maxNumVoxels )
	{
		final ArrayList< double[] > voxelSpacings = getVoxelSpacings( source );

		for ( int level = 0; level < voxelSpacings.size(); level++ )
		{
			final long numElements = Intervals.numElements( source.getSource( 0, level ) );

			if ( numElements <= maxNumVoxels )
				return level;
		}
		return null;
	}

	public static Source< ? > getSource( SpimData spimData, int sourceIndex )
	{
		ArrayList< ConverterSetup > converterSetups = new ArrayList<>();
		ArrayList< SourceAndConverter< ? > > sources = new ArrayList<>();
		BigDataViewer.initSetups( spimData, converterSetups, sources );
		return sources.get( sourceIndex ).getSpimSource();
	}

	public static void fetchFutures( ArrayList< Future > futures )
	{
		for ( Future future : futures )
		{
			try
			{
				future.get();
				// Logger.info( "Added object to 3D Viewer " + (i++) + "/" + futures.size() );
			} catch ( InterruptedException e )
			{
				e.printStackTrace();
			} catch ( ExecutionException e )
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Note: It is important not(!) to catch the Double.parseDouble error inside of this function,
	 * because this error is used by other functions as an indicator whether the
	 * cell is numeric.
	 *
	 * @param cell
	 * @return
	 */
	public static Double parseDouble( String cell )
	{
		if ( cell.equalsIgnoreCase( "nan" )
				|| cell.equalsIgnoreCase( "na" )
				|| cell.equals( "" ) )
			return Double.NaN;
		else if ( cell.equalsIgnoreCase( "inf" ) )
			return Double.POSITIVE_INFINITY;
		else if ( cell.equalsIgnoreCase( "-inf" ) )
			return Double.NEGATIVE_INFINITY;
		else
			return Double.parseDouble( cell );
	}

	public static String toStringWithoutSuperfluousDecimals(double d)
	{
		if(d == (long) d)
			return String.format("%d",(long)d);
		else
			return String.format("%s",d);
	}
}
