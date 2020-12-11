package tests;

import de.embl.cba.tables.Tables;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.plot.TableRowsScatterPlot;
import de.embl.cba.tables.select.SelectionModels;
import de.embl.cba.tables.tablerow.ColumnBasedTableRow;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class TestScatterPlot
{
	//@Test // TODO: why does this not run as a test but throws an assertion error?
	public void run()
	{
		List< ColumnBasedTableRow > tableRows = Tables.open( "src/test/resources/test-data/umap.csv" );

		SelectionColoringModel< ColumnBasedTableRow > selectionColoringModel = SelectionModels.getDefaultSelectionColoringModel();

		ArrayList< String > colNames = new ArrayList<>( tableRows.get( 0 ).getColumnNames() );

		TableRowsScatterPlot scatterPlot = new TableRowsScatterPlot(
				tableRows,
				selectionColoringModel,
				new String[]{ colNames.get( 1 ), colNames.get( 2 ) },
				new double[]{ 1.0, 1.0 },
				0.2 );

		scatterPlot.show( );
	}

	public static void main( String[] args )
	{
		new TestScatterPlot().run();
	}
}
