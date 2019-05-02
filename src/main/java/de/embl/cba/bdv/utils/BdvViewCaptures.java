package de.embl.cba.bdv.utils;

import bdv.cache.CacheControl;
import bdv.util.Bdv;
import bdv.util.BdvHandle;
import bdv.util.Prefs;
import bdv.viewer.Source;
import bdv.viewer.ViewerPanel;
import bdv.viewer.overlay.ScaleBarOverlayRenderer;
import bdv.viewer.render.MultiResolutionRenderer;
import bdv.viewer.state.ViewerState;

import de.embl.cba.transforms.utils.Transforms;
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import net.imglib2.*;
import net.imglib2.Point;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.realtransform.Scale;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.ui.PainterThread;
import net.imglib2.ui.RenderTarget;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static de.embl.cba.bdv.utils.BdvUtils.*;

public abstract class BdvViewCaptures
{

	@Deprecated
	public static < R extends RealType< R > & NativeType< R > >
	void captureViewOld( Bdv bdv, double resolution )
	{

		double[] scalingFactors = new double[ 3 ];
		scalingFactors[ 0 ] = 1 / resolution;
		scalingFactors[ 1 ] = 1 / resolution;
		scalingFactors[ 2 ] = 1.0;

		int n = bdv.getBdvHandle().getViewerPanel().getState().getSources().size();

		final ArrayList< RandomAccessibleInterval< R > > randomAccessibleIntervals
				= new ArrayList<>();

		for ( int sourceIndex = 0; sourceIndex < n; ++sourceIndex )
		{
			final Interval interval = getSourceGlobalBoundingInterval( bdv, sourceIndex );

			final Interval viewerInterval =
					Intervals.largestContainedInterval(
						getViewerGlobalBoundingInterval( bdv ) );

			final boolean intersects = ! Intervals.isEmpty(
					Intervals.intersect( interval, viewerInterval ) );

			if ( intersects )
			{
				RealRandomAccessible< R > rra =
						( RealRandomAccessible ) getRealRandomAccessible( bdv, sourceIndex );

				final AffineTransform3D sourceTransform =
						getSourceTransform( bdv, sourceIndex );

				rra = RealViews.transform( rra, sourceTransform );

				Scale scale = new Scale( scalingFactors );
				RealRandomAccessible< R > rescaledRRA = RealViews.transform( rra, scale );
				final RandomAccessible< R > rastered = Views.raster( rescaledRRA );

				final FinalInterval scaledInterval =
						Transforms.scaleIntervalInXY(
								viewerInterval, scale );

				final RandomAccessibleInterval< R > cropped =
						Views.interval( rastered, scaledInterval );

				randomAccessibleIntervals.add( Views.dropSingletonDimensions( cropped )  );
			}

		}

		// showAsCompositeImage( bdv, resolution, randomAccessibleIntervals );

	}

