package de.embl.cba.tables.results;

import ij.WindowManager;
import ij.text.TextWindow;

import java.awt.*;
import java.util.HashMap;

public class ResultsTableFetcher
{
	public static HashMap< String, ij.measure.ResultsTable > fetchResultsTables()
	{
		HashMap< String, ij.measure.ResultsTable > titleToResultsTable = new HashMap<>();

		final Frame[] nonImageWindows = WindowManager.getNonImageWindows();
		for ( Frame nonImageWindow : nonImageWindows )
		{
			if ( nonImageWindow instanceof TextWindow )
			{
				final TextWindow textWindow = ( TextWindow ) nonImageWindow;

				final ij.measure.ResultsTable resultsTable = textWindow.getResultsTable();

				if ( resultsTable != null )
					titleToResultsTable.put( resultsTable.getTitle(), resultsTable );
			}
		}

		return titleToResultsTable;
	}
}
