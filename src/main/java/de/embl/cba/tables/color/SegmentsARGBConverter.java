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
package de.embl.cba.tables.color;

import de.embl.cba.tables.imagesegment.ImageSegment;
import de.embl.cba.tables.imagesegment.LabelFrameAndImage;
import net.imglib2.Volatile;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;

import java.util.Map;

public class SegmentsARGBConverter< T extends ImageSegment > implements LabelsARGBConverter
{
	private final Map< LabelFrameAndImage, T > labelFrameAndImageToSegment;
	private final String imageId;
	private final ColoringModel< T > coloringModel;
	private ARGBType singleColor;

	private int frame;

	public SegmentsARGBConverter(
			Map< LabelFrameAndImage, T > labelFrameAndImageToSegment,
			String imageId,
			ColoringModel coloringModel )
	{
		this.labelFrameAndImageToSegment = labelFrameAndImageToSegment;
		this.imageId = imageId;
		this.coloringModel = coloringModel;
		this.singleColor = null;
		this.frame = 0;
	}

	@Override
	public void convert( RealType label, VolatileARGBType color )
	{
		if ( label instanceof Volatile )
		{
			if ( ! ( ( Volatile ) label ).isValid() )
			{
				color.set( 0 );
				color.setValid( false );
				return;
			}
		}

		if ( label.getRealDouble() == 0 )
		{
			color.setValid( true );
			color.set( 0 );
			return;
		}

		if ( singleColor != null )
		{
			color.setValid( true );
			color.set( singleColor.get() );
			return;
		}

		final LabelFrameAndImage labelFrameAndImage =
				new LabelFrameAndImage( label.getRealDouble(), frame, imageId  );

		final T imageSegment = labelFrameAndImageToSegment.get( labelFrameAndImage );

		if ( imageSegment == null )
		{
			color.set( 0 );
			color.setValid( true );
		}
		else
		{
			coloringModel.convert( imageSegment, color.get() );

			final int alpha = ARGBType.alpha( color.get().get() );
			if( alpha < 255 )
				color.mul( alpha / 255.0 );

			color.setValid( true );
		}
	}

	@Override
	public void timePointChanged( int timePointIndex )
	{
		this.frame = timePointIndex;
	}

	@Override
	public void setSingleColor( ARGBType argbType )
	{
		singleColor = argbType;
	}
}
