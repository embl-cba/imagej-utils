/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2022 EMBL
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
package de.embl.cba.morphometry;

import de.embl.cba.morphometry.geometry.CentroidsParameters;
import de.embl.cba.morphometry.geometry.CoordinatesAndValues;
import de.embl.cba.morphometry.regions.Regions;
import de.embl.cba.transforms.utils.Transforms;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import ij.process.LUT;
import loci.plugins.in.ImagePlusReader;
import loci.plugins.in.ImportProcess;
import loci.plugins.in.ImporterOptions;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.*;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.algorithm.neighborhood.HyperSphereShape;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.outofbounds.OutOfBounds;
import net.imglib2.roi.labeling.*;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.AbstractIntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Intervals;
import net.imglib2.util.LinAlgHelpers;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.log.LogService;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.*;

import static de.embl.cba.morphometry.Constants.*;
import static de.embl.cba.morphometry.viewing.BdvViewer.show;
import static java.lang.Math.acos;
import static java.lang.Math.ceil;

public class Utils
{
	public static int imagePlusChannelDimension = 2;

	public static String logFilePath = null;

	public static ImagePlus openWithBioFormats( File file )
	{
		return openWithBioFormats( file.getAbsolutePath() );
	}


	public static void setNewLogFilePath( String aLogFilePath )
	{
		logFilePath = aLogFilePath;
		createLogFile();
	}

	public static void writeToLogFile( String message )
	{
		try {
			Files.write( Paths.get( logFilePath ), message.getBytes(), StandardOpenOption.APPEND);
		}
		catch (IOException e) {
			//exception handling left as an exercise for the reader
		}
	}

	public static void createLogFile()
	{
		PrintWriter writer = null;
		try
		{
			writer = new PrintWriter( logFilePath, "UTF-8" );
			writer.println( "Start logging..." );
		}
		catch ( FileNotFoundException e )
		{
			e.printStackTrace();
		}
		catch ( UnsupportedEncodingException e )
		{
			e.printStackTrace();
		}
		writer.close();
	}


	public static void log( String message, LogService logService )
	{
		logService.info( message );
	}

	public static < T extends RealType< T > & NativeType< T > >
	CoordinatesAndValues computeAverageIntensitiesAlongAxis(
			RandomAccessibleInterval< T > rai,
			double maxAxisDist,
			int axis,
			double axisMin, // calibrated
			double axisMax,
			double calibration )
	{
		final CoordinatesAndValues coordinatesAndValues = new CoordinatesAndValues();

		final double xMin = axisMin / calibration;
		final double xMax = axisMax / calibration;
		for ( long x = ( long ) xMin; x <= ( long ) xMax; ++x )
		{
			final IntervalView< T > intensitySlice = Views.hyperSlice( rai, axis, x );
			coordinatesAndValues.coordinates.add( (double) x * calibration );
			coordinatesAndValues.values.add( computeAverage( intensitySlice, maxAxisDist, calibration ) );
		}

		return coordinatesAndValues;
	}

	public static < T extends RealType< T > & NativeType< T > >
	CoordinatesAndValues computeAverageIntensitiesAlongAxis(
			RandomAccessibleInterval< T > rai,
			double maxAxisDist,
			int axis,
			double calibration )
	{
		return computeAverageIntensitiesAlongAxis( rai, maxAxisDist, axis, rai.min( axis ) * calibration, rai.max( axis ) * calibration, calibration );
	}

	public static long countNonZeroPixelsAlongAxis(
			RandomAccessibleInterval< BitType > rai,
			int axis )
	{
		// Set position at zero
		final RandomAccess< BitType > access = rai.randomAccess();
		access.setPosition( new long[ rai.numDimensions() ] );

		long numNonZeroPixels = 0;
		for ( long coordinate = rai.min( axis ); coordinate <= rai.max( axis ); ++coordinate )
		{
			access.setPosition( coordinate, axis );
			if ( access.get().get() ) numNonZeroPixels++;
		}

		return numNonZeroPixels;
	}


	public static < T extends RealType< T > & NativeType< T > >
	CoordinatesAndValues computeMaximumIntensitiesAlongAxis(
			RandomAccessibleInterval< T > rai, double maxAxisDist, int axis, double calibration )
	{
		final CoordinatesAndValues coordinatesAndValues = new CoordinatesAndValues();

		for ( long coordinate = rai.min( axis ); coordinate <= rai.max( axis ); ++coordinate )
		{
			final IntervalView< T > intensitySlice = Views.hyperSlice( rai, axis, coordinate );
			coordinatesAndValues.coordinates.add( (double) coordinate * calibration );
			coordinatesAndValues.values.add( computeMaximum( intensitySlice, maxAxisDist ) );
		}

		return coordinatesAndValues;
	}

	public static double sum( List<Double> values ){
		if (values.size() > 0) {
			double sum = 0;
			for (Double d : values) {
				sum += d;
			}
			return sum;
		}
		return 0;
	}

	public static double mean( List<Double> values ){
		double sum = sum( values );
		double mean = 0;
		mean = sum / ( values.size() * 1.0 );
		return mean;
	}

	public static double sdev( List<Double> values, Double mean ){
		double sdev = 0;
		for( double v : values )
			sdev += ( v - mean ) * ( v - mean );
		sdev /= ( values.size() * 1.0 );
		sdev = Math.sqrt( sdev );
		return sdev;
	}

	public static double median( List<Double> values )
	{
		final ArrayList< Double > sorted = new ArrayList<>( values );

		Collections.sort( sorted );

		if ( sorted.size() == 1 ) return sorted.get( 0 );
		if ( sorted.size() == 0 ) return 0;

		int middle = sorted.size()/2;

		if (sorted.size() % 2 == 1) {
			return sorted.get(middle);
		} else {
			return (sorted.get(middle-1) + sorted.get(middle)) / 2.0;
		}
	}

