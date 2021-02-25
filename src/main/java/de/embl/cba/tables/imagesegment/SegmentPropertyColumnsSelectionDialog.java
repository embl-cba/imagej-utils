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
package de.embl.cba.tables.imagesegment;

import de.embl.cba.tables.imagesegment.SegmentProperty;
import ij.Prefs;
import ij.gui.GenericDialog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class SegmentPropertyColumnsSelectionDialog
{
	public static final String NO_COLUMN_SELECTED = "None";
	public static final String IMAGE_SEGMENT_COORDINATE_COLUMN_PREFIX = "SegmentPropertyColumn.";

	private String[] columnChoices;
	private final GenericDialog gd;

	public SegmentPropertyColumnsSelectionDialog( Collection< String > columns )
	{
		setColumnChoices( columns );

		gd = new GenericDialog( "Image Segments Properties Columns Selection" );

		addColumnSelectionUIs();
	}

	private void addColumnSelectionUIs()
	{
		for ( SegmentProperty coordinate : SegmentProperty.values() )
		{
			final String previousChoice =
					Prefs.get( getKey( coordinate ), columnChoices[ 0 ] );
			gd.addChoice( coordinate.toString(), columnChoices, previousChoice );
		}
	}

	private Map< SegmentProperty, String > collectChoices()
	{
		final HashMap< SegmentProperty, String > coordinateToColumnName = new HashMap<>();

		for ( SegmentProperty coordinate : SegmentProperty.values() )
		{
			final String columnName = gd.getNextChoice();
			coordinateToColumnName.put( coordinate, columnName );
			Prefs.set( getKey( coordinate ), columnName );
		}

		Prefs.savePreferences();

		return coordinateToColumnName;
	}

	private String getKey( SegmentProperty coordinate )
	{
		return IMAGE_SEGMENT_COORDINATE_COLUMN_PREFIX + coordinate.toString();
	}

	private void setColumnChoices( Collection< String > columns )
	{
		final int numColumns = columns.size();

		columnChoices = new String[ numColumns + 1 ];

		columnChoices[ 0 ] = NO_COLUMN_SELECTED;

		int i = 1;
		for ( String column : columns )
			columnChoices[ i++ ] = column;
	}


	public Map< SegmentProperty, String > fetchUserInput()
	{
		gd.showDialog();

		if ( gd.wasCanceled() ) return null;

		final Map< SegmentProperty, String > coordinateToColumn = collectChoices();

		return coordinateToColumn;
	}


}
