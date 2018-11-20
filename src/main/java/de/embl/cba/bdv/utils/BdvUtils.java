package de.embl.cba.bdv.utils;

import bdv.tools.transformation.TransformedSource;
import bdv.util.*;
import bdv.viewer.*;
import bdv.viewer.state.SourceState;
import de.embl.cba.bdv.utils.labels.luts.LabelsSource;
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.*;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.embl.cba.bdv.utils.transforms.Transforms.createBoundingIntervalAfterTransformation;

public abstract class BdvUtils
{

	public static final String OVERLAY = "overlay";


	public static FinalInterval getCurrentlyVisibleInterval( Bdv bdv, int sourceId )
	{
		final AffineTransform3D sourceTransform = getSourceTransform( bdv, sourceId );
		final RandomAccessibleInterval< ? > rai = getRandomAccessibleInterval( bdv, sourceId );
		return createBoundingIntervalAfterTransformation( rai, sourceTransform );
	}

	public static void zoomToSource( Bdv bdv, String sourceName )
	{
		zoomToSource( bdv, getSourceIndex( bdv, sourceName ) );
	}

	public static void zoomToSource( Bdv bdv, int sourceId )
	{
		final FinalInterval interval = getInterval( bdv, sourceId );

		zoomToInterval( bdv, interval, 1.0 );
	}

	public static FinalInterval getInterval( Bdv bdv, int sourceId )
	{
		final AffineTransform3D sourceTransform = getSourceTransform( bdv, sourceId );
		final RandomAccessibleInterval< ? > rai = getRandomAccessibleInterval( bdv, sourceId );
		return createBoundingIntervalAfterTransformation( rai, sourceTransform );
	}

	public static FinalRealInterval getCurrentViewerInterval( Bdv bdv )
	{
		AffineTransform3D viewerTransform = new AffineTransform3D();
		bdv.getBdvHandle().getViewerPanel().getState().getViewerTransform( viewerTransform );
		viewerTransform = viewerTransform.inverse();
		final long[] min = new long[ 3 ];
		final long[] max = new long[ 3 ];
		max[ 0 ] = bdv.getBdvHandle().getViewerPanel().getWidth();
		max[ 1 ] = bdv.getBdvHandle().getViewerPanel().getHeight();
		return viewerTransform.estimateBounds( new FinalInterval( min, max ) );
	}

	public static int getSourceIndex( Bdv bdv, Source< ? > source )
	{
		final List< SourceState< ? > > sources = bdv.getBdvHandle().getViewerPanel().getState().getSources();

		for ( int i = 0; i < sources.size(); ++i )
		{
			if ( sources.get( i ).getSpimSource().equals( source ) )
			{
				return i;
			}
		}

		return -1;
	}

	public static String getName( Bdv bdv, int sourceId )
	{
		return bdv.getBdvHandle().getViewerPanel().getState().getSources().get( sourceId ).getSpimSource().getName();
	}

	public static ArrayList< String > getSourceNames( Bdv bdv )
	{
		final ArrayList< String > sourceNames = new ArrayList<>();

		final List< SourceState< ? > > sources = bdv.getBdvHandle().getViewerPanel().getState().getSources();

		for ( SourceState source : sources )
		{
			sourceNames.add( source.getSpimSource().getName() );
		}

		return sourceNames;
	}


	public static int getSourceIndex( Bdv bdv, String sourceName )
	{
		return getSourceNames( bdv ).indexOf( sourceName );
	}

	public static VoxelDimensions getVoxelDimensions( Bdv bdv, int sourceId )
	{
		return bdv.getBdvHandle().getViewerPanel().getState().getSources().get( sourceId ).getSpimSource().getVoxelDimensions();
	}


	public static AffineTransform3D getSourceTransform( Bdv bdv, int sourceId )
	{
		final AffineTransform3D sourceTransform = new AffineTransform3D();
		bdv.getBdvHandle().getViewerPanel().getState().getSources().get( sourceId ).getSpimSource().getSourceTransform( 0, 0, sourceTransform );
		return sourceTransform;
	}


	public static AffineTransform3D getSourceTransform( Source source, int t, int level )
	{
		AffineTransform3D sourceTransform = new AffineTransform3D();
		source.getSourceTransform( t, level, sourceTransform );
		return sourceTransform;
	}


	public static RandomAccessibleInterval< ? > getRandomAccessibleInterval( Bdv bdv, int sourceId )
	{
		return bdv.getBdvHandle().getViewerPanel().getState().getSources().get( sourceId ).getSpimSource().getSource( 0, 0 );
	}

	public static RealRandomAccessible< ? > getRealRandomAccessible( Bdv bdv, int sourceId )
	{
		return bdv.getBdvHandle().getViewerPanel().getState().getSources().get( sourceId ).getSpimSource().getInterpolatedSource( 0, 0, Interpolation.NLINEAR );
	}