	/**
	 * TODO:
	 * - make it optional to capture an interpolated view
	 *
	 *
	 *
	 * @param bdv
	 * @param resolution
	 * @param voxelUnits
	 *
	 */
	public static void captureView( BdvHandle bdv, double resolution, String voxelUnits )
	{

		// TODO:
		double[] scalingFactors = new double[ 3 ];
		scalingFactors[ 0 ] = 1 / resolution;
		scalingFactors[ 1 ] = 1 / resolution;
		scalingFactors[ 2 ] = 1.0;

		final AffineTransform3D viewerTransform = new AffineTransform3D();
		bdv.getViewerPanel().getState().getViewerTransform( viewerTransform );

		final double[] viewerVoxelSpacing = getViewerVoxelSpacing( bdv );

		final int w = getBdvWindowWidth( bdv );
		final int h = getBdvWindowHeight( bdv );

		final ArrayList< RandomAccessibleInterval< UnsignedShortType > > rais
				= new ArrayList<>();

		final List< Integer > sourceIndices = getVisibleSourceIndices( bdv );

		for ( int sourceIndex : sourceIndices )
		{
			if ( BdvUtils.isSourceIntersectingCurrentView( bdv, sourceIndex ) )
			{

				final RandomAccessibleInterval< UnsignedShortType > rai
						= ArrayImgs.unsignedShorts( w, h );

				final Source< ? > source = getSource( bdv, sourceIndex );
				final RandomAccess< ? extends RealType< ? > > sourceAccess =
						getRealTypeNonVolatileRandomAccess( source, 0, 0 );
				final AffineTransform3D sourceTransform =
						BdvUtils.getSourceTransform( source, 0,0  );
				final RandomAccessibleInterval< ? > sourceRai = source.getSource( 0, 0 );

				AffineTransform3D viewerToSourceTransform = new AffineTransform3D();

				viewerToSourceTransform.preConcatenate( viewerTransform.inverse() );
				viewerToSourceTransform.preConcatenate( sourceTransform.inverse() );

				final RandomAccess< UnsignedShortType > access = rai.randomAccess();

				final double[] canvasPosition = new double[ 3 ];
				final double[] globalPosition = new double[ 3 ];
				final double[] sourceRealPosition = new double[ 3 ];
				final long[] sourcePosition = new long[ 3 ];

				for ( double x = 0; x < w; x++ )
					for ( double y = 0; y < h; y++ )
					{
						canvasPosition[ 0 ] = x;
						canvasPosition[ 1 ] = y;

						viewerToSourceTransform.apply( canvasPosition, sourceRealPosition );

						// TODO: make work with real interpolated access
						for ( int d = 0; d < 3; d++ )
							sourcePosition[ d ] = (long) sourceRealPosition[ d ];

						if ( Intervals.contains( sourceRai, new Point( sourcePosition ) ) )
						{
							sourceAccess.setPosition( sourcePosition );
							Double pixelValue = sourceAccess.get().getRealDouble();
							access.setPosition( ( int ) x, 0 );
							access.setPosition( ( int ) y, 1 );
							access.get().setReal( pixelValue );
						}
					}

				rais.add( rai );
			}
		}

		showAsCompositeImage( viewerVoxelSpacing, voxelUnits, rais );
	}

	public static
	void showAsCompositeImage(
			double[] voxelSpacing,
			String voxelUnit,
			ArrayList< RandomAccessibleInterval< UnsignedShortType > > rais )
	{
		final RandomAccessibleInterval< UnsignedShortType > stack = Views.stack( rais );

		final ImagePlus imp = ImageJFunctions.wrap( stack, "Bdv View Capture" );

		// duplicate: otherwise it is virtual and cannot be modified
		final ImagePlus dup = new Duplicator().run( imp );

		IJ.run( dup,
				"Properties...",
				"channels="+rais.size()
						+" slices=1 frames=1 unit="+voxelUnit
						+" pixel_width=" + voxelSpacing[ 0 ]
						+" pixel_height=" + voxelSpacing[ 1 ]
						+" voxel_depth=" + voxelSpacing[ 2 ] );

		final CompositeImage compositeImage = new CompositeImage( dup );
		for ( int channel = 1; channel <= compositeImage.getNChannels(); ++channel )
		{
			compositeImage.setC( channel );
			switch ( channel )
			{
				// TODO: get from bdv
				case 1:
					compositeImage.setChannelLut(
							compositeImage.createLutFromColor( Color.GRAY ) );
					compositeImage.setDisplayRange( 0, 1000 ); break;
				case 2: compositeImage.setChannelLut(
						compositeImage.createLutFromColor( Color.GREEN ) ); break;
				case 3: compositeImage.setChannelLut(
						compositeImage.createLutFromColor( Color.MAGENTA ) ); break;
				case 4: compositeImage.setChannelLut(
						compositeImage.createLutFromColor( Color.CYAN ) ); break;
				default: compositeImage.setChannelLut(
						compositeImage.createLutFromColor( Color.ORANGE ) ); break;
			}
		}
		compositeImage.show();
		compositeImage.setTitle( "capture" );
		IJ.run(compositeImage, "Make Composite", "");
	}
}
