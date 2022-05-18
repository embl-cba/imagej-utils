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
package de.embl.cba.bdv.utils.converters;

import de.embl.cba.bdv.utils.lut.Luts;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;

import java.util.ArrayList;
import java.util.function.Function;

public class MappingRandomARGBConverter extends RandomARGBConverter
{
	final Function< Double, ? extends Object > mappingFn;
	final private ArrayList< Object > uniqueObjectsList;

	public MappingRandomARGBConverter( Function< Double, ? extends Object > mappingFn )
	{
		this( mappingFn, Luts.GLASBEY );
	}

	public MappingRandomARGBConverter( Function< Double, ? extends Object > mappingFn, byte[][] lut )
	{
		super( lut );
		this.mappingFn = mappingFn;
		this.uniqueObjectsList = new ArrayList<>(  );
	}

	@Override
	public void convert( RealType realType, VolatileARGBType volatileARGBType )
	{
		Object object = mappingFn.apply( realType.getRealDouble() );

		if ( object == null )
		{
			volatileARGBType.set( 0 );
			return;
		}

		if( ! uniqueObjectsList.contains( object ) ) uniqueObjectsList.add( object );


		final double random = createRandom( uniqueObjectsList.indexOf( object ) + 1 );
		final byte lutIndex = (byte) ( 255.0 * random );
		volatileARGBType.set( Luts.getARGBIndex( lutIndex, lut ) );
	}

	public Function< Double, ? extends Object > getMappingFn()
	{
		return mappingFn;
	}

}
