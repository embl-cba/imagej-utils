package de.embl.cba.bdv.utils.bigwarp;

import bdv.ij.util.ProgressWriterIJ;
import bdv.util.BdvHandle;
import bdv.viewer.Source;
import bigwarp.BigWarp;
import bigwarp.BigWarpInit;
import de.embl.cba.bdv.utils.BdvUtils;
import ij.gui.GenericDialog;
import mpicbg.spim.data.SpimDataException;

import java.util.Arrays;
import java.util.List;

public class BigWarpLauncher
{
	public static class Dialog
	{
		public static Source< ? > movingSource;
		public static Source< ? > fixedSource;

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


			final Source< ? > movingSource = BdvUtils.getVolatileSource( bdvHandle, BdvUtils.getSourceIndex( bdvHandle, gd.getNextChoice() ) );
			final Source< ? > fixedSource = BdvUtils.getVolatileSource( bdvHandle, BdvUtils.getSourceIndex( bdvHandle, gd.getNextChoice() ) );

			return true;
		}

	}

	public BigWarpLauncher( Source< ? > movingSource, Source< ? > fixedSource )
	{
		final String[] sourceNames = { movingSource.getName(), fixedSource.getName() };
		final Source< ? >[] movingSources = { movingSource };
		final Source< ? >[] fixedSources = { movingSource };

		final BigWarp.BigWarpData< ? > bigWarpData = BigWarpInit.createBigWarpData( movingSources, fixedSources, sourceNames );
		final BigWarp bigWarp = tryGetBigWarp( bigWarpData );
		bigWarp.getViewerFrameP().getViewerPanel().requestRepaint();
		bigWarp.getViewerFrameQ().getViewerPanel().requestRepaint();
		bigWarp.getLandmarkFrame().repaint();
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
