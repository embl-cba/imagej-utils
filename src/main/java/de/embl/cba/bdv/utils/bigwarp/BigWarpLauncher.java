package de.embl.cba.bdv.utils.bigwarp;

import bdv.ij.util.ProgressWriterIJ;
import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.MinMaxGroup;
import bdv.util.BdvHandle;
import bdv.viewer.Source;
import bigwarp.BigWarp;
import bigwarp.BigWarpInit;
import de.embl.cba.bdv.utils.BdvUtils;
import ij.gui.GenericDialog;
import mpicbg.spim.data.SpimDataException;

import java.util.List;

public class BigWarpLauncher
{
	public static class Dialog
	{
		public static Source< ? > movingVolatileSource;
		public static Source< ? > fixedVolatileSource;
		public static double[] displayRangeMovingSource;
		public static double[] displayRangeFixedSource;

		public static boolean showDialog( BdvHandle bdvHandle, List< Integer > sourceIndices )
		{
			final GenericDialog gd = new GenericDialog( "Register Images in Big Warp" );

			final String[] sourceNames = new String[ sourceIndices.size() ];
			for ( int i = 0; i < sourceNames.length ; i++ )
				sourceNames[ i ]  = BdvUtils.getSourceName( bdvHandle, sourceIndices.get( i ) );

			gd.addChoice( "Moving image", sourceNames, sourceNames[ 0 ] );
			gd.addChoice( "Fixed image", sourceNames, sourceNames[ 1 ] );

			gd.showDialog();
			if ( gd.wasCanceled() ) return false;

			final int movingSourceIndex = BdvUtils.getSourceIndex( bdvHandle, gd.getNextChoice() );
			movingVolatileSource = BdvUtils.getVolatileSource( bdvHandle, movingSourceIndex );
			displayRangeMovingSource = BdvUtils.getDisplayRange( bdvHandle, movingSourceIndex );

			final int fixedSourceIndex = BdvUtils.getSourceIndex( bdvHandle, gd.getNextChoice() );
			fixedVolatileSource = BdvUtils.getVolatileSource( bdvHandle, fixedSourceIndex );
			displayRangeFixedSource = BdvUtils.getDisplayRange( bdvHandle, fixedSourceIndex );

			return true;
		}

	}

	public BigWarpLauncher( Source< ? > movingSource, Source< ? > fixedSource, double[] displayRangeMovingSource, double[] displayRangeFixedSource )
	{
		final String[] sourceNames = { movingSource.getName(), fixedSource.getName() };
		final Source< ? >[] movingSources = { movingSource };
		final Source< ? >[] fixedSources = { movingSource };

		final BigWarp.BigWarpData< ? > bigWarpData = BigWarpInit.createBigWarpData( movingSources, fixedSources, sourceNames );
		final BigWarp bigWarp = tryGetBigWarp( bigWarpData );

		setDisplayRange( bigWarp, displayRangeMovingSource, 0 );
		setDisplayRange( bigWarp, displayRangeMovingSource, 1 );

		bigWarp.getViewerFrameP().getViewerPanel().requestRepaint();
		bigWarp.getViewerFrameQ().getViewerPanel().requestRepaint();
		bigWarp.getLandmarkFrame().repaint();
	}

	public void setDisplayRange( BigWarp bigWarp, double[] displayRangeMovingSource, int sourceIndexBigWarp )
	{
		final ConverterSetup converterSetup = bigWarp.getSetupAssignments().getConverterSetups().get( sourceIndexBigWarp );
		final double min = displayRangeMovingSource[ 0 ];
		final double max = displayRangeMovingSource[ 1 ];
		converterSetup.setDisplayRange( min, max );
		final MinMaxGroup minMaxGroup = bigWarp.getSetupAssignments().getMinMaxGroup( converterSetup );
		minMaxGroup.getMinBoundedValue().setCurrentValue( min );
		minMaxGroup.getMaxBoundedValue().setCurrentValue( max );
	}

	public BigWarp tryGetBigWarp( BigWarp.BigWarpData< ? > bigWarpData )
	{
		try
		{
			return new BigWarp( bigWarpData, "Big Warp",  new ProgressWriterIJ() );
		} catch ( SpimDataException e )
		{
			e.printStackTrace();
		}
		return null;
	}

}
