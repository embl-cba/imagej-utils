package de.embl.cba.bdv.utils;

import bdv.cache.CacheControl;
import bdv.util.Bdv;
import bdv.util.Prefs;
import bdv.viewer.ViewerPanel;
import bdv.viewer.overlay.ScaleBarOverlayRenderer;
import bdv.viewer.render.MultiResolutionRenderer;
import bdv.viewer.state.ViewerState;
import de.embl.cba.bdv.utils.intervals.Intervals;
import de.embl.cba.bdv.utils.transforms.Transforms;
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.*;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.realtransform.Scale;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.ui.PainterThread;
import net.imglib2.ui.RenderTarget;
import net.imglib2.view.Views;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static de.embl.cba.bdv.utils.BdvUtils.*;
import static de.embl.cba.bdv.utils.intervals.Intervals.asIntegerInterval;

public abstract class BdvViewCaptures
{

	public static < T extends RealType< T > & NativeType< T > > void captureView( Bdv bdv, double resolution )
	{

		double[] scalingFactors = new double[ 3 ];
		scalingFactors[ 0 ] = 1 / resolution;
		scalingFactors[ 1 ] = 1 / resolution;
		scalingFactors[ 2 ] = 1.0;

		int n = bdv.getBdvHandle().getViewerPanel().getState().getSources().size();

		final ArrayList< RandomAccessibleInterval< T > > randomAccessibleIntervals = new ArrayList<>();

		for ( int sourceIndex = 0; sourceIndex < n; ++sourceIndex )
		{
			final FinalInterval interval = getCurrentlyVisibleInterval( bdv, sourceIndex );

			final FinalRealInterval viewerRealInterval = getCurrentViewerInterval( bdv );

			final boolean intersecting = Intervals.intersecting( interval, viewerRealInterval );

			if ( intersecting )
			{
				RealRandomAccessible< T > rra = ( RealRandomAccessible ) getRealRandomAccessible( bdv, sourceIndex );
				final AffineTransform3D sourceTransform = getSourceTransform( bdv, sourceIndex );
				rra = RealViews.transform( rra, sourceTransform );

				Scale scale = new Scale( scalingFactors );
				RealRandomAccessible< T > rescaledRRA = RealViews.transform( rra, scale );
				final RandomAccessible< T > rastered = Views.raster( rescaledRRA );

				final FinalInterval scaledInterval = Transforms.scaleIntervalInXY( asIntegerInterval( viewerRealInterval ), scale );

				final RandomAccessibleInterval< T > cropped = Views.interval( rastered, scaledInterval );

				randomAccessibleIntervals.add( cropped  );
			}

		}

		showAsIJ1MultiColorImage( bdv, resolution, randomAccessibleIntervals );

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

		final ScaleBarOverlayRenderer scalebar = Prefs.showScaleBarInMovie() ? new ScaleBarOverlayRenderer() : null;

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
}
