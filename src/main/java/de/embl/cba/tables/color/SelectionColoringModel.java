/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2022 EMBL
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
package de.embl.cba.tables.color;

import de.embl.cba.tables.select.SelectionModel;
import net.imglib2.type.numeric.ARGBType;

import java.util.Arrays;
import java.util.List;

public class SelectionColoringModel < T > extends AbstractColoringModel< T >
{
	private ColoringModel< T > coloringModel;
	private SelectionModel< T > selectionModel;

	private SelectionColoringMode selectionColoringMode;
	private ARGBType selectionColor;
	private double brightnessNotSelected;

	public static final ARGBType YELLOW = new ARGBType( ARGBType.rgba( 255, 255, 0, 255 ) );

	private final List< SelectionColoringMode > selectionColoringModes;

	public enum SelectionColoringMode
	{
		SelectionColor,
		SelectionColorAndDimNotSelected,
		DimNotSelected
	}

	public SelectionColoringModel(
			ColoringModel< T > coloringModel,
			SelectionModel< T > selectionModel )
	{
		setColoringModel( coloringModel );
		this.selectionModel = selectionModel;
		this.selectionColoringModes = Arrays.asList( SelectionColoringMode.values() );

		this.selectionColor = YELLOW;
		this.brightnessNotSelected = 0.2;
		this.selectionColoringMode = SelectionColoringMode.DimNotSelected;
	}

	@Override
	public void convert( T input, ARGBType output )
	{
		coloringModel.convert( input, output );

		if ( selectionModel.isEmpty() ) return;

		final boolean isSelected = selectionModel.isSelected( input );

		switch ( selectionColoringMode )
		{
			case DimNotSelected:
				if ( ! isSelected )
					dim( output );
				break;

			case SelectionColor:
				if ( isSelected )
					output.set( selectionColor );
				break;

			case SelectionColorAndDimNotSelected:
				if ( isSelected )
					output.set( selectionColor );
				else
					dim( output );
				break;

			default:
				break;
		}
	}

	/**
	 * Implements dimming via alpha
	 *
	 * @param output
	 */
	private void dim( ARGBType output )
	{
		final int colorIndex = output.get();

		output.set(
				ARGBType.rgba(
						ARGBType.red( colorIndex ),
						ARGBType.green( colorIndex ),
						ARGBType.blue( colorIndex ),
						brightnessNotSelected * 255 )
		);
	}

	public void setSelectionColoringMode( SelectionColoringMode selectionColoringMode )
	{
		this.selectionColoringMode = selectionColoringMode;
		notifyColoringListeners();
	}

	public void setSelectionColoringMode( SelectionColoringMode selectionColoringMode, double brightnessNotSelected )
	{
		this.selectionColoringMode = selectionColoringMode;

		// ensure value between 0 and 1
		brightnessNotSelected = Math.min( 1.0, brightnessNotSelected );
		brightnessNotSelected = Math.max( 0.0, brightnessNotSelected );
		this.brightnessNotSelected = brightnessNotSelected;

		notifyColoringListeners();
	}

	public SelectionColoringMode getSelectionColoringMode()
	{
		return selectionColoringMode;
	}

	public void setSelectionColor( ARGBType selectionColor )
	{
		this.selectionColor = selectionColor;
		notifyColoringListeners();
	}

	public void setColoringModel( ColoringModel< T > coloringModel )
	{
		this.coloringModel = coloringModel;
		notifyColoringListeners();

		// chain event notification
		coloringModel.listeners().add( () -> de.embl.cba.tables.color.SelectionColoringModel.this.notifyColoringListeners() );
	}

	public ColoringModel< T > getColoringModel()
	{
		return coloringModel;
	}

	public void iterateSelectionMode()
	{
		final int selectionModeIndex = selectionColoringModes.indexOf( selectionColoringMode );

		if ( selectionModeIndex < selectionColoringModes.size() - 1 )
		{
			setSelectionColoringMode( selectionColoringModes.get( selectionModeIndex + 1 ) );
		}
		else
		{
			setSelectionColoringMode( selectionColoringModes.get( 0 ) );
		}
	}

	public SelectionModel< T > getSelectionModel()
	{
		return selectionModel;
	}

	public double getBrightnessNotSelected()
	{
		return brightnessNotSelected;
	}
}