	public static void zoomToInterval( Bdv bdv, FinalInterval interval, double zoomFactor )
	{
		final AffineTransform3D affineTransform3D = getImageZoomTransform( bdv, interval, zoomFactor );

		bdv.getBdvHandle().getViewerPanel().setCurrentViewerTransform( affineTransform3D );
	}

	public static AffineTransform3D getImageZoomTransform( Bdv bdv, FinalInterval interval, double zoomFactor )
	{

		final AffineTransform3D affineTransform3D = new AffineTransform3D();

		double[] shiftToImage = new double[ 3 ];

		for ( int d = 0; d < 3; ++d )
		{
			shiftToImage[ d ] = -( interval.min( d ) + interval.dimension( d ) / 2.0 );
		}

		affineTransform3D.translate( shiftToImage );

		int[] bdvWindowDimensions = new int[ 2 ];
		bdvWindowDimensions[ 0 ] = bdv.getBdvHandle().getViewerPanel().getWidth();
		bdvWindowDimensions[ 1 ] = bdv.getBdvHandle().getViewerPanel().getHeight();

		affineTransform3D.scale( zoomFactor * bdvWindowDimensions[ 0 ] / interval.dimension( 0 ) );

		double[] shiftToBdvWindowCenter = new double[ 3 ];

		for ( int d = 0; d < 2; ++d )
		{
			shiftToBdvWindowCenter[ d ] += bdvWindowDimensions[ d ] / 2.0;
		}

		affineTransform3D.translate( shiftToBdvWindowCenter );

		return affineTransform3D;
	}


	public static RandomAccessibleInterval< IntegerType > getIndexImg( BdvStackSource bdvStackSource, int t, int level )
	{
		final Source source = getSource( bdvStackSource, 0 );

		return getIndexImg( source, t, level );

	}

	public static Source getSource( BdvStackSource bdvStackSource, int i )
	{
		final SourceAndConverter sourceAndConverter = ( SourceAndConverter ) bdvStackSource.getSources().get( 0 );

		return sourceAndConverter.getSpimSource();
	}

	public static RandomAccessibleInterval< IntegerType > getIndexImg( Source source, int t, int level )
	{
		if ( source instanceof TransformedSource )
		{
			final Source wrappedSource = ( ( TransformedSource ) source ).getWrappedSource();

			if ( wrappedSource instanceof LabelsSource )
			{
				return ( ( LabelsSource ) wrappedSource ).getIndexImg( t, 0 );
			}
		}

		return null; // TODO: throw some type error...
	}

	public static ARGBType asArgbType( Color color )
	{
		return new ARGBType( ARGBType.rgba( color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() ) );
	}

	public static long[] getPositionInSourceStack( Source source, RealPoint mousePositionInMicrometerUnits, int t, int level )
	{
		final AffineTransform3D sourceTransform = BdvUtils.getSourceTransform( source, t, level );

		final RealPoint positionInPixelUnits = new RealPoint( 3 );

		sourceTransform.inverse().apply( mousePositionInMicrometerUnits, positionInPixelUnits );

		final long[] longPosition = new long[ 3 ];

		for ( int d = 0; d < 3; ++d )
		{
			longPosition[ d ] = (long) positionInPixelUnits.getFloatPosition( d );
		}

		return longPosition;
	}


	public static < T extends RealType< T > & NativeType< T > > void showAsIJ1MultiColorImage( Bdv bdv, double resolution, ArrayList< RandomAccessibleInterval< T > > randomAccessibleIntervals )
	{
		final ImagePlus imp = ImageJFunctions.wrap( Views.stack( randomAccessibleIntervals ), "capture" );
		final ImagePlus dup = new Duplicator().run( imp ); // otherwise it is virtual and cannot be modified
		IJ.run( dup, "Subtract...", "value=32768 slice");
		VoxelDimensions voxelDimensions = getVoxelDimensions( bdv, 0 );
		IJ.run( dup, "Properties...", "channels="+randomAccessibleIntervals.size()+" slices=1 frames=1 unit="+voxelDimensions.unit()+" pixel_width="+resolution+" pixel_height="+resolution+" voxel_depth=1.0");
		final CompositeImage compositeImage = new CompositeImage( dup );
		for ( int channel = 1; channel <= compositeImage.getNChannels(); ++channel )
		{
			compositeImage.setC( channel );
			switch ( channel )
			{
				case 1: // tomogram
					compositeImage.setChannelLut( compositeImage.createLutFromColor( Color.GRAY ) );
					compositeImage.setDisplayRange( 0, 1000 ); // TODO: get from bdv
					break;
				case 2: compositeImage.setChannelLut( compositeImage.createLutFromColor( Color.GRAY ) ); break;
				case 3: compositeImage.setChannelLut( compositeImage.createLutFromColor( Color.RED ) ); break;
				case 4: compositeImage.setChannelLut( compositeImage.createLutFromColor( Color.GREEN ) ); break;
				default: compositeImage.setChannelLut( compositeImage.createLutFromColor( Color.BLUE ) ); break;
			}
		}
		compositeImage.show();
		compositeImage.setTitle( "capture" );
		IJ.run(compositeImage, "Make Composite", "");
	}

