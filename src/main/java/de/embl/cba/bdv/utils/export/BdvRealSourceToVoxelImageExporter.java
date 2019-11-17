package de.embl.cba.bdv.utils.export;

import bdv.export.ProgressWriter;
import bdv.util.BdvHandle;
import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import bdv.viewer.state.SourceState;
import de.embl.cba.bdv.utils.RAIUtils;
import ij.ImagePlus;
import ij.io.FileSaver;
import net.imglib2.*;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.*;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.RandomAccessibleOnRealRandomAccessible;
import net.imglib2.view.Views;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BdvRealExporter < T extends RealType< T > & NativeType< T > >
{
	private final List< SourceState< ? > > sacs;
	private final List< Integer > sourceIndices;
	private final RealInterval interval;
	private final int tMin;
	private final int tMax;
	private final Interpolation interpolation;
	private final double[] outputVoxelSpacings;
	private final ExportModality exportModality;
	private final ProgressWriter progress;
	private AffineTransform3D resolutionTransform;
	protected AffineTransform3D pixelRenderToPhysical;
	private String outputUnit;
	private FinalInterval outputPixelInterval;
	private int numThreads;
	private String outputDirectory;

	public enum ExportModality
	{
		ShowAsImagePlus,
		SaveAsTiffStacks
	}

	public BdvRealExporter(
			BdvHandle bdv,
			List< Integer > sourceIndices,
			final RealInterval interval,
			final int tMin,
			final int tMax,
			final Interpolation interpolation,
			final double[] outputVoxelSpacings,
			final ExportModality exportModality,
			int numThreads,
			final ProgressWriter progress )
	{
		this.sacs = bdv.getViewerPanel().getState().getSources();
		this.sourceIndices = sourceIndices;
		this.interval = interval;
		this.tMin = tMin;
		this.tMax = tMax;
		this.interpolation = interpolation;
		this.outputVoxelSpacings = outputVoxelSpacings;
		this.exportModality = exportModality;
		this.numThreads = numThreads;
//		this.baseType = baseType;
		this.progress = progress;
	}

	public void export()
	{
		setOutputPixelInterval();
		setRenderResolutionTransform();
		setOutputVoxelUnit();

		for ( int i : sourceIndices )
		{
			final Source< ? > source = sacs.get( i ).getSpimSource();
			final ArrayList< RandomAccessibleInterval< T > > rais = new ArrayList<>();

			for ( int t = tMin; t <= tMax; ++t )
			{
				final RandomAccessibleInterval< T > rai = tryGetRAI( source, t );
				final String name = source.getName() + "--T" + String.format( "%1$05d", t );;

				switch ( exportModality )
				{
					case ShowAsImagePlus:
						rais.add( rai );
						break;
					case SaveAsTiffStacks:
						final ImagePlus imagePlusStack = asImagePlus( rai, name, false );
						FileSaver fileSaver = new FileSaver( imagePlusStack );
						fileSaver.saveAsTiff( outputDirectory + File.separator + name + ".tif" );
						break;
					default:
						break;
				}
			}

			switch ( exportModality )
			{
				case ShowAsImagePlus:
					final ImagePlus imagePlusMovie = asImagePlus( rais, source.getName() );
					imagePlusMovie.show();
					break;
				default:
					break;
			}
		}
	}

	public ImagePlus asImagePlus( ArrayList< RandomAccessibleInterval< T > > raiXYZTimePoints, String name )
	{
		final RandomAccessibleInterval< T > raiXYZT = Views.stack( raiXYZTimePoints );
		final RandomAccessibleInterval< T > raiXYZTC = Views.addDimension( raiXYZT, 0, 0 );
		final RandomAccessibleInterval< T > raiXYZCT = Views.permute( raiXYZTC, 3, 4 );
		final RandomAccessibleInterval< T > raiXYCZT = Views.permute( raiXYZCT, 2, 3 );
		return asImagePlus( raiXYCZT, name, true );
	}

	public void setOutputDirectory( String outputDirectory )
	{
		this.outputDirectory = outputDirectory;
	}

	private RandomAccessibleInterval< T > tryGetRAI( Source< ? > source, int t )
	{
		try
		{
			return getCroppedAndRasteredRAI( source, t ) ;
		} catch ( Exception e ){
			e.printStackTrace();
		}
		return null;
	}

	private RandomAccessibleInterval< T > getCroppedAndRasteredRAI( Source< ? > source, int t )
	{
		final RealRandomAccessible< T > realRA = ( RealRandomAccessible< T > ) source.getInterpolatedSource( t, 0, interpolation );

		final AffineTransform3D sourceTransform = new AffineTransform3D();
		source.getSourceTransform( t, 0, sourceTransform );

		final RealRandomAccessible< T > physicalRRA = RealViews.transform( realRA, sourceTransform );

		final RealRandomAccessible< T > outputVoxelSizeRRA = RealViews.affine( physicalRRA, resolutionTransform.inverse() );

		final RandomAccessibleOnRealRandomAccessible< T > voxelRA = Views.raster( outputVoxelSizeRRA );

		return Views.interval( voxelRA, outputPixelInterval );
	}

	private ImagePlus asImagePlus(
			RandomAccessibleInterval< T > voxelRAI,
			String name,
			boolean virtual )
	{
		final IntervalView< T > ts = Views.addDimension( voxelRAI, 0, 0 );

		final IntervalView< T > permute = Views.permute( ts, 2, 3 );

		RandomAccessibleInterval< T > zeroMin = Views.zeroMin( permute );

		if ( ! virtual )
			zeroMin = RAIUtils.copyVolumeRAI( zeroMin, numThreads );

		final ImagePlus imagePlus = ImageJFunctions.wrap( zeroMin, name );
		imagePlus.getCalibration().setUnit( outputUnit );
		imagePlus.getCalibration().pixelWidth = outputVoxelSpacings[ 0 ];
		imagePlus.getCalibration().pixelHeight = outputVoxelSpacings[ 1 ];
		imagePlus.getCalibration().pixelDepth = outputVoxelSpacings[ 2 ];

		return imagePlus;
	}

	private void setOutputVoxelUnit()
	{
		outputUnit = sacs.get( 0 ).getSpimSource().getVoxelDimensions().unit();
	}

	private void setOutputPixelInterval( )
	{
//		double[] inputres = new double[ 3 ];
//		VoxelDimensions voxdims = source.getVoxelDimensions();
//		voxdims.dimensions( inputres );
//
		final int numDimensions = 3;

		long[] min = new long[ numDimensions ];
		long[] max = new long[ numDimensions ];
		for( int d = 0; d < numDimensions; d++ )
		{
			min[ d ] = (long) Math.ceil( interval.realMin( d ) / outputVoxelSpacings[ d ] );
			max[ d ] = (long) Math.ceil( interval.realMax( d ) / outputVoxelSpacings[ d ] );
		}

		outputPixelInterval = new FinalInterval( min, max );
	}

	private void setRenderResolutionTransform( )
	{
		resolutionTransform = new AffineTransform3D();
		for( int i = 0; i < outputVoxelSpacings.length; i++ )
			resolutionTransform.set( outputVoxelSpacings[ i ], i, i );
	}
}