	/**
	 * Compute the median absolute deviation.
	 *
	 * @param values
	 * @param median
	 * @return
	 */
	public static double mad( List<Double> values, Double median )
	{
		final ArrayList< Double > absoluteDeviations = new ArrayList<>();

		for ( Double value : values )
		{
			absoluteDeviations.add( Math.abs( value - median ) );
		}

		return median( absoluteDeviations );
	}

	public static < T extends RealType< T > & NativeType< T > >
	CoordinatesAndValues computeAverageIntensitiesAlongAxisWithinMask( RandomAccessibleInterval< T > rai, RandomAccessibleInterval< BitType > mask, int axis, double calibration )
	{
		final CoordinatesAndValues coordinatesAndValues = new CoordinatesAndValues();

		for ( long coordinate = rai.min( axis ); coordinate <= rai.max( axis ); ++coordinate )
		{
			final IntervalView< T > intensitySlice = Views.hyperSlice( rai, axis, coordinate );
			final IntervalView< BitType > maskSlice = Views.hyperSlice( mask, axis, coordinate );

			coordinatesAndValues.coordinates.add( (double) coordinate * calibration );
			coordinatesAndValues.values.add( computeAverage( intensitySlice, maskSlice ) );
		}

		return coordinatesAndValues;
	}

	public static < T extends RealType< T > & NativeType< T > >
	CoordinatesAndValues computeAverageIntensitiesAlongAxis(
			RandomAccessibleInterval< T > rai, int axis, double calibration )
	{
		final CoordinatesAndValues coordinatesAndValues = new CoordinatesAndValues();

		for ( long coordinate = rai.min( axis ); coordinate <= rai.max( axis ); ++coordinate )
		{
			final IntervalView< T > intensitySlice = Views.hyperSlice( rai, axis, coordinate );
			coordinatesAndValues.coordinates.add( (double) coordinate * calibration );
			coordinatesAndValues.values.add( computeAverage( intensitySlice ) );
		}

		return coordinatesAndValues;
	}


	public static <T extends RealType<T> & NativeType< T > >
	ArrayList< RandomAccessibleInterval< T > > maskAllChannels(
			ArrayList< RandomAccessibleInterval< T > > channels,
			RandomAccessibleInterval< BitType > mask,
			boolean showImages )
	{
		ArrayList< RandomAccessibleInterval< T > > maskedChannels = new ArrayList<>(  );

		long numChannels = channels.size();

		for ( int c = 0; c < numChannels; ++c )
		{
			final RandomAccessibleInterval< T > channel = Utils.copyAsArrayImg( channels.get( c ) );
			Utils.applyMask( channel, mask );
			maskedChannels.add( channel );

			if ( showImages ) show( channel, "masked channel " + c,  Transforms.origin(), 1.0 );

		}

		return maskedChannels;
	}

	public static CentroidsParameters computeCentroidsParametersAlongXAxis(
			RandomAccessibleInterval< BitType > rai,
			double calibration,
			double maxDistanceToCenter )
	{

		CentroidsParameters centroidsParameters = new CentroidsParameters();

		final double[] unitVectorInNegativeZDirection = new double[]{ 0, -1 };

		final double[] centralCentroid = computeCentroidPerpendicularToAxis( rai, X, 0 ); // this is not very robust, because the central one could be off

		for ( long coordinate = rai.min( X ); coordinate <= rai.max( X ); ++coordinate )
		{

			if ( Math.abs( coordinate * calibration ) < maxDistanceToCenter )
			{

				final double[] centroid = computeCentroidPerpendicularToAxis( rai, X, coordinate );
				final long numVoxels = computeNumberOfVoxelsPerpendicularToAxis( rai, X, coordinate );

				if ( centroid != null )
				{
					//				double[] centerDisplacementVector = subtract( centroid, centralCentroid ); // this is not very robust, because the central one could be off

					double[] centerDisplacementVector = centroid;
					double centerDisplacementLength = LinAlgHelpers.length( centerDisplacementVector );

					/**
					 *  centroid[ 0 ] is the y-axis coordinate
					 *  the sign of the y-axis coordinate determines the sign of the angle,
					 *  i.e. the direction of rotation
					 */
					final double angle = Math.signum( centerDisplacementVector[ 0 ] ) * 180 / Math.PI * acos( dotProduct( centerDisplacementVector, unitVectorInNegativeZDirection ) / centerDisplacementLength );

					centroidsParameters.distances.add( centerDisplacementLength * calibration );
					centroidsParameters.angles.add( angle );
					centroidsParameters.axisCoordinates.add( ( double ) coordinate * calibration );
					centroidsParameters.centroids.add( new RealPoint( coordinate * calibration, centroid[ 0 ] * calibration, centroid[ 1 ] * calibration ) );
					centroidsParameters.numVoxels.add( ( double ) numVoxels );
				}
			}
		}

		return centroidsParameters;

	}

	public static double vectorLength( double[] vector )
	{
		double norm = 0;

		for ( int d = 0; d < vector.length; ++d )
		{
			norm += vector[ d ] * vector[ d ];
		}

		norm = Math.sqrt( norm );

		return norm;
	}

	public static double dotProduct( double[] vector01, double[] vector02  )
	{
		double dotProduct = 0;

		for ( int d = 0; d < vector01.length; ++d )
		{
			dotProduct += vector01[ d ] * vector02[ d ];
		}

		return dotProduct;
	}

	public static double[] subtract( double[] vector01, double[] vector02  )
	{
		double[] subtraction = new double[ vector01.length ];

		for ( int d = 0; d < vector01.length; ++d )
		{
			subtraction[d] = vector01[ d ] - vector02[ d ];
		}

		return subtraction;
	}


