/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2024 EMBL
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
import bdv.util.RandomAccessibleIntervalSource4D;
import de.embl.cba.bdv.utils.sources.ModifiableRandomAccessibleIntervalSource4D;
import de.embl.cba.bdv.utils.wrap.Wraps;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.HyperStackConverter;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.ArrayList;

public class TestImagePlusWrapAsSource4D
{

	// TODO: Write a proper test!

	public static < R extends RealType< R > & NativeType< R > >
	void main( String[] args )
	{
		// 5D
		final ImagePlus imagePlus01 = IJ.openImage(
				TestImagePlusWrapAsSource4D.class.getResource(
						"imagePlus-20x-20y-2c-5z-3t.zip" ).getFile() );

		final ArrayList< ModifiableRandomAccessibleIntervalSource4D< R > > wrap01 =
				Wraps.imagePlusAsSource4DChannelList( imagePlus01 );

		// Only one channel
		final ImagePlus imagePlusC1 = IJ.openImage(
				TestImagePlusWrapAsSource4D.class.getResource(
						"imagePlus-20x-20y-1c-5z-3t.zip" ).getFile() );

		final ArrayList< ModifiableRandomAccessibleIntervalSource4D< R > > wrapC1 =
				Wraps.imagePlusAsSource4DChannelList( imagePlusC1 );

		// Only one channel, only one time-point
		final ImagePlus imagePlusC1T1 = IJ.openImage(
				TestImagePlusWrapAsSource4D.class.getResource(
						"imagePlus-20x-20y-1c-5z-1t.zip" ).getFile() );

		final ArrayList< ModifiableRandomAccessibleIntervalSource4D< R > > wrapC1T1 =
				Wraps.imagePlusAsSource4DChannelList( imagePlusC1T1 );

		int a = 1;

	}
}
