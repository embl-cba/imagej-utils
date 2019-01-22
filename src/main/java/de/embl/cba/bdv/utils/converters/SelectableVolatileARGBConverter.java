/**
 * License: GPL
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package de.embl.cba.bdv.utils.converters;

import bdv.util.Bdv;
import bdv.viewer.TimePointListener;
import ij.ImagePlus;
import net.imglib2.Volatile;
import net.imglib2.converter.Converter;
import net.imglib2.img.VirtualStackAdapter;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;

import java.util.*;

public class SelectableVolatileARGBConverter implements
		Converter< RealType, VolatileARGBType >, TimePointListener
{
	public static final ARGBType COLOR_SELECTED = new ARGBType( ARGBType.rgba( 255, 255, 0, 255 ) );
	public static final int BACKGROUND = 0;
	private Converter< RealType, VolatileARGBType > wrappedConverter;
	private Map< Integer, Set< Double > > selectedValues; // timepoint map
	private double brightnessNotSelected;
	private SelectionMode selectionMode;
	private ARGBType colorSelected;
	private int currentTimePoint;

	public enum SelectionMode
	{
		DimNotSelected,
		OnlyShowSelected,
		ColorSelectedBrightYellow,
		ColorSelectedBrightYellowAndDimNotSelected;
	}

	public SelectableVolatileARGBConverter( )
	{
		this( new RandomARGBConverter() );
	}

	public SelectableVolatileARGBConverter(
			Converter< RealType, VolatileARGBType > realARGBConverter )
	{
		wrappedConverter = realARGBConverter;
		selectedValues = null;
		currentTimePoint = 0;

		setSelectionMode( SelectionMode.DimNotSelected );
	}

	@Override
	public void convert( final RealType input, final VolatileARGBType output )
	{
		if ( input instanceof Volatile )
		{
			if ( ! ( ( Volatile ) input ).isValid() )
			{
				output.setValid( false );
				return;
			}
		}

		setOutputColor( input, output);

		output.setValid( true );
	}

	private void setOutputColor( final RealType input,
								 final VolatileARGBType output )
	{

		if ( input.getRealDouble() == BACKGROUND )
		{
			output.set( 0 );
			return;
		}

		wrappedConverter.convert( input, output );

		if ( isSelected( input ) &&  colorSelected != null )
		{
			output.set( colorSelected.get() );
		}
		else
		{
			output.get().mul( brightnessNotSelected);
		}

	}

	private boolean isSelected( final RealType input )
	{

		if ( selectedValues == null ) return true;

		if ( selectedValues.get( currentTimePoint ) == null ) return true;

		if ( selectedValues.get( currentTimePoint ).contains( input.getRealDouble() ) ) return true;

		return false;

	}


	public synchronized void addSelection( double value, int timepoint )
	{
		if ( selectedValues == null )
			selectedValues = new HashMap<>( );

		if ( selectedValues.get( timepoint ) == null )
			selectedValues.put( timepoint,  new HashSet<>( ) );

		selectedValues.get( timepoint ).add( value );
	}

	public synchronized void removeSelection( double value, int timepoint )
	{
		selectedValues.get( timepoint ).remove( value );
	}

	public synchronized void clearSelections( )
	{
		this.selectedValues = null;
	}

	public Map< Integer, Set< Double > > getSelections()
	{
		return selectedValues;
	}

	public void setBrightnessNotSelectedValues( final double brightnessNotSelected )
	{
		this.brightnessNotSelected = brightnessNotSelected;
	}

	public void setWrappedConverter( Converter< RealType, VolatileARGBType > converter )
	{
		this.wrappedConverter = converter;
	}

	public Converter< RealType, VolatileARGBType > getWrappedConverter()
	{
		return wrappedConverter;
	}

	public SelectionMode getSelectionMode()
	{
		return selectionMode;
	}

	public void setSelectionMode( SelectionMode selectionMode )
	{
		this.selectionMode = selectionMode;

		switch ( selectionMode )
		{
			case DimNotSelected:
				brightnessNotSelected = 0.2;
				colorSelected = null;
				break;
			case OnlyShowSelected:
				brightnessNotSelected = 0.0;
				colorSelected = null;
				break;
			case ColorSelectedBrightYellow:
				brightnessNotSelected = 1.0;
				colorSelected = COLOR_SELECTED;
				break;
			case ColorSelectedBrightYellowAndDimNotSelected:
				brightnessNotSelected = 0.2;
				colorSelected = COLOR_SELECTED;
				break;
		}

	}

	@Override
	public void timePointChanged( int timePointIndex )
	{
		currentTimePoint = timePointIndex;
	}
}