	private static double[] computeCentroidPerpendicularToAxis( RandomAccessibleInterval< BitType > rai, int axis, long coordinate )
	{
		final IntervalView< BitType > slice = Views.hyperSlice( rai, axis, coordinate );

		int numHyperSliceDimensions = rai.numDimensions() - 1;

		final double[] centroid = new double[ numHyperSliceDimensions ];

		int numPoints = 0;

		final Cursor< BitType > cursor = slice.cursor();

		while( cursor.hasNext() )
		{
			if( cursor.next().get() )
			{
				for ( int d = 0; d < numHyperSliceDimensions; ++d )
				{
					centroid[ d ] += cursor.getLongPosition( d );
					numPoints++;
				}
			}
		}

		if ( numPoints > 0 )
		{
			for ( int d = 0; d < numHyperSliceDimensions; ++d )
			{
				centroid[ d ] /= 1.0D * numPoints;
			}
			return centroid;
		}
		else
		{
			return null;
		}
	}

	private static long computeNumberOfVoxelsPerpendicularToAxis( RandomAccessibleInterval< BitType > rai, int axis, long coordinate )
	{
		final IntervalView< BitType > slice = Views.hyperSlice( rai, axis, coordinate );

		int numHyperSliceDimensions = rai.numDimensions() - 1;

		final double[] centroid = new double[ numHyperSliceDimensions ];

		int numPoints = 0;

		final Cursor< BitType > cursor = slice.cursor();

		while ( cursor.hasNext() )
		{
			if ( cursor.next().get() )
			{
				numPoints++;
			}
		}

		return numPoints;
	}

	public static < T extends RealType< T > & NativeType< T > >
	RandomAccessibleInterval< T > createBlurredRai(
			RandomAccessibleInterval< T > rai,
			double sigma,
			double scaling )
	{
		ImgFactory< T > imgFactory = new ArrayImgFactory( rai.randomAccess().get()  );

		RandomAccessibleInterval< T > blurred = imgFactory.create( Intervals.dimensionsAsLongArray( rai ) );

		blurred = Views.translate( blurred, Intervals.minAsLongArray( rai ) );

		Gauss3.gauss( sigma / scaling, Views.extendBorder( rai ), blurred ) ;

		return blurred;
	}

	public static < T extends RealType< T > & NativeType< T > >
	RandomAccessibleInterval< T > createGaussFilteredArrayImg(
			RandomAccessibleInterval< T > rai,
			double[] sigmas )
	{
		ImgFactory< T > imgFactory = new ArrayImgFactory( rai.randomAccess().get()  );

		RandomAccessibleInterval< T > blurred = imgFactory.create( Intervals.dimensionsAsLongArray( rai ) );

		blurred = Views.translate( blurred, Intervals.minAsLongArray( rai ) );

		Gauss3.gauss( sigmas, Views.extendBorder( rai ), blurred ) ;

		return blurred;
	}


	public static  < T extends RealType< T > & NativeType< T > >
	void applyMask(
			RandomAccessibleInterval< T > rai,
			RandomAccessibleInterval< BitType > mask )
	{
		final Cursor< T > cursor = Views.iterable( rai ).cursor();
		final OutOfBounds< BitType > maskAccess =
				Views.extendZero( mask ).randomAccess();

  		while ( cursor.hasNext() )
		{
			cursor.fwd();
			maskAccess.setPosition( cursor );
			if ( ! maskAccess.get().get() )
				cursor.get().setZero();
		}
	}

	public static < T extends RealType< T > & NativeType< T > >
	RandomAccessibleInterval< T > createAverageProjectionAlongAxis(
			RandomAccessibleInterval< T > rai, int d, double min, double max, double scaling )
	{
		Projection< T > projection = new Projection< T >(  rai, d,  (long) ( min / scaling) , (long) ( max / scaling )  );
		return projection.average();
	}

	public static < T extends RealType< T > & NativeType< T > >
	RandomAccessibleInterval< T > createSumProjectionAlongAxis(
			RandomAccessibleInterval< T > rai, int d, double min, double max, double scaling )
	{
		Projection< T > projection = new Projection< T >(  rai, d,  (long) ( min / scaling) , (long) ( max / scaling )  );
		return projection.sum();
	}

	public static < T extends RealType< T > & NativeType< T > > RandomAccessibleInterval< BitType > createBinaryImage( RandomAccessibleInterval< T > input, double doubleThreshold )
	{
		final ArrayImg< BitType, LongArray > binaryImage = ArrayImgs.bits( Intervals.dimensionsAsLongArray( input ) );

		T threshold = input.randomAccess().get().copy();
		threshold.setReal( doubleThreshold );

		final BitType one = new BitType( true );
		final BitType zero = new BitType( false );

		LoopBuilder.setImages( input, binaryImage ).forEachPixel( ( i, b ) ->
				{
					b.set( i.compareTo( threshold ) > 0 ?  one : zero );
				}
		);

		return binaryImage;

	}


	public static double[] getCalibration( ImagePlus imp )
	{
		double[] calibration = new double[ 3 ];

		calibration[ X ] = imp.getCalibration().pixelWidth;
		calibration[ Y ] = imp.getCalibration().pixelHeight;
		calibration[ Z ] = imp.getCalibration().pixelDepth;

		return calibration;
	}

	public static double[] get2dCalibration( Calibration calibration )
	{
		double[] calibration2D = new double[ 2 ];

		calibration2D[ X ] = calibration.pixelWidth;
		calibration2D[ Y ] = calibration.pixelHeight;

		return calibration2D;
	}

	public static void correctCalibrationForSubSampling( double[] calibration, int subSampling )
	{
		for ( int d : XYZ )
		{
			calibration[ d ] *= subSampling;
		}
	}

