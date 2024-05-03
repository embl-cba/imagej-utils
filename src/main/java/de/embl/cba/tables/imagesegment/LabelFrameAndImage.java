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
package de.embl.cba.tables.imagesegment;

import de.embl.cba.tables.imagesegment.ImageSegment;

import java.util.Objects;

public class LabelFrameAndImage
{
	private final String image;
	private final double label;
	private final int frame;

	public LabelFrameAndImage( double label, int frame, String image )
	{
		this.label = label;
		this.frame = frame;
		this.image = image;

	}

	public LabelFrameAndImage( ImageSegment imageSegment )
	{
		this.image = imageSegment.imageId();
		this.label = imageSegment.labelId();
		this.frame = imageSegment.timePoint();
	}

	@Override
	public boolean equals( Object o )
	{
		if ( this == o ) return true;
		if ( o == null || getClass() != o.getClass() ) return false;
		de.embl.cba.tables.imagesegment.LabelFrameAndImage that = ( de.embl.cba.tables.imagesegment.LabelFrameAndImage ) o;
		return Double.compare( that.label, label ) == 0 &&
				frame == that.frame &&
				Objects.equals( image, that.image );
	}

	@Override
	public int hashCode()
	{
		return Objects.hash( label, image, frame );
	}

	public String getImage()
	{
		return image;
	}

	public double getLabel()
	{
		return label;
	}

	public int getFrame()
	{
		return frame;
	}
}
