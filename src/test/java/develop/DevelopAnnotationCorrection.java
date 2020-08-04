package develop;

import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.tables.morpholibj.ExploreMorphoLibJLabelImage;
import de.embl.cba.tables.view.combined.SegmentsTableBdvAnd3dViews;
import ij.IJ;
import ij.ImagePlus;
import net.imagej.ImageJ;
import tests.Test3DView;

public class DevelopAnnotationCorrection
{
	public static void main( String[] args )
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

		BdvUtils.centerBdvWindowLocation( views.getSegmentsBdvView().getBdv() );

		views.getTableRowsTableView().addColumn( "Annotation", new String[]{"None", "A", "B" } );
		views.getTableRowsTableView().continueAnnotation( "Annotation" );
	}
}
