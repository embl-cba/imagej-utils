package de.embl.cba.bdv.utils.behaviour;

import bdv.ij.util.ProgressWriterIJ;
import bdv.tools.boundingbox.TransformedRealBoxSelectionDialog;
import bdv.util.BdvHandle;
import bdv.viewer.Interpolation;
import de.embl.cba.bdv.utils.BdvDialogs;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.Logger;
import de.embl.cba.bdv.utils.capture.BdvViewCaptures;
import de.embl.cba.bdv.utils.capture.PixelSpacingDialog;
import de.embl.cba.bdv.utils.export.BdvRealSourceToVoxelImageExporter;
import ij.CompositeImage;
import ij.IJ;
import ij.gui.GenericDialog;
import net.imglib2.FinalRealInterval;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;

import javax.swing.*;
import java.io.File;

import static de.embl.cba.bdv.utils.export.BdvRealSourceToVoxelImageExporter.*;

// TODO:
// - remove logging, return things

public class BdvBehaviours
{
	public static void addPositionAndViewLoggingBehaviour(
			BdvHandle bdv,
			org.scijava.ui.behaviour.util.Behaviours behaviours,
			String trigger )
	{
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {

			(new Thread( () -> {
				final RealPoint globalMouseCoordinates = BdvUtils.getGlobalMouseCoordinates( bdv );
				Logger.log( "\nBigDataViewer position: \n" + globalMouseCoordinates.toString() );
				Logger.log( "BigDataViewer transform: \n"+ getBdvViewerTransform( bdv ) );
			} )).start();

		}, "Print position and view", trigger ) ;
	}

	public static void addViewCaptureBehaviour(
			BdvHandle bdv,
			org.scijava.ui.behaviour.util.Behaviours behaviours,
			String trigger )
	{
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) ->
		{
			new Thread( () -> {
				final String pixelUnit = "micrometer";
				final PixelSpacingDialog dialog = new PixelSpacingDialog( BdvUtils.getViewerVoxelSpacing( bdv )[ 0 ], pixelUnit );
				if ( ! dialog.showDialog() ) return;
				final CompositeImage compositeImage = BdvViewCaptures.captureView(
						bdv,
						dialog.getPixelSpacing(),
						pixelUnit,
						false );
				compositeImage.show();
			}).start();
		}, "capture view", trigger ) ;
	}

	public static void addExportSourcesToVoxelImagesBehaviour(
			BdvHandle bdvHandle,
			org.scijava.ui.behaviour.util.Behaviours behaviours,
			String trigger )
	{
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) ->
		{
			new Thread( () ->
			{
				final FinalRealInterval maximalRangeInterval = BdvUtils.getRealIntervalOfVisibleSources( bdvHandle );

				final TransformedRealBoxSelectionDialog.Result result =
						BdvDialogs.showBoundingBoxDialog(
								bdvHandle,
								maximalRangeInterval );

				BdvUtils.getVoxelDimensionsOfCurrentSource( bdvHandle ).dimensions( Dialog.outputVoxelSpacings );

				if ( ! Dialog.showDialog() ) return;

				final BdvRealSourceToVoxelImageExporter exporter =
						new BdvRealSourceToVoxelImageExporter(
								bdvHandle,
								BdvUtils.getVisibleSourceIndices( bdvHandle ),
								result.getInterval(),
								result.getMinTimepoint(),
								result.getMaxTimepoint(),
								Dialog.interpolation,
								Dialog.outputVoxelSpacings,
								Dialog.exportModality,
								Dialog.exportDataType,
								Dialog.numProcessingThreads,
								new ProgressWriterIJ()
						);

				if ( Dialog.exportModality.equals( ExportModality.SaveAsTiffStacks ) )
				{
					final String outputDirectory = IJ.getDirectory( "Choose and output directory" );
					exporter.setOutputDirectory( outputDirectory );
				}


				exporter.export();

			}).start();
		}, "ExportSourcesToVoxelImages", trigger ) ;
	}

	public static void addDisplaySettingsBehaviour(
			BdvHandle bdv,
			org.scijava.ui.behaviour.util.Behaviours behaviours,
			String trigger )
	{
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) ->
						BdvDialogs.showDisplaySettingsDialogForSourcesAtMousePosition(
								bdv,
								false,
								true ),
				"show display settings dialog",
				trigger ) ;
	}

	public static String getBdvViewerTransform( BdvHandle bdv )
	{
		final AffineTransform3D view = new AffineTransform3D();
		bdv.getViewerPanel().getState().getViewerTransform( view );

		return view.toString().replace( "3d-affine", "View" );
	}

	public static void addSourceBrowsingBehaviour( BdvHandle bdv, Behaviours behaviours  )
	{
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {

			(new Thread( () -> {
				final int currentSource = bdv.getViewerPanel().getVisibilityAndGrouping().getCurrentSource();
				if ( currentSource == 0 ) return;
				bdv.getViewerPanel().getVisibilityAndGrouping().setCurrentSource( currentSource - 1 );
			} )).start();

		}, "Go to previous source", "J" ) ;

		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {

			(new Thread( () -> {
				final int currentSource = bdv.getViewerPanel().getVisibilityAndGrouping().getCurrentSource();
				if ( currentSource == bdv.getViewerPanel().getVisibilityAndGrouping().numSources() - 1  ) return;
				bdv.getViewerPanel().getVisibilityAndGrouping().setCurrentSource( currentSource + 1 );
			} )).start();

		}, "Go to next source", "K" ) ;
	}
}
