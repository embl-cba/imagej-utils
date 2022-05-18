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
package de.embl.cba.tables.imagesegment;

import de.embl.cba.tables.imagesegment.DefaultImageSegment;
import net.imglib2.FinalRealInterval;

public class DefaultImageSegmentBuilder
{
	private String imageId = getDefaultImageIdName();
	private double label = getDefaultLabel();
	private int timePoint = getDefaultTimePoint();
	private double x = getDefaultX();
	private double y = getDefaultY();
	private double z = getDefaultZ();
	private FinalRealInterval boundingBox = getDefaultBoundingBox();

	public DefaultImageSegment build()
	{
		final DefaultImageSegment defaultImageSegment
				= new DefaultImageSegment(
						imageId,
						label,
						timePoint ,
						x, y, z,
						boundingBox );

		return defaultImageSegment;
	}

	public de.embl.cba.tables.imagesegment.DefaultImageSegmentBuilder setImageId( String imageSetName )
	{
		this.imageId = imageSetName;
		return this;
	}

	public de.embl.cba.tables.imagesegment.DefaultImageSegmentBuilder setLabel( double label )
	{
		this.label = label;
		return this;
	}

	public de.embl.cba.tables.imagesegment.DefaultImageSegmentBuilder setTimePoint( int timePoint )
	{
		this.timePoint = timePoint;
		return this;
	}


	public de.embl.cba.tables.imagesegment.DefaultImageSegmentBuilder setBoundingBox( FinalRealInterval boundingBox )
	{
		this.boundingBox = boundingBox;
		return this;
	}

	public de.embl.cba.tables.imagesegment.DefaultImageSegmentBuilder setX( double x )
	{
		this.x = x;
		return this;
	}

	public de.embl.cba.tables.imagesegment.DefaultImageSegmentBuilder setY( double y )
	{
		this.y = y;
		return this;
	}

	public de.embl.cba.tables.imagesegment.DefaultImageSegmentBuilder setZ( double z )
	{
		this.z = z;
		return this;
	}

	public static String getDefaultImageIdName()
	{
		return "LabelImage";
	}

	public static double getDefaultLabel()
	{
		return 1;
	}

	public static int getDefaultTimePoint()
	{
		return 0;
	}

	public static double getDefaultX()
	{
		return 0.0;
	}

	public static double getDefaultY()
	{
		return 0.0;
	}

	public static double getDefaultZ()
	{
		return 0.0;
	}

	public static FinalRealInterval getDefaultBoundingBox()
	{
		return null;
	}
}