	public static < T extends RealType< T > & NativeType< T > >
	double computeAverage( final RandomAccessibleInterval< T > rai )
	{
		final Cursor< T > cursor = Views.iterable( rai ).cursor();

		double average = 0;

		while ( cursor.hasNext() )
		{
			average += cursor.next().getRealDouble();
		}

		average /= Views.iterable( rai ).size();

		return average;
	}

	public static < T extends RealType< T > & NativeType< T > >
	double computeAverage( final RandomAccessibleInterval< T > rai,
						   double maxCenterDist,
						   double voxelSpacing )
	{
		final double maxVoxelDist = maxCenterDist / voxelSpacing;

		final Cursor< T > cursor = Views.iterable( rai ).cursor();

		double average = 0;
		long n = 0;
		double[] position = new double[ rai.numDimensions() ];

		while ( cursor.hasNext() )
		{
			cursor.fwd();
			cursor.localize( position );
			if ( Utils.vectorLength( position ) <= maxVoxelDist )
			{
				average += cursor.get().getRealDouble();
				++n;
			}
		}

		average /= n;

		return average;
	}

	public static < T extends RealType< T > & NativeType< T > >
	double computeMaximum( final RandomAccessibleInterval< T > rai, double maxAxisDist )
	{
		final Cursor< T > cursor = Views.iterable( rai ).cursor();

		double max = - Double.MAX_VALUE;
		double[] position = new double[ rai.numDimensions() ];

		while ( cursor.hasNext() )
		{
			cursor.fwd();
			cursor.localize( position );
			if ( Utils.vectorLength( position ) <= maxAxisDist)
			{
				if( cursor.get().getRealDouble() > max )
				{
					max = cursor.get().getRealDouble();
				}
			}
		}

		return max;
	}


	public static < T extends RealType< T > & NativeType< T > >
	double[] computeMaximumLocation(
			final RandomAccessibleInterval< T > rai,
			double maxAxisDist )
	{
		final Cursor< T > cursor = Views.iterable( rai ).cursor();

		double max = - Double.MAX_VALUE;
		double[] position = new double[ rai.numDimensions() ];
		double[] maxLoc = new double[ rai.numDimensions() ];

		while ( cursor.hasNext() )
		{
			cursor.fwd();
			cursor.localize( position );
			if ( Utils.vectorLength( position ) <= maxAxisDist)
			{
				if( cursor.get().getRealDouble() > max )
				{
					max = cursor.get().getRealDouble();
					cursor.localize( maxLoc );
				}
			}
		}

		return maxLoc;
	}

	public static < T extends RealType< T > & NativeType< T > >
	double computeAverage( final RandomAccessibleInterval< T > rai,
						   final RandomAccessibleInterval< BitType > mask )
	{
		final Cursor< BitType > cursor = Views.iterable( mask ).cursor();
		final RandomAccess< T > randomAccess = rai.randomAccess();

		randomAccess.setPosition( cursor );

		double average = 0;
		long n = 0;

		while ( cursor.hasNext() )
		{
			if ( cursor.next().get() )
			{
				randomAccess.setPosition( cursor );
				average += randomAccess.get().getRealDouble();
				++n;
			}
		}

		average /= n;

		return average;
	}

	public static < T extends RealType< T > & NativeType< T > >
	ArrayList< Double > getValuesWithinMaskAsList(
			final RandomAccessibleInterval< T > rai,
			final RandomAccessibleInterval< BitType > mask )
	{
		final Cursor< BitType > maskCursor = Views.iterable( mask ).cursor();
		final RandomAccess< T > intensityAccess = rai.randomAccess();

		intensityAccess.setPosition( maskCursor );

		final ArrayList< Double > doubles = new ArrayList<>();

		while ( maskCursor.hasNext() )
		{
			if ( maskCursor.next().get() )
			{
				intensityAccess.setPosition( maskCursor );
				doubles.add( intensityAccess.get().getRealDouble() );
			}
		}

		return doubles;
	}

	public static < T extends RealType< T > & NativeType< T > >
	List< RealPoint > computeLocalMaxima(
			RandomAccessibleInterval< T > blurred,
			int sigmaForBlurringAverageProjection )
	{
		Shape shape = new HyperSphereShape( sigmaForBlurringAverageProjection );

		List< RealPoint > points = Algorithms.findLocalMaximumValues( blurred, shape );

		return points;
	}

	public static List< RealPoint > asRealPointList( RealPoint maximum )
	{
		List< RealPoint > realPoints = new ArrayList<>();
		final double[] doubles = new double[ maximum.numDimensions() ];
		maximum.localize( doubles );
		realPoints.add( new RealPoint( doubles) );

		return realPoints;
	}

	public static < T extends RealType< T > & NativeType< T > >
	RandomAccessibleInterval< T > copyAsArrayImg( RandomAccessibleInterval< T > orig )
	{
		final RandomAccessibleInterval< T > copy =
				Views.translate(
					new ArrayImgFactory( Util.getTypeFromInterval( orig ) ).create( orig ),
						Intervals.minAsLongArray( orig ) );

		LoopBuilder.setImages( copy, orig ).forEachPixel( Type::set );

		return copy;
	}

	public static < T extends RealType< T > & NativeType< T > >
	ArrayList< RandomAccessibleInterval< BitType > >
	labelMapsAsMasks( ArrayList< RandomAccessibleInterval< T > > labelMaps )
	{
		final ArrayList< RandomAccessibleInterval< BitType > > masks = new ArrayList<>();

		long numTimePoints = labelMaps.size();

		for ( int t = 0; t < numTimePoints; ++t )
		{
			final RandomAccessibleInterval< BitType > mask = Utils.asMask( labelMaps.get( t ) );
			masks.add( mask );
		}

		return masks;
	}

