package tests;

import de.embl.cba.tables.TableColumns;
import de.embl.cba.tables.morpholibj.ExploreMorphoLibJLabelImage;
import de.embl.cba.tables.view.combined.SegmentsTableBdvAnd3dViews;
import ij.IJ;
import net.imagej.ImageJ;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestAppendTables
{
	// TODO: write a proper test without any UI popping up
	public static void main( String[] args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		IJ.open( TestAppendTables.class.getResource(
				"../test-data/3d-image-lbl-morpho.csv" ).getFile() );

		final ExploreMorphoLibJLabelImage explore = new ExploreMorphoLibJLabelImage(
				IJ.openImage( TestAppendTables.class.getResource(
						"../test-data/3d-image.zip" ).getFile() ),
				IJ.openImage( TestAppendTables.class.getResource(
						"../test-data/3d-image-lbl.zip" ).getFile() ),
				"3d-image-lbl-morpho" );

		final SegmentsTableBdvAnd3dViews views = explore.getTableBdvAnd3dViews();

		final ArrayList< Double > orderColumn = TableColumns.getNumericColumnAsDoubleList(
				views.getTableRowsTableView().getTable(),
				ExploreMorphoLibJLabelImage.LABEL );

		Map< String, List< String > > columns2 =
			TableColumns.orderedStringColumnsFromTableFile(
					TestAppendTables.class.getResource( "../test-data/3d-image-lbl-morpho-unorderedIncompleteAddOn.csv" ).getFile(),
					null,
					"Label",
					orderColumn );

		views.getTableRowsTableView().addColumns( columns2 );
	}
}

