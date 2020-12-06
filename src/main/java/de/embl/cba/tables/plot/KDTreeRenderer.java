package de.embl.cba.tables.plot;

import bdv.util.BdvFunctions;
import bdv.util.BdvStackSource;
import net.imglib2.*;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class KDTreeRenderer<T extends RealType<T>,P extends RealLocalizable >
{

	final double radius;

	final double searchDistSqr;

	final double invSquareSearchDistance;

	final double value;

	final KDTree< T > tree;
	
	public KDTreeRenderer( List<T> vals, List<P> pts,
						   final double radius,
						   final double value )
	{
		tree = new KDTree< T >( vals, pts );
		this.value = value;

		this.radius = radius;
		this.searchDistSqr = ( radius * radius );
		this.invSquareSearchDistance = 1 / searchDistSqr;
	}

	public RealRandomAccessible<T> getRealRandomAccessible(
			final double searchDist,
			final DoubleUnaryOperator rbf )
	{
		RBFInterpolator.RBFInterpolatorFactory< T > interp = 
				new RBFInterpolator.RBFInterpolatorFactory< T >( 
						rbf, searchDist, false,
						tree.firstElement().copy() );

		return Views.interpolate( tree, interp );
	}
	
	public double rbfRadius( final double rsqr )
	{
		if( rsqr > searchDistSqr )
			return 0;
		else
		{
			return value * ( 1 - ( rsqr * invSquareSearchDistance )); 
		}
	}

	public static < T extends RealType< T > > void main( String[] args )
	{
		Interval interval = FinalInterval.createMinMax( 0, 0, 0 , 10, 10, 0 );
		RandomPointsCreator< DoubleType > pointsCreator = new RandomPointsCreator<>( new DoubleType(), 100, interval );

		KDTreeRenderer< T, RealPoint > renderer = new KDTreeRenderer( pointsCreator.getFixedValueList(), pointsCreator.getRandomPointList(), 0.5, 10.0 );

		RealRandomAccessible< T > img = renderer.getRealRandomAccessible( 0.5, renderer::rbfRadius );

		// visualize
		BdvStackSource< T > bdv = BdvFunctions.show( img, interval, "Render points" );

		// set the display range
		bdv.getBdvHandle().getSetupAssignments().getMinMaxGroups().get( 0 ).setRange( 0, 10.0 );
	}
}