	public static < T extends RealType< T > & NativeType< T > >
	long[] getCenterLocation( Interval rai )
	{
		int numDimensions = rai.numDimensions();

		long[] center = new long[ numDimensions ];

		for ( int d = 0; d < numDimensions; ++d )
		{
			center[ d ] = ( rai.max( d ) - rai.min( d ) ) / 2 + rai.min( d );
		}

		return center;
	}


	public static < T extends RealType< T > & NativeType< T > >
	boolean isBoundaryPixel( Cursor< T > cursor, RandomAccessibleInterval< T > rai )
	{
		int numDimensions = rai.numDimensions();
		final long[] position = new long[ numDimensions ];
		cursor.localize( position );


		for ( int d = 0; d < numDimensions; ++d )
		{
			if ( position[ d ] == rai.min( d ) ) return true;
			if ( position[ d ] == rai.max( d ) ) return true;
		}

		return false;
	}

	public static < T extends RealType< T > & NativeType< T > >
	boolean isLateralBoundaryPixel( Neighborhood< T > cursor, RandomAccessibleInterval< T > rai )
	{
		int numDimensions = rai.numDimensions();
		final long[] position = new long[ numDimensions ];
		cursor.localize( position );

		for ( int d = 0; d < numDimensions - 1; ++d )
		{
			if ( position[ d ] == rai.min( d ) ) return true;
			if ( position[ d ] == rai.max( d ) ) return true;
		}

		return false;

	}


	public static < T extends RealType< T > & NativeType< T > >
	RandomAccessibleInterval< T > invertedView( RandomAccessibleInterval< T > input )
	{
		final double maximum = Algorithms.getMaximumValue( input );
		Logger.log( "Inverting image; maximum value is: " + maximum );

		final RandomAccessibleInterval< T > inverted = Converters.convert( input, ( i, o ) -> {
			o.setReal( ( int ) ( maximum - i.getRealDouble() ) );
		},  Views.iterable( input ).firstElement() );

		return inverted;
	}

	public static long[] asLongs( double[] doubles )
	{
		final long[] longs = new long[ doubles.length ];

		for ( int i = 0; i < doubles.length; ++i )
		{
			longs[ i ] = (long) doubles[ i ];
		}

		return longs;
	}

	public static long[] asRoundedLongs( double[] doubles )
	{
		final long[] longs = new long[ doubles.length ];

		for ( int i = 0; i < doubles.length; ++i )
		{
			longs[ i ] = Math.round( doubles[ i ] );
		}

		return longs;
	}

	public static long[] divideAndReturnAsLongs( double[] doubles, double factor )
	{
		final long[] longs = new long[ doubles.length ];

		for ( int i = 0; i < doubles.length; ++i )
		{
			longs[ i ] = (long) ( doubles[ i ] / factor );
		}

		return longs;

	}

	public static void divide( double[] doubles, double factor )
	{
		for ( int i = 0; i < doubles.length; ++i )
			doubles[ i ] /= factor;
	}

	public static RandomAccessibleInterval< IntType >  asIntImg( ImgLabeling< Integer, IntType > labeling )
	{
		final RandomAccessibleInterval< IntType > intImg =
				Converters.convert( ( RandomAccessibleInterval< LabelingType< Integer > > ) labeling,
						( i, o ) -> {
							o.set( i.getIndex().getInteger() );
						}, new IntType() );

		return intImg;
	}

	public static double[] as2dDoubleArray( double value )
	{
		double[] array = new double[ 2 ];
		Arrays.fill( array, value );
		return array;
	}

	public static double[] as3dDoubleArray( double value )
	{
		double[] array = new double[ 3 ];
		Arrays.fill( array, value );
		return array;
	}

	public static FinalInterval getInterval( LabelRegion labelRegion )
	{
		final long[] min = Intervals.minAsLongArray( labelRegion );
		final long[] max = Intervals.maxAsLongArray( labelRegion );
		return new FinalInterval( min, max );
	}

	public static < T extends RealType< T > & NativeType< T > >
	void drawPoint(
			RandomAccessibleInterval< T > rai,
			double[] position,
			double radius,
			double calibration )
	{
		Shape shape = new HyperSphereShape( (int) ceil( radius / calibration ) );
		final RandomAccessible< Neighborhood< T > > nra = shape.neighborhoodsRandomAccessible( rai );
		final RandomAccess< Neighborhood< T > > neighborhoodRandomAccess = nra.randomAccess();

		neighborhoodRandomAccess.setPosition( asLongs( position )  );
		final Neighborhood< T > neighborhood = neighborhoodRandomAccess.get();

		final Cursor< T > cursor = neighborhood.cursor();
		while( cursor.hasNext() )
		{
			try
			{
				cursor.next().setReal( 200 );
			}
			catch ( ArrayIndexOutOfBoundsException e )
			{
				Logger.log( "[ERROR] Draw points out of bounds..." );
				break;
			}
		}
	}

	public static < T extends RealType< T > & NativeType< T > >
	RandomAccessibleInterval< T > getEnlargedRai( RandomAccessibleInterval< T > rai )
	{
		long[] min = new long[ 2 ];
		long[] max = new long[ 2 ];
		rai.max( max );
		for ( int d = 0; d < 2; ++d )
		{
			max[ d ] *= 1.2;
		}
		final FinalInterval interval = new FinalInterval( min, max );
		return Views.interval( Views.extendZero( rai ), interval );
	}

