package tests;

import de.embl.cba.bdv.utils.lut.GlasbeyARGBLut;
import de.embl.cba.tables.Tables;
import de.embl.cba.tables.color.LazyCategoryColoringModel;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.plot.TableRowsScatterPlot;
import de.embl.cba.tables.select.DefaultSelectionModel;
import de.embl.cba.tables.select.SelectionModels;
import de.embl.cba.tables.tablerow.ColumnBasedTableRow;
import org.junit.Test;

import java.util.List;

public class TestScatterPlot
{
	@Test
	public void run()
	{
		List< ColumnBasedTableRow > tableRows = Tables.open( "src/test/resources/test-data/try_plot_umap.csv" );

		SelectionColoringModel< ColumnBasedTableRow > selectionColoringModel = SelectionModels.getDefaultSelectionColoringModel();

		TableRowsScatterPlot scatterPlot = new TableRowsScatterPlot(
				tableRows,
				selectionColoringModel,
				new String[]{ "umap_1", "umap_2" },
				new double[]{ 1.0, 1.0 },
				0.2 );

		scatterPlot.show( );
	}

	public static void main( String[] args )
	{
		new TestScatterPlot().run();
	}
}
