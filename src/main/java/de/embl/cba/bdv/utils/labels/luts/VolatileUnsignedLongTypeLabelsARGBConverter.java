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
package de.embl.cba.bdv.utils.labels.luts;

import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.type.volatiles.VolatileUnsignedLongType;

import java.util.HashMap;
import java.util.Map;

/**
 * Conversion logic adapted from BigCat Viewer.
 */
public class VolatileUnsignedLongTypeLabelsARGBConverter implements Converter< VolatileUnsignedLongType, VolatileARGBType >
{
    private long seed = 50;
    private Map< Long, Integer > lut = new HashMap<>();
	private boolean isLabelSelected = false;
	private long selectedLabel;

	@Override
	public void convert( final VolatileUnsignedLongType input, final VolatileARGBType output )
	{
		if ( input.isValid() )
		{
			double x = input.getRealDouble();
			long lx = ( long ) x;

			LabelUtils.setOutput( output, x, lx, isLabelSelected, selectedLabel, lut, seed );
		}
		else
		{
			output.setValid( false );
		}
	}


	public void incrementSeed()
	{
		seed++;
		lut = new HashMap<>();
	}

	public void select( long i )
	{
		isLabelSelected = true;
		selectedLabel = i;
	}

	public void selectNone()
	{
		isLabelSelected = false;
	}


}