	public static < T extends RealType< T > & NativeType< T > >
	RandomAccessibleInterval< T > getEnlargedRai( RandomAccessibleInterval< T > rai, int border )
	{
		int n = rai.numDimensions();

		long[] min = new long[ n ];
		long[] max = new long[ n ];

		rai.min( min );
		rai.max( max );

		for ( int d = 0; d < n; ++d )
		{
			min[ d ] -= border;
			max[ d ] += border;

		}

		final FinalInterval interval = new FinalInterval( min, max );
		return Views.interval( Views.extendZero( rai ), interval );
	}

	public static < T extends RealType< T > & NativeType< T > >
	void setValues( RandomAccessibleInterval< T > rai, double value )
	{
		Cursor< T > cursor = Views.iterable( rai ).localizingCursor();

		while ( cursor.hasNext() )
		{
			cursor.next().setReal( value );
		}
	}

	public static ImgLabeling< Integer, IntType > labelMapAsImgLabeling( RandomAccessibleInterval< IntType > labelMap )
	{
		final ImgLabeling< Integer, IntType > imgLabeling = new ImgLabeling<>( labelMap );

		final double maximumLabel = Algorithms.getMaximumValue( labelMap );

		final ArrayList< Set< Integer > > labelSets = new ArrayList< >();

		labelSets.add( new HashSet<>() ); // empty 0 label
		for ( int label = 1; label <= maximumLabel; ++label )
		{
			final HashSet< Integer > set = new HashSet< >();
			set.add( label );
			labelSets.add( set );
		}

		new LabelingMapping.SerialisationAccess< Integer >( imgLabeling.getMapping() )
		{
			{
				super.setLabelSets( labelSets );
			}
		};

		return imgLabeling;
	}

	public static < T extends RealType< T > > ImgLabeling< Integer, IntType > labelMapAsImgLabelingRobert( RandomAccessibleInterval< T > labelMap )
	{
		final RandomAccessibleInterval< IntType > indexImg = ArrayImgs.ints( Intervals.dimensionsAsLongArray( labelMap ) );
		final ImgLabeling< Integer, IntType > imgLabeling = new ImgLabeling<>( indexImg );

		final Cursor< LabelingType< Integer > > labelCursor = Views.flatIterable( imgLabeling ).cursor();

		for ( final RealType input : Views.flatIterable( labelMap ) ) {

			final LabelingType< Integer > element = labelCursor.next();

			if ( input.getRealFloat() != 0 )
			{
				element.add( (int) input.getRealFloat() );
			}
		}

		return imgLabeling;
	}



	private static Set< Integer > getLabelSet( RandomAccessibleInterval< UnsignedShortType > labelMap )
	{
		final Cursor< UnsignedShortType > cursor = Views.iterable( labelMap ).cursor();

		final Set< Integer > labelSet = new HashSet<>();

		while ( cursor.hasNext() )
		{
			labelSet.add(  cursor.next().getInteger() );
		}

		return labelSet;
	}

	public static void drawObject( RandomAccessibleInterval< IntType > img,
								   LabelRegion labelRegion,
								   int value )
	{
		final Cursor< Void > regionCursor = labelRegion.cursor();
		final RandomAccess< IntType > access = img.randomAccess();
		BitType bitTypeTrue = new BitType( true );
		while ( regionCursor.hasNext() )
		{
			regionCursor.fwd();
			access.setPosition( regionCursor );
			access.get().set( value );
		}
	}

	public static RandomAccessibleInterval<BitType> asMask( ImgLabeling<Integer, IntType> imgLabeling )
	{
		final RandomAccessibleInterval< IntType > labeling = imgLabeling.getSource();

		RandomAccessibleInterval< BitType > mask = asMask( labeling );

		return mask;

	}

	public static < T extends RealType< T > & NativeType< T > >
	ArrayList< RandomAccessibleInterval< BitType > > asMasks( ArrayList< RandomAccessibleInterval< T > > images )
	{
		final ArrayList< RandomAccessibleInterval< BitType > > masks = new ArrayList<>();

		for ( int t = 0; t < images.size(); t++ )
		{
			masks.add( asMask( images.get( t ) ) );
		}

		return masks;
	}


	public static < T extends RealType< T > & NativeType< T > >
	RandomAccessibleInterval<BitType> asMask( RandomAccessibleInterval< T > rai )
	{
		RandomAccessibleInterval< BitType > mask =
				ArrayImgs.bits( Intervals.dimensionsAsLongArray( rai ) );
		mask = Transforms.getWithAdjustedOrigin( rai, mask  );
		final RandomAccess< BitType > maskAccess = mask.randomAccess();

		final Cursor< T > cursor = Views.iterable( rai ).cursor();

		while ( cursor.hasNext() )
		{
			cursor.fwd();

			if ( cursor.get().getRealDouble() > 0 )
			{
				maskAccess.setPosition( cursor );
				maskAccess.get().set( true );
			}
		}

		return mask;
	}

	public static < T extends RealType< T > & NativeType< T > >
	int getNumObjects( RandomAccessibleInterval< T > mask )
	{
		final LabelRegions labelRegions = new LabelRegions( Regions.asImgLabeling( mask, ConnectedComponents.StructuringElement.FOUR_CONNECTED )  );
		return labelRegions.getExistingLabels().size() - 1;
	}

	public static < T extends RealType< T > & NativeType< T > >
	ImagePlus getAsImagePlusMovie(
			ArrayList< RandomAccessibleInterval< T > > rais2D,
			String title )
	{
		RandomAccessibleInterval movie = Views.stack( rais2D );
		movie = Views.addDimension( movie, 0, 0);
		movie = Views.addDimension( movie, 0, 0);
		movie = Views.permute( movie, 2,4 );
		final ImagePlus imp = new Duplicator().run( ImageJFunctions.wrap( movie, title ) );
		imp.setTitle( title );
		return imp;
	}

