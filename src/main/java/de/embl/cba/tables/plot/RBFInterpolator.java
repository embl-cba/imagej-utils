package de.embl.cba.tables.plot;

import net.imglib2.*;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.neighborsearch.RadiusNeighborSearch;
import net.imglib2.neighborsearch.RadiusNeighborSearchOnKDTree;
import net.imglib2.type.numeric.RealType;

import java.util.function.DoubleUnaryOperator;

/**
 * A radial basis function interpolator
 * @author John Bogovic
 *
 * @param <T>
 */
public class RBFInterpolator<T extends RealType<T> > extends RealPoint implements RealRandomAccess< T >
{
	final static protected double minThreshold = Double.MIN_VALUE * 1000;

	final protected RadiusNeighborSearch< T > search;

	final protected KDTree< T > tree;

	final T value;

	double searchRadius;

	final DoubleUnaryOperator rbf;  // from squaredDistance to weight

	final boolean normalize;

	public RBFInterpolator(
			final KDTree< T > tree,
			final DoubleUnaryOperator rbf,
			final double searchRadius,
			final boolean normalize,
			T t )
	{
		super( tree.numDimensions() );

		this.rbf = rbf;
		this.tree = tree;
		this.search = new RadiusNeighborSearchOnKDTree< T >( tree );
		this.normalize = normalize;
		this.searchRadius = searchRadius;

		this.value = t.copy();
	}
	
	public void setRadius( final double radius )
	{
		this.searchRadius = radius;
	}

	public void increaseRadius( final double amount )
	{
		this.searchRadius += amount;
	}

	public void decreaseRadius( final double amount )
	{
		this.searchRadius -= amount;
	}

	@Override
	public T get()
	{
		search.search( this, searchRadius, false );

		if ( search.numNeighbors() == 0 )
			value.setZero();
		else
		{
			double sumIntensity = 0;
			double sumWeights = 0;

			for ( int i = 0; i < search.numNeighbors(); ++i )
			{
				final Sampler< T > sampler = search.getSampler( i );

				if ( sampler == null )
					break;

				final T t = sampler.get();
				final double weight = rbf.applyAsDouble( search.getSquareDistance( i ) );

				if( normalize )
					sumWeights += weight;

				sumIntensity += t.getRealDouble() * weight;
			}

			if( normalize )
				value.setReal( sumIntensity / sumWeights );
			else
				value.setReal( sumIntensity );
		}

		return value;
	}

	@Override
	public RBFInterpolator< T > copy()
	{
		return new RBFInterpolator< T >( tree,
				rbf, searchRadius, normalize, value );
	}

	@Override
	public RBFInterpolator< T > copyRealRandomAccess()
	{
		return copy();
	}

	public static class RBFInterpolatorFactory<T extends RealType<T> > implements InterpolatorFactory< T, KDTree< T > >
	{
		final double searchRad;
		final DoubleUnaryOperator rbf;
		final boolean normalize;
		T val;

		public RBFInterpolatorFactory( 
				final DoubleUnaryOperator rbf,
				final double sr, 
				final boolean normalize, 
				T t )
		{
			this.searchRad = sr;
			this.rbf = rbf;
			this.normalize = normalize;
			this.val = t;
		}

		@Override
		public RBFInterpolator<T> create( final KDTree< T > tree )
		{
			return new RBFInterpolator<T>( tree, rbf, searchRad, false, val );
		}

		@Override
		public RealRandomAccess< T > create(
				final KDTree< T > tree,
				final RealInterval interval )
		{
			return create( tree );
		}
	}
}
