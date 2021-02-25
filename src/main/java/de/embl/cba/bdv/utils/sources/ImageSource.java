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
package de.embl.cba.bdv.utils.sources;

import bdv.util.BdvSource;
import mpicbg.spim.data.SpimData;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;

import static de.embl.cba.transforms.utils.Transforms.createBoundingIntervalAfterTransformation;

public class ImageSource
{
	private final String filePath;
	private final BdvSource bdvSource;
	private final SpimData spimData;

	public ImageSource( String filePath, BdvSource bdvSource, SpimData spimData )
	{
		this.filePath = filePath;
		this.bdvSource = bdvSource;
		this.spimData = spimData;
	}

	public String getName()
	{
		return filePath.split( "\\." )[ 0 ];
	}

	public String getFilePath()
	{
		return filePath;
	}

	public FinalInterval getInterval()
	{
		final AffineTransform3D affineTransform3D = spimData.getViewRegistrations().getViewRegistration( 0, 0 ).getModel();
		RandomAccessibleInterval< ? > image = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader( 0 ).getImage( 0 );

		final FinalInterval boundingIntervalAfterTransformation = createBoundingIntervalAfterTransformation( image, affineTransform3D );

		return boundingIntervalAfterTransformation;
	}

	public SpimData getSpimData()
	{
		return spimData;
	}


}