	public static boolean acceptFile( String fileNameEndsWith, String file )
	{
		final String[] fileNameEndsWithList = fileNameEndsWith.split( "," );

		for ( String endsWith : fileNameEndsWithList )
		{
			if ( file.endsWith( endsWith.trim() ) )
			{
				return true;
			}
		}

		return false;
	}

	public static ImagePlus asLabelImagePlus( RandomAccessibleInterval< IntType > indexImg )
	{
		final Duplicator duplicator = new Duplicator();
		final ImagePlus labelImagePlus = duplicator.run( ImageJFunctions.wrap( indexImg, "mask" ) );
		labelImagePlus.setLut( getGoldenAngleLUT() );
		return labelImagePlus;
	}


	public static LUT getGoldenAngleLUT()
	{
		byte[][] bytes = createGoldenAngleLut( 256 );
		final byte[][] rgb = new byte[ 3 ][ 256 ];

		for ( int c = 0; c < 3; ++c )
		{
			rgb[ c ][ 0 ] = 0; // Black background
		}

		for ( int c = 0; c < 3; ++c )
		{
			for ( int i = 1; i < 256; ++i )
			{
				rgb[ c ][ i ] = bytes[ i ][ c ];
			}
		}

		LUT lut = new LUT( rgb[ 0 ], rgb[ 1 ], rgb[ 2 ] );
		return lut;
	}


	/**
	 * Make lookup table with esthetically pleasing colors based on the golden
	 * angle
	 *
	 * From: MorphoLibJ
	 * // TODO: properly cite!
	 *
	 * @param nColors number of colors to generate
	 * @return lookup table with golden-angled-based colors
	 */
	public final static byte[][] createGoldenAngleLut( int nColors )
	{
		// hue for assigning new color ([0.0-1.0])
		float hue = 0.5f;
		// saturation for assigning new color ([0.5-1.0])
		float saturation = 0.75f;

		// create colors recursively by adding golden angle ratio to hue and
		// saturation of previous color
		Color[] colors = new Color[nColors];
		for (int i = 0; i < nColors; i++)
		{
			// create current color
			colors[i] = Color.getHSBColor(hue, saturation, 1);

			// update hue and saturation for next color
			hue += 0.38197f; // golden angle
			if (hue > 1)
				hue -= 1;
			saturation += 0.38197f; // golden angle
			if (saturation > 1)
				saturation -= 1;
			saturation = 0.5f * saturation + 0.5f;
		}

		// create map
		byte[][] map = new byte[nColors][3];

		// fill up the color map by converting color array
		for (int i = 0; i < nColors; i++)
		{
			Color color = colors[i];
			map[i][0] = (byte) color.getRed();
			map[i][1] = (byte) color.getGreen();
			map[i][2] = (byte) color.getBlue();
		}

		return map;
	}

	public static < T extends AbstractIntegerType< T > >
	Set< Long > computeUniqueValues( RandomAccessibleInterval< T > rai )
	{
		final Set< Long > unique = new HashSet<>(  );

		final Cursor< T > cursor = Views.iterable( rai ).cursor();

		while ( cursor.hasNext() )
		{
			unique.add( cursor.next().getIntegerLong() );
		}

		return unique;
	}

	public static ImagePlus getAsImagePlusMovie( RandomAccessibleInterval rai, String title )
	{
		final ImagePlus wrap = ImageJFunctions.wrap(
				Views.permute(
						Views.addDimension( rai, 0, 0 ),
						2, 3 ), title );
		return wrap;
	}

	public static ImagePlus getAsImagePlusMovie( RandomAccessibleInterval rai, String title, Calibration calibration )
	{
		final ImagePlus wrap = getAsImagePlusMovie( rai, title );
		wrap.setCalibration( calibration );
		return wrap;
	}

	public static < T extends RealType< T > & NativeType< T > >
	ImagePlus labelingsAsImagePlus( ArrayList< RandomAccessibleInterval< T > > labelings )
	{
		ImagePlus segmentationImp = getAsImagePlusMovie( labelings, SEGMENTATION );
		segmentationImp.setLut( getGoldenAngleLUT() );
		segmentationImp.setTitle( SEGMENTATION );
		return segmentationImp;
	}

	public static < T extends RealType< T > & NativeType< T > >
	ArrayList< RandomAccessibleInterval< T > > get2DImagePlusMovieAsFrameList(
			ImagePlus imagePlus,
			long channelOneBased,
			long tMinOneBased,
			long tMaxOneBased )
	{
		if ( imagePlus.getNSlices() != 1 )
		{
			Logger.error( "Only 2D images (one z-slice) are supported.");
			return null;
		}

		final Img< T > wrap = ImageJFunctions.wrap( imagePlus );

		ArrayList<  RandomAccessibleInterval< T > > frames = new ArrayList<>();

		for ( long t = tMinOneBased - 1; t < tMaxOneBased; ++t )
		{
			RandomAccessibleInterval< T > channel = extractChannel( imagePlus, channelOneBased, wrap );

			RandomAccessibleInterval< T > timepoint = extractTimePoint( imagePlus, t, channel );

			frames.add( copyAsArrayImg( timepoint ) );
		}

		return frames;
	}

	public static < T extends RealType< T > & NativeType< T > >
	ArrayList< RandomAccessibleInterval< T > >
	get2DImagePlusMovieAsFrameList(
			ImagePlus imagePlus,
			long channelOneBased )
	{
		return get2DImagePlusMovieAsFrameList(
				imagePlus,
				channelOneBased,
				1,
				imagePlus.getNFrames() );
	}

	public static < T extends RealType< T > & NativeType< T > > RandomAccessibleInterval< T >
	extractTimePoint( ImagePlus imagePlus, long t, RandomAccessibleInterval< T > channel )
	{
		RandomAccessibleInterval< T > timepoint;

		if ( imagePlus.getNFrames() != 1 )
		{
			timepoint = Views.hyperSlice( channel, 2, t );
		}
		else
		{
			timepoint = channel;
		}

		return timepoint;
	}

