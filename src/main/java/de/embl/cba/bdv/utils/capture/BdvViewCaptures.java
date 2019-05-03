package de.embl.cba.bdv.utils.capture;

import bdv.cache.CacheControl;
import bdv.util.Bdv;
import bdv.util.BdvHandle;
import bdv.util.Prefs;
import bdv.viewer.Source;
import bdv.viewer.ViewerPanel;
import bdv.viewer.overlay.ScaleBarOverlayRenderer;
import bdv.viewer.render.MultiResolutionRenderer;
import bdv.viewer.state.ViewerState;

import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.transforms.utils.Transforms;
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.plugin.Duplicator;
import ij.process.LUT;
import net.imglib2.*;
import net.imglib2.Point;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.realtransform.Scale;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
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


	/**
	 * TODO:
	 * - make it optional to capture an interpolated view
	 *
	 *
	 *
	 * @param bdv
	 * @param pixelSpacing
	 * @param voxelUnits
	 *
	 */
	public static void captureView( BdvHandle bdv, double pixelSpacing, String voxelUnits )
	{

		final AffineTransform3D viewerTransform = new AffineTransform3D();
		bdv.getViewerPanel().getState().getViewerTransform( viewerTransform );

		final double[] viewerVoxelSpacing = getViewerVoxelSpacing( bdv );

		double dxy = pixelSpacing / viewerVoxelSpacing[ 0 ] ;

		final int w = getBdvWindowWidth( bdv );
		final int h = getBdvWindowHeight( bdv );

		final long captureWidth = ( long ) Math.ceil( w / dxy );
		final long captureHeight = ( long ) Math.ceil( h / dxy );

		final ArrayList< RandomAccessibleInterval< UnsignedShortType > > rais
				= new ArrayList<>();
		final ArrayList< ARGBType > colors
				= new ArrayList<>();
		final ArrayList< double[] > displayRanges
				= new ArrayList<>();

		final List< Integer > sourceIndices = getVisibleSourceIndices( bdv );

		for ( int sourceIndex : sourceIndices )
		{
			if ( BdvUtils.isSourceIntersectingCurrentView( bdv, sourceIndex ) )
			{

				final RandomAccessibleInterval< UnsignedShortType > rai
						= ArrayImgs.unsignedShorts( captureWidth, captureHeight );

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
				final double[] sourceRealPosition = new double[ 3 ];
				final long[] sourcePosition = new long[ 3 ];

				// TODO: rather loop through the capture image
				for ( int x = 0; x < captureWidth; x++ )
					for ( int y = 0; y < captureHeight; y++ )
					{
						canvasPosition[ 0 ] = x * dxy;
						canvasPosition[ 1 ] = y * dxy;

						viewerToSourceTransform.apply( canvasPosition, sourceRealPosition );

						// TODO: make work with real interpolated access
						for ( int d = 0; d < 3; d++ )
							sourcePosition[ d ] = (long) sourceRealPosition[ d ];

						if ( Intervals.contains( sourceRai, new Point( sourcePosition ) ) )
						{
							sourceAccess.setPosition( sourcePosition );
							Double pixelValue = sourceAccess.get().getRealDouble();
							access.setPosition( x, 0 );
							access.setPosition( y, 1 );
							access.get().setReal( pixelValue );
						}
					}

				rais.add( rai );
				colors.add( getSourceColor( bdv, sourceIndex ) );

				displayRanges.add( BdvUtils.getDisplayRange( bdv, sourceIndex) );
			}
		}

		final double[] captureVoxelSpacing = new double[ 3 ];
		for ( int d = 0; d < 2; d++ )
			captureVoxelSpacing[ d ] = pixelSpacing;
		captureVoxelSpacing[ 2 ] = viewerVoxelSpacing[ 2 ]; // TODO: makes sense?

		if ( rais.size() > 0 )
			showAsCompositeImage( captureVoxelSpacing, voxelUnits, rais, colors, displayRanges );
	}

	public static
	void showAsCompositeImage(
			double[] voxelSpacing,
			String voxelUnit,
			ArrayList< RandomAccessibleInterval< UnsignedShortType > > rais,
			ArrayList< ARGBType > colors,
			ArrayList< double[] > displayRanges )
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
			final Color color = new Color( colors.get( channel - 1 ).get() );
			final LUT lut = compositeImage.createLutFromColor( color );
			compositeImage.setC( channel );
			compositeImage.setChannelLut( lut );
			final double[] range = displayRanges.get( channel - 1 );
			compositeImage.setDisplayRange( range[ 0 ], range[ 1 ] );
		}

		compositeImage.show();
		compositeImage.setTitle( "Bdv View Capture" );
		IJ.run(compositeImage, "Make Composite", "");

	}
}
