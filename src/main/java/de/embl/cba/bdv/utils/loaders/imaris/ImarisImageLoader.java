package de.embl.cba.bdv.utils.loaders.imaris;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import bdv.AbstractViewerSetupImgLoader;
import bdv.ViewerImgLoader;
import bdv.cache.CacheControl;
import bdv.img.cache.CacheArrayLoader;
import bdv.img.cache.VolatileGlobalCellCache;
import bdv.img.hdf5.MipmapInfo;
import bdv.img.hdf5.ViewLevelId;

import bdv.img.imaris.HDF5AccessHack;
import bdv.img.imaris.IHDF5Access;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.generic.sequence.ImgLoaderHint;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.cache.volatiles.CacheHints;
import net.imglib2.cache.volatiles.LoadingStrategy;
import net.imglib2.img.NativeImg;
import net.imglib2.img.basictypeaccess.volatiles.VolatileAccess;
import net.imglib2.img.cell.AbstractCellImg;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.cell.CellImg;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.volatiles.VolatileUnsignedShortType;

public class ImarisImageLoader< T extends NativeType< T >, V extends Volatile< T > & NativeType< V > , A extends VolatileAccess > implements ViewerImgLoader
{
	private final DataTypes.DataType< T, V, A > dataType;

	private IHDF5Access hdf5Access;

	private final MipmapInfo mipmapInfo;

	private final long[][] mipmapDimensions;

	private final File hdf5File;

	private final AbstractSequenceDescription< ?, ?, ? > sequenceDescription;

	private VolatileGlobalCellCache cache;

	private CacheArrayLoader< A > loader;

	private final HashMap< Integer, ImarisImageLoader.SetupImgLoader > setupImgLoaders;

	public ImarisImageLoader(
			final DataTypes.DataType< T, V, A > dataType,
			final File hdf5File,
			final MipmapInfo mipmapInfo,
			final long[][] mipmapDimensions,
			final AbstractSequenceDescription< ?, ?, ? > sequenceDescription )
	{
		this.dataType = dataType;
		this.hdf5File = hdf5File;
		this.mipmapInfo = mipmapInfo;
		this.mipmapDimensions = mipmapDimensions;
		this.sequenceDescription = sequenceDescription;
		this.setupImgLoaders = new HashMap<>();
	}

	private boolean isOpen = false;

	private void open()
	{
		if ( !isOpen )
		{
			synchronized ( this )
			{
				if ( isOpen )
					return;
				isOpen = true;

				final IHDF5Reader hdf5Reader = HDF5Factory.openForReading( hdf5File );

				final List< ? extends BasicViewSetup > setups = sequenceDescription.getViewSetupsOrdered();

				final int maxNumLevels = mipmapInfo.getNumLevels();

				try
				{
					hdf5Access = new HDF5AccessHack( hdf5Reader );
				}
				catch ( final Exception e )
				{
					throw new RuntimeException( e );
				}
				loader = dataType.createArrayLoader( hdf5Access );
				cache = new VolatileGlobalCellCache( maxNumLevels, 1 );

				for ( final BasicViewSetup setup : setups )
				{
					final int setupId = setup.getId();
					setupImgLoaders.put( setupId, new ImarisImageLoader.SetupImgLoader( setupId ) );
				}
			}
		}
	}

	/**
	 * (Almost) create a {@link CellImg} backed by the cache. The created image
	 * needs a {@link NativeImg#setLinkedType(net.imglib2.type.Type) linked
	 * type} before it can be used. The type should be either
	 * {@link UnsignedShortType} and {@link VolatileUnsignedShortType}.
	 */
	protected < T extends NativeType< T > > AbstractCellImg< T, A, ?, ? > prepareCachedImage( final ViewLevelId id, final LoadingStrategy loadingStrategy, final T type )
	{
		open();
		final int timepointId = id.getTimePointId();
		final int setupId = id.getViewSetupId();
		final int level = id.getLevel();
		final long[] dimensions = mipmapDimensions[ level ];
		final int[] cellDimensions = mipmapInfo.getSubdivisions()[ level ];
		final CellGrid grid = new CellGrid( dimensions, cellDimensions );

		final int priority = mipmapInfo.getMaxLevel() - level;
		final CacheHints cacheHints = new CacheHints( loadingStrategy, priority, false );
		return cache.createImg( grid, timepointId, setupId, level, cacheHints, loader, type );
	}

	@Override
	public CacheControl getCacheControl()
	{
		open();
		return cache;
	}

	@Override
	public ImarisImageLoader.SetupImgLoader getSetupImgLoader( final int setupId )
	{
		open();
		return setupImgLoaders.get( setupId );
	}

	public class SetupImgLoader extends AbstractViewerSetupImgLoader< T, V >
	{
		private final int setupId;

		protected SetupImgLoader( final int setupId )
		{
			super( dataType.getType(), dataType.getVolatileType() );
			this.setupId = setupId;
		}

		@Override
		public RandomAccessibleInterval< T > getImage( final int timepointId, final int level, final ImgLoaderHint... hints )
		{
			final ViewLevelId id = new ViewLevelId( timepointId, setupId, level );
			return prepareCachedImage( id, LoadingStrategy.BLOCKING, dataType.getType() );
		}

		@Override
		public RandomAccessibleInterval< V > getVolatileImage( final int timepointId, final int level, final ImgLoaderHint... hints )
		{
			final ViewLevelId id = new ViewLevelId( timepointId, setupId, level );
			return prepareCachedImage( id, LoadingStrategy.BUDGETED, dataType.getVolatileType() );
		}

		@Override
		public double[][] getMipmapResolutions()
		{
			return mipmapInfo.getResolutions();
		}

		@Override
		public AffineTransform3D[] getMipmapTransforms()
		{
			return mipmapInfo.getTransforms();
		}

		@Override
		public int numMipmapLevels()
		{
			return mipmapInfo.getNumLevels();
		}
	}
}
