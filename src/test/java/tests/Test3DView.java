/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2021 EMBL
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
package tests;

import bdv.viewer.Source;
import de.embl.cba.tables.Utils;
import de.embl.cba.tables.ij3d.UniverseUtils;
import de.embl.cba.tables.morpholibj.ExploreMorphoLibJLabelImage;
import de.embl.cba.tables.select.SelectionModel;
import de.embl.cba.tables.tablerow.TableRowImageSegment;
import de.embl.cba.tables.view.Segments3dView;
import de.embl.cba.tables.view.combined.SegmentsTableBdvAnd3dViews;
import ij.IJ;
import ij.ImagePlus;
import ij3d.ContentConstants;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imagej.ImageJ;
import net.imagej.patcher.LegacyInjector;
import net.imglib2.type.numeric.ARGBType;
import org.junit.Test;

import java.util.List;

public class Test3DView
{
	//@Test
	public void showObjectAndVolumeIn3D() throws SpimDataException
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		final ImagePlus intensities = IJ.openImage(
				Test3DView.class.getResource(
						"../test-data/3d-image.zip" ).getFile() );

		final ImagePlus labels = IJ.openImage(
				Test3DView.class.getResource(
						"../test-data/3d-image-lbl.zip" ).getFile() );

		IJ.open( Test3DView.class.getResource(
				"../test-data/3d-image-lbl-morpho.csv" ).getFile() );

		final ExploreMorphoLibJLabelImage explore = new ExploreMorphoLibJLabelImage(
				intensities,
				labels,
				"3d-image-lbl-morpho.csv" );

		final SegmentsTableBdvAnd3dViews views = explore.getTableBdvAnd3dViews();

		final SelectionModel< TableRowImageSegment > selectionModel = views.getSelectionModel();
		final List< TableRowImageSegment > tableRowImageSegments = views.getTableRowImageSegments();

		selectionModel.setSelected( tableRowImageSegments.get( 0 ), true );
		selectionModel.focus( tableRowImageSegments.get( 0 ) );

		final Segments3dView< TableRowImageSegment > segments3dView = views.getSegments3dView();

		// Add SpimData volume
		SpimData spimData = new XmlIoSpimData().load( Test3DView.class.getResource(
				"../test-data/3d-image.xml" ).getFile()  );

		final Source< ? > source = Utils.getSource( spimData, 0 );


		segments3dView.initUniverseAndListener();

		UniverseUtils.addSourceToUniverse(
				segments3dView.getUniverse(),
				source,
				100 * 100 * 100,
				ContentConstants.VOLUME,
				new ARGBType( 0xff00ff00 ),
				0.0F, 0, 255 );

		// Add ImagePlus volume
		intensities.getCalibration().pixelWidth = 10;
		UniverseUtils.addImagePlusToUniverse( segments3dView.getUniverse(), intensities, ContentConstants.VOLUME, 0F );
	}

	public static void main( String[] args ) throws SpimDataException
	{
		new Test3DView().showObjectAndVolumeIn3D();
	}
}
