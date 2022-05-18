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
package tests;

import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.sources.LazySpimSource;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestNonVolatileAccess
{
	//@Test
	public < R extends RealType< R > > void lazySpimSourceAccess()
	{
		final LazySpimSource< R > source = new LazySpimSource<>( "source",
				TestNonVolatileAccess.class.getResource( "../test-data/labels-ulong.xml" ).getFile() );

		final RandomAccessibleInterval< ? extends RealType< ? > > rai =
				BdvUtils.getRealTypeNonVolatileRandomAccessibleInterval( source, 0, 0 );

		final RandomAccess< ? extends RealType< ? > > randomAccess = rai.randomAccess();
 		randomAccess.setPosition( new long[]{10,10,10} );
		final RealType< ? > realType = randomAccess.get();
		System.out.println( realType.getRealDouble() );
		assertEquals( 2.0, realType.getRealDouble(), 0.0 );
	}

	public static void main( String[] args )
	{
		new TestNonVolatileAccess().lazySpimSourceAccess( );
	}

}
