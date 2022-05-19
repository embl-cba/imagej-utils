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

import de.embl.cba.tables.imagesegment.ImageSegment;
import net.imglib2.FinalRealInterval;

public class DefaultImageSegment implements ImageSegment
{
	private final double[] position;
	private final String imageId;
	private final double labelId;
	private final int timePoint;

	private float[] mesh;
	private FinalRealInterval boundingBox;

	public DefaultImageSegment(
			String imageId,
			double labelId,
			int timePoint,
			double x,
			double y,
			double z,
			FinalRealInterval boundingBox )
	{
		this.imageId = imageId;
		this.labelId = labelId;
		this.timePoint = timePoint;
		this.position = new double[]{ x, y, z };
		this.boundingBox = boundingBox;
	}

	@Override
	public String imageId()
	{
		return imageId;
	}

	@Override
	public double labelId()
	{
		return labelId;
	}

	@Override
	public int timePoint()
	{
		return timePoint;
	}

	@Override
	public FinalRealInterval boundingBox()
	{
		return boundingBox;
	}

	@Override
	public void setBoundingBox( FinalRealInterval boundingBox )
	{
		this.boundingBox = boundingBox;
	}

	@Override
	public float[] getMesh()
	{
		return mesh;
	}

	@Override
	public void setMesh( float[] mesh )
	{
		this.mesh = mesh;
	}

	@Override
	public void localize( float[] position )
	{
		for ( int d = 0; d < position.length; d++ )
		{
			position[ d ] = (float) this.position[ d ];
		}
	}

	@Override
	public void localize( double[] position )
	{
		for ( int d = 0; d < position.length; d++ )
		{
			position[ d ] = this.position[ d ];
		}
	}

	@Override
	public float getFloatPosition( int d )
	{
		return (float) position[ d ];
	}

	@Override
	public double getDoublePosition( int d )
	{
		return position[ d ];
	}

	@Override
	public int numDimensions()
	{
		return position.length;
	}

}
