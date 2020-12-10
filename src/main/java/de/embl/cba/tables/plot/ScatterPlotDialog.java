package de.embl.cba.tables.plot;

import ij.gui.GenericDialog;

public class ScatterPlotDialog
{
	private final String[] columnNames;
	private final String[] selectedColumnNames;
	private final double[] scaleFactors;
	private double dotSizeScaleFactor;

	public ScatterPlotDialog( String[] columnNames, String[] selectedColumnNames, double[] scaleFactors, double dotSizeScaleFactor )
	{
		this.columnNames = columnNames;
		this.selectedColumnNames = selectedColumnNames;
		this.scaleFactors = scaleFactors;
		this.dotSizeScaleFactor = dotSizeScaleFactor;
	}

	public boolean show()
	{
		final String[] xy = { "X", "Y " };

		//lineChoices = new String[]{ GridLinesOverlay.NONE, GridLinesOverlay.Y_NX, GridLinesOverlay.Y_N };

		final GenericDialog gd = new GenericDialog( "Column selection" );

		for ( int d = 0; d < xy.length; d++ )
		{
			gd.addChoice( "Column " + xy[ d ], columnNames, selectedColumnNames[ d ] );
			gd.addNumericField( "Scale Factor " + xy[ d ], scaleFactors[ d ] );
		}

		gd.addNumericField( "Dot Size Scale Factor", dotSizeScaleFactor );
		//gd.addChoice( "Add lines", lineChoices, GridLinesOverlay.NONE );
		gd.showDialog();

		if ( gd.wasCanceled() ) return false;

		for ( int d = 0; d < xy.length; d++ )
		{
			selectedColumnNames[ d ] = gd.getNextChoice();
			scaleFactors[ d ] = gd.getNextNumber();
		}
		dotSizeScaleFactor = gd.getNextNumber();

		return true;
	}

	public String[] getSelectedColumns()
	{
		return selectedColumnNames;
	}

	public double[] getScaleFactors()
	{
		return scaleFactors;
	}

	public double getDotSizeScaleFactor()
	{
		return dotSizeScaleFactor;
	}
}
