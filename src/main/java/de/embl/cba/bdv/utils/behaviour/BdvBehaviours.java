/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2024 EMBL
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
package de.embl.cba.bdv.utils.behaviour;

import bdv.ij.util.ProgressWriterIJ;
import bdv.tools.boundingbox.TransformedRealBoxSelectionDialog;
import bdv.util.BdvHandle;
import bdv.viewer.SourceAndConverter;
import de.embl.cba.bdv.utils.BdvDialogs;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.Logger;
import de.embl.cba.bdv.utils.capture.BdvViewCaptures;
import de.embl.cba.bdv.utils.capture.ViewCaptureDialog;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;

import javax.swing.*;
import java.util.List;

// TODO:
// - remove logging, return things

public class BdvBehaviours
{
	public static void addPositionAndViewLoggingBehaviour(
			BdvHandle bdv,
			Behaviours behaviours,
			String trigger )
	{
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {

			(new Thread( () -> {
				Logger.log( "\nBigDataViewer position: " + BdvUtils.getGlobalMousePositionString( bdv ) );
				Logger.log( "BigDataViewer transform: " + BdvUtils.getBdvViewerTransformString( bdv ) );
			} )).start();

		}, "Print position and view", trigger ) ;
	}

	public static void addViewCaptureBehaviour(
			BdvHandle bdvHandle,
			Behaviours behaviours,
			String trigger )
	{
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) ->
		{
			new Thread( () -> {
				new ViewCaptureDialog( bdvHandle ).run();
			}).start();
		}, "capture raw view", trigger ) ;
	}

	public static void addSimpleViewCaptureBehaviour(
			BdvHandle bdv,
			Behaviours behaviours,
			String trigger )
	{
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) ->
		{
			new Thread( () -> {
				SwingUtilities.invokeLater( () -> {
					final JFileChooser jFileChooser = new JFileChooser();
					if ( jFileChooser.showSaveDialog( bdv.getViewerPanel() ) == JFileChooser.APPROVE_OPTION )
					{
						BdvViewCaptures.saveScreenShot(
								jFileChooser.getSelectedFile(),
								bdv.getViewerPanel() );
					}
				});
			}).start();

		}, "capture simple view", trigger ) ;
	}

	public static void addDisplaySettingsBehaviour(
			BdvHandle bdv,
			Behaviours behaviours,
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

	public static void addSourceBrowsingBehaviour( BdvHandle bdv, Behaviours behaviours  )
	{
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {

			(new Thread( () -> {
				final List< SourceAndConverter< ? > > sources = bdv.getViewerPanel().state().getSources();
				final SourceAndConverter< ? > currentSource = bdv.getViewerPanel().state().getCurrentSource();
				int currentSourceIndex = sources.indexOf( currentSource );
				if ( currentSourceIndex == 0 )
					return;
				else
					bdv.getViewerPanel().state().setCurrentSource( sources.get( --currentSourceIndex ) );
			} )).start();

		}, "Go to previous source", "J" ) ;

		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {

			(new Thread( () -> {
				final List< SourceAndConverter< ? > > sources = bdv.getViewerPanel().state().getSources();
				final SourceAndConverter< ? > currentSource = bdv.getViewerPanel().state().getCurrentSource();
				int currentSourceIndex = sources.indexOf( currentSource );
				if ( currentSourceIndex == sources.size() - 1 )
					return;
				else
					bdv.getViewerPanel().state().setCurrentSource( sources.get( ++currentSourceIndex ) );
			} )).start();

		}, "Go to next source", "K" ) ;
	}
}
