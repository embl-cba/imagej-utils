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

import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import mpicbg.spim.data.generic.sequence.BasicMultiResolutionImgLoader;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BdvHdf5ULongLoadingTest
{
	@Test
	public < R extends RealType< R > > void test() throws SpimDataException
	{
		final SpimData spimData = new XmlIoSpimData().load( BdvHdf5ULongLoadingTest.class.getResource( "../test-data/labels-ulong.xml" ).getFile() );
		final BasicMultiResolutionImgLoader imgLoader = ( BasicMultiResolutionImgLoader ) spimData.getSequenceDescription().getImgLoader();
		final RandomAccessibleInterval< ? > rai = imgLoader.getSetupImgLoader( 0 ).getImage( 0, 0 );
		final RandomAccess< ? > randomAccess = rai.randomAccess();
		randomAccess.setPosition( new long[]{10,10,10} );
		final UnsignedLongType longType = ( UnsignedLongType ) randomAccess.get();
		System.out.println( longType.get() );
		//System.out.println( realType.getRealDouble() );
		//assertEquals( 2.0, realType.getRealDouble(), 0.0 );
	}
}
