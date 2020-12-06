package de.embl.cba.tables.plot;

import bdv.util.BdvFunctions;
import bdv.util.BdvStackSource;
import net.imglib2.*;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class KDTreeRenderer<T extends NumericType<T>,P extends RealLocalizable >
{
	final double radius;

	final double squareRadius;

	final double invSquareRadius;

	final KDTree< T > tree;

	private final T defaultValue;

	public KDTreeRenderer( List< T > vals,
						   List< P > pts,
						   final double radius,
						   T defaultValue )
	{
		this.defaultValue = defaultValue;
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
						defaultValue.copy() );

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
		Interval interval = FinalInterval.createMinMax( 0, 0, 0, 10, 10, 0 );

		//int max = 10000;

		ARGBType argbType = new ARGBType( ARGBType.rgba( 255, 255, 255, 255 ));
		RandomPointsCreator< ARGBType > pointsCreator = new RandomPointsCreator( argbType, 10, interval );

		KDTreeRenderer< T, RealPoint > renderer = new KDTreeRenderer( pointsCreator.getValueList(), pointsCreator.getPointList(), 0.5, new ARGBType( ARGBType.rgba( 100, 100, 100, 255 ) ) );

		RealRandomAccessible< T > img = renderer.getRealRandomAccessible( 1.0, renderer::intensity );

		// visualize
		BdvStackSource< T > bdv = BdvFunctions.show( img, interval, "Render points" );

		//bdv.setDisplayRange( 0, 1.0 );

		// set the display range
		//bdv.getBdvHandle().getSetupAssignments().getMinMaxGroups().get( 0 ).setRange( 0, 1000.0 );
	}
}
