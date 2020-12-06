package de.embl.cba.tables.plot;

import net.imglib2.*;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.neighborsearch.RadiusNeighborSearch;
import net.imglib2.neighborsearch.RadiusNeighborSearchOnKDTree;
import net.imglib2.type.numeric.NumericType;

import java.util.function.DoubleUnaryOperator;

/**
 * A radial basis function interpolator
 * @author John Bogovic
 *
 * @param <T>
 */
public class RBFInterpolator< T extends NumericType<T> > extends RealPoint implements RealRandomAccess< T >
{
	final static protected double minThreshold = Double.MIN_VALUE * 1000;

	final protected RadiusNeighborSearch< T > search;

	final protected KDTree< T > tree;

	final T type;

	double searchRadius;

	final DoubleUnaryOperator intensityComputer;  // from squaredDistance to weight

	final boolean normalize;

	public RBFInterpolator(
			final KDTree< T > tree,
			final DoubleUnaryOperator intensityComputer,
			final double searchRadius,
			final boolean normalize,
			T type )
	{
		super( tree.numDimensions() );

		this.intensityComputer = intensityComputer;
		this.tree = tree;
		this.search = new RadiusNeighborSearchOnKDTree< T >( tree );
		this.normalize = normalize;
		this.searchRadius = searchRadius;

		this.type = type;
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
		search.search( this, searchRadius, true );

		T copy = type.copy();

		if ( search.numNeighbors() > 0 )
		{
			final Sampler< T > sampler = search.getSampler( 0 );
			final T value = sampler.get();
			final double weight = intensityComputer.applyAsDouble( search.getSquareDistance( 0 ) );
			copy.set( value );
			copy.mul( weight );
			return copy;
		}
		else
		{
			copy.setZero();
			return copy;
		}
	}

	@Override
	public RBFInterpolator< T > copy()
	{
		return new RBFInterpolator< T >( tree, intensityComputer, searchRadius, normalize, type );
	}

	@Override
	public RBFInterpolator< T > copyRealRandomAccess()
	{
		return copy();
	}

	public static class RBFInterpolatorFactory< T extends NumericType<T> > implements InterpolatorFactory< T, KDTree< T > >
	{
		final double searchRad;
		final DoubleUnaryOperator intensityComputer;
		final boolean normalize;
		T defaultValue;

		public RBFInterpolatorFactory( 
				final DoubleUnaryOperator intensityComputer,
				final double sr, 
				final boolean normalize, 
				T defaultValue )
		{
			this.searchRad = sr;
			this.intensityComputer = intensityComputer;
			this.normalize = normalize;
			this.defaultValue = defaultValue;
		}

		@Override
		public RBFInterpolator<T> create( final KDTree< T > tree )
		{
			return new RBFInterpolator<T>( tree, intensityComputer, searchRad, normalize, defaultValue );
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