	public static < T extends RealType< T > & NativeType< T > > RandomAccessibleInterval< T > extractChannel( ImagePlus imagePlus, long channelOneBased, Img< T > wrap )
	{
		RandomAccessibleInterval< T > channel;

		if ( imagePlus.getNChannels() != 1 )
		{
			channel = Views.hyperSlice( wrap, 2, channelOneBased - 1);
		}
		else
		{
			channel = wrap;
		}
		return channel;
	}

	public static void wait( int millis )
	{
		try
		{
			Thread.sleep( millis );
		} catch ( InterruptedException e )
		{
			e.printStackTrace();
		}
	}

	public static < T extends RealType< T > & NativeType< T > >
	void drawMaskIntoImage(
			RandomAccessibleInterval< T > mask,
			RandomAccessibleInterval< T > image,
			double value )
	{

		final Cursor< T > cursor = Views.iterable( mask ).cursor();
		final RandomAccess< T > access = image.randomAccess();

		while ( cursor.hasNext() )
		{
			if ( cursor.next().getRealDouble() > 0.0 )
			{
				access.setPosition( cursor );
				access.get().setReal( value );
			}
		}
	}

	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > createEmptyCopy( RandomAccessibleInterval< R > image )
	{
		RandomAccessibleInterval< R > copy =
				new ArrayImgFactory( Util.getTypeFromInterval( image ) ).create( image );
		copy = Views.translate( copy, Intervals.minAsLongArray( image ) );
		return copy;
	}

	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< BitType > createEmptyMask( RandomAccessibleInterval< R > image )
	{
		RandomAccessibleInterval< BitType > mask =
				new ArrayImgFactory( new BitType(  ) ).create( image );
		mask = Views.translate( mask, Intervals.minAsLongArray( image ) );
		return mask;
	}

	public static double[] copy( double[] values )
	{
		final double[] copy = new double[ values.length ];
		for ( int i = 0; i < values.length; i++ ) copy[ i ] = values[ i ];
		return copy;
	}

	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > get3DRaiAs5DRaiWithImagePlusDimensionOrder( RandomAccessibleInterval< R > registeredImage )
	{
		// make 5D XYCZT
		registeredImage =
				Views.permute(
						Views.addDimension(
								Views.addDimension( registeredImage, 0 , 0 ),
								0 , 0 ),
						2 ,3 );
		return registeredImage;
	}

	public static ImagePlus openWithBioFormats( String path )
	{
		try
		{
			ImporterOptions opts = new ImporterOptions();
			opts.setId( path );
			opts.setVirtual( true );
			ImportProcess process = new ImportProcess( opts );
			process.execute();
			ImagePlusReader impReader = new ImagePlusReader( process );
			ImagePlus[] imps = impReader.openImagePlus();
			return imps[ 0 ];
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			return null;
		}
	}

	public static < T extends RealType< T > & NativeType< T > >
	void saveLabelings(
			ArrayList< RandomAccessibleInterval< T > > labelings,
			Calibration calibration,
			String outputLabelingsPath )
	{
		final ImagePlus labelsImp = labelingsAsImagePlus( labelings );
		labelsImp.setCalibration( calibration );

		new FileSaver( labelsImp ).saveAsTiff( outputLabelingsPath );

		Logger.log( "Label images saved: " + outputLabelingsPath );
	}

	public static < T extends RealType< T > & NativeType< T > >
	RandomAccessibleInterval< T > getChannelImages( ImagePlus imagePlus )
	{
		RandomAccessibleInterval< T > images = ImageJFunctions.wrap( imagePlus );

		int numChannels = imagePlus.getNChannels();

		if ( numChannels == 1 )
			images = Views.addDimension( images, 0 ,0 );
		else
			images = Views.permute( images, imagePlusChannelDimension, 3 );

		return images;
	}

	public static < T extends RealType< T > & NativeType< T > >
	RandomAccessibleInterval< T > getChannelImage( RandomAccessibleInterval< T > images, int channel )
	{
		RandomAccessibleInterval< T > rai = Views.hyperSlice( images, 3, channel );
		return rai;
	}

	public static < T extends RealType< T > & NativeType< T > >
	void saveRAIListAsMovie(
			ArrayList< RandomAccessibleInterval< T > > rais,
			Calibration calibration,
			String outputPath, String title )
	{

		final ImagePlus imp = getAsImagePlusMovie( rais, title );
		imp.setCalibration( calibration );
		new FileSaver( imp ).saveAsTiff( outputPath );
		Logger.log( "Movie saved: " + outputPath );
	}

	public static < R extends RealType< R > & NativeType< R > >
	double measureCoefficientOfVariation(
			RandomAccessibleInterval< R > intensities,
			RandomAccessibleInterval< BitType > mask,
			Double meanOffset )
	{
		final ArrayList< Double > intensitiesWithMask =
				getValuesWithinMaskAsList( intensities, mask );

		final double mean = Utils.mean( intensitiesWithMask );
		final double sdev = Utils.sdev( intensitiesWithMask, mean );

		double cov = sdev / ( mean - meanOffset );

		return cov;
	}

	public static < R extends RealType< R > & NativeType< R > >
	double measureSum(
			RandomAccessibleInterval< R > intensities,
			RandomAccessibleInterval< BitType > mask,
			Double meanOffset )
	{
		final ArrayList< Double > doubles =
				getValuesWithinMaskAsList( intensities, mask );

		final double sum = Utils.sum( doubles );
		final double sumOffset = meanOffset * doubles.size();
		final double offsetCorrectedSum = sum - sumOffset;

		return offsetCorrectedSum;
	}


}
