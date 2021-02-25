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
package de.embl.cba.tables.plot;

import bdv.TransformEventHandler2D;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import de.embl.cba.bdv.utils.lut.GlasbeyARGBLut;
import net.imglib2.*;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;

public class KDTreeRenderer<T extends NumericType<T>,P extends RealLocalizable >
{
	final private double radius;

	final private double squareRadius;

	final private double invSquareRadius;

	final private KDTree< T > tree;

	private final T type;

	public KDTreeRenderer( List< T > vals,
						   List< P > pts,
						   final double radius,
						   T type )
	{
		this.type = type;
		tree = new KDTree< T >( vals, pts );
		this.radius = radius;
		this.squareRadius = ( radius * radius );
		this.invSquareRadius = 1 / squareRadius;
	}

	public RealRandomAccessible<T> getRealRandomAccessible(
			final double searchDist,
			final DoubleUnaryOperator rbf )
	{
		RBFInterpolator.RBFInterpolatorFactory< T > interp = 
				new RBFInterpolator.RBFInterpolatorFactory< T >( 
						rbf, searchDist, true,
						type.copy() );

		return Views.interpolate( tree, interp );
	}
	
	public double intensity( final double squareDistance )
	{
		if ( squareDistance > squareRadius )
		{
			return 0;
		}
		else
		{
			return ( 1.0 - ( squareDistance * invSquareRadius ) );
		}
	}

	public static < T extends RealType< T > > void main( String[] args )
	{
		Interval interval = FinalInterval.createMinMax( 0, 0, 0, 100, 100, 0 );

		//int max = 10000;

		ARGBType argbType = new ARGBType( ARGBType.rgba( 255, 255, 255, 255 ));
		RandomPointsCreator< ARGBType > pointsCreator = new RandomPointsCreator( argbType, 1000, interval );

		ArrayList< ARGBType > values = pointsCreator.getFixedValueList();

		Random random = new Random();
		ArrayList< Integer > argbs = GlasbeyARGBLut.argbGlasbeyIndices();
		for ( ARGBType value : values )
		{
			value.set( argbs.get( random.nextInt( argbs.size() ) ) );
		}


		KDTreeRenderer< T, RealPoint > renderer = new KDTreeRenderer( values, pointsCreator.getPointList(), 0.5, new ARGBType( ARGBType.rgba( 100, 100, 100, 255 ) ) );


		RealRandomAccessible< T > img = renderer.getRealRandomAccessible( 1.0, renderer::intensity );

		// visualize
		BdvStackSource< T > bdvStackSource = BdvFunctions.show( img, interval, "Render points", BdvOptions.options().transformEventHandlerFactory( TransformEventHandler2D::new ) );


		//bdv.setDisplayRange( 0, 1.0 );

		// set the display range
		//bdv.getBdvHandle().getSetupAssignments().getMinMaxGroups().get( 0 ).setRange( 0, 1000.0 );
	}
}