	public static ArrayList< Color > getColors( List< Integer > nonOverlaySources )
	{
		ArrayList< Color > defaultColors = new ArrayList<>(  );
		if ( nonOverlaySources.size() > 1 )
		{
			defaultColors.add( Color.BLUE );
			defaultColors.add( Color.GREEN );
			defaultColors.add( Color.RED );
			defaultColors.add( Color.MAGENTA );
			defaultColors.add( Color.GRAY );
			defaultColors.add( Color.GRAY );
			defaultColors.add( Color.GRAY );
			defaultColors.add( Color.GRAY );
			defaultColors.add( Color.GRAY );
		}
		else
		{
			defaultColors.add( Color.GRAY );
		}
		return defaultColors;
	}

	public static List< Integer > getNonOverlaySourceIndices( Bdv bdv, List< SourceState< ? > > sources )
	{
		final List< Integer > nonOverlaySources = new ArrayList<>(  );

		for ( int sourceIndex = 0; sourceIndex < sources.size(); ++sourceIndex )
		{
			String name = getName( bdv, sourceIndex );
			if ( ! name.contains( OVERLAY ) )
			{
				nonOverlaySources.add( sourceIndex );
			}
		}

		return nonOverlaySources;
	}

	public static boolean isLabelsSource( BdvStackSource bdvStackSource )
	{

		final Source source = getSource( bdvStackSource, 0 );

		return isLabelsSource( source );

	}

	private static boolean isLabelsSource( Source source )
	{
		if ( source instanceof TransformedSource )
		{
			final Source wrappedSource = ( ( TransformedSource ) source ).getWrappedSource();

			if ( wrappedSource instanceof LabelsSource )
			{
				return true;
			}
		}

		return false;
	}

	public static LabelsSource getLabelsSource( BdvStackSource bdvStackSource )
	{
		final Source source = getSource( bdvStackSource, 0 );

		return getLabelsSource( source );
	}

	private static LabelsSource getLabelsSource( Source source )
	{
		if ( source instanceof TransformedSource )
		{
			final Source wrappedSource = ( ( TransformedSource ) source ).getWrappedSource();

			if ( wrappedSource instanceof LabelsSource )
			{
				return  ( ( LabelsSource ) wrappedSource) ;
			}
		}

		return null;
	}



	public static RealPoint getGlobalMouseCoordinates( Bdv bdv )
	{
		final RealPoint posInBdvInMicrometer = new RealPoint( 3 );
		bdv.getBdvHandle().getViewerPanel().getGlobalMouseCoordinates( posInBdvInMicrometer );
		return posInBdvInMicrometer;
	}


	public static void deselectAllObjectsInActiveLabelSources( Bdv bdv )
	{
		final HashMap< Integer, Long > sourcesAndSelectedObjects = new HashMap<>();

		final List< Integer > visibleSourceIndices = bdv.getBdvHandle().getViewerPanel().getState().getVisibleSourceIndices();

		for ( int sourceIndex : visibleSourceIndices )
		{

			final SourceState< ? > sourceState = bdv.getBdvHandle().getViewerPanel().getState().getSources().get( sourceIndex );

			final Source source = sourceState.getSpimSource();

			if ( isLabelsSource( source ) )
			{

				BdvUtils.getLabelsSource( source ).selectNone( );
			}
		}

		bdv.getBdvHandle().getViewerPanel().requestRepaint();

	}

	public static Map< Integer, Long > selectObjectsInActiveLabelSources( Bdv bdv, RealPoint point )
	{

		final HashMap< Integer, Long > sourcesAndSelectedObjects = new HashMap<>();

		final List< Integer > visibleSourceIndices = bdv.getBdvHandle().getViewerPanel().getState().getVisibleSourceIndices();

		for ( int sourceIndex : visibleSourceIndices )
		{

			final SourceState< ? > sourceState = bdv.getBdvHandle().getViewerPanel().getState().getSources().get( sourceIndex );

			final Source source = sourceState.getSpimSource();

			if ( isLabelsSource( source ) )
			{

				final RandomAccessibleInterval< IntegerType > indexImg = BdvUtils.getIndexImg( source, 0, 0 );

				final long[] positionInSourceStack = BdvUtils.getPositionInSourceStack( source, point, 0, 0 );

				final RandomAccess< IntegerType > access = indexImg.randomAccess();

				try
				{
					access.setPosition( positionInSourceStack );
					final long objectIndex = access.get().getIntegerLong();

					if ( objectIndex != 0 )
					{
						sourcesAndSelectedObjects.put( sourceIndex, objectIndex );
					}

					BdvUtils.getLabelsSource( source ).select( objectIndex );
				}
				catch ( Exception e )
				{
					int a = 1;
					// selected pixel outside image bounds
				}



			}
		}

		bdv.getBdvHandle().getViewerPanel().requestRepaint();

		return sourcesAndSelectedObjects;
	}

}
