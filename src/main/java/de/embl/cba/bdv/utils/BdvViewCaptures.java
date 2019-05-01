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
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.*;
import net.imglib2.Point;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.array.ArrayRandomAccess;
import net.imglib2.img.basictypeaccess.array.ShortArray;
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

	public static < R extends RealType< R > & NativeType< R > >
	void captureView( Bdv bdv, double resolution )
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

		showAsIJ1MultiColorImage( bdv, resolution, randomAccessibleIntervals );

	}

	/**
	 * TODO:
	 * - make it optional to capture an interpolated view
	 *
	 *
	 *
	 * @param bdv
	 * @param resolution
	 * @param <R>
	 */
	public static < R extends RealType< R > & NativeType< R > >
	void captureView2( BdvHandle bdv, double resolution )
	{

		double[] scalingFactors = new double[ 3 ];
		scalingFactors[ 0 ] = 1 / resolution;
		scalingFactors[ 1 ] = 1 / resolution;
		scalingFactors[ 2 ] = 1.0;

		final AffineTransform3D viewerTransform = new AffineTransform3D();
		bdv.getViewerPanel().getState().getViewerTransform( viewerTransform );

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

		ImageJFunctions.show( rais.get(0), "" );
		//showAsIJ1MultiColorImage( bdv, 1.0, rais );
	}

	public static BufferedImage captureView( Bdv bdv, int size )
	{
		int width = size;
		int height = size;

		final ViewerPanel viewer = bdv.getBdvHandle().getViewerPanel();
		final ViewerState renderState = viewer.getState();
		final int canvasW = viewer.getDisplay().getWidth();
		final int canvasH = viewer.getDisplay().getHeight();

		final AffineTransform3D affine = new AffineTransform3D();
		renderState.getViewerTransform( affine );
		affine.set( affine.get( 0, 3 ) - canvasW / 2, 0, 3 );
		affine.set( affine.get( 1, 3 ) - canvasH / 2, 1, 3 );
		affine.scale( ( double ) width / canvasW );
		affine.set( affine.get( 0, 3 ) + width / 2, 0, 3 );
		affine.set( affine.get( 1, 3 ) + height / 2, 1, 3 );
		renderState.setViewerTransform( affine );

		final ScaleBarOverlayRenderer scalebar =
				Prefs.showScaleBarInMovie() ? new ScaleBarOverlayRenderer() : null;

		class MyTarget implements RenderTarget
		{
			BufferedImage bi;

			@Override
			public BufferedImage setBufferedImage( final BufferedImage bufferedImage )
			{
				bi = bufferedImage;
				return null;
			}

			@Override
			public int getWidth()
			{
				return width;
			}

			@Override
			public int getHeight()
			{
				return height;
			}
		}

		final MyTarget target = new MyTarget();
		final MultiResolutionRenderer renderer = new MultiResolutionRenderer(
				target, new PainterThread( null ), new double[] { 1 }, 0, false, 1, null, false,
				viewer.getOptionValues().getAccumulateProjectorFactory(), new CacheControl.Dummy() );


		int timepoint = 0;
		renderState.setCurrentTimepoint( timepoint );
		renderer.requestRepaint();
		renderer.paint( renderState );

		if ( Prefs.showScaleBarInMovie() )
		{
			final Graphics2D g2 = target.bi.createGraphics();
			g2.setClip( 0, 0, width, height );
			scalebar.setViewerState( renderState );
			scalebar.paint( g2 );
		}

		return target.bi;

	}


	public static < T extends RealType< T > & NativeType< T > >
	void showAsIJ1MultiColorImage(
			Bdv bdv,
			double voxelSpacing,
			ArrayList< RandomAccessibleInterval< T > > rais )
	{
		final RandomAccessibleInterval< T > stack = Views.stack( rais );

		final ImagePlus imp = ImageJFunctions.wrap( stack, "capture" );
		// duplicate: otherwise it is virtual and cannot be modified
		final ImagePlus dup = new Duplicator().run( imp );
		IJ.run( dup, "Subtract...", "value=32768 slice");
		VoxelDimensions voxelDimensions = getVoxelDimensions( bdv, 0 );
		IJ.run( dup,
				"Properties...",
				"channels="+rais.size()
						+" slices=1 frames=1 unit="+voxelDimensions.unit()
						+" pixel_width="+voxelSpacing
						+" pixel_height="+voxelSpacing+" voxel_depth=1.0");
		final CompositeImage compositeImage = new CompositeImage( dup );
		for ( int channel = 1; channel <= compositeImage.getNChannels(); ++channel )
		{
			compositeImage.setC( channel );
			switch ( channel )
			{
				case 1:
					compositeImage.setChannelLut(
							compositeImage.createLutFromColor( Color.GRAY ) );
					compositeImage.setDisplayRange( 0, 1000 ); // TODO: get from bdv
					break;
				case 2: compositeImage.setChannelLut(
						compositeImage.createLutFromColor( Color.GRAY ) ); break;
				case 3: compositeImage.setChannelLut(
						compositeImage.createLutFromColor( Color.RED ) ); break;
				case 4: compositeImage.setChannelLut(
						compositeImage.createLutFromColor( Color.GREEN ) ); break;
				default: compositeImage.setChannelLut(
						compositeImage.createLutFromColor( Color.BLUE ) ); break;
			}
		}
		compositeImage.show();
		compositeImage.setTitle( "capture" );
		IJ.run(compositeImage, "Make Composite", "");
	}
}
