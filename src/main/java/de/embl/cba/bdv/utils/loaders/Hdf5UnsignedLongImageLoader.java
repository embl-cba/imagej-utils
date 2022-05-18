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
package de.embl.cba.bdv.utils.loaders;

import bdv.AbstractViewerSetupImgLoader;
import bdv.ViewerImgLoader;
import bdv.img.cache.VolatileGlobalCellCache;
import bdv.img.hdf5.*;
import bdv.util.ConstantRandomAccessible;
import bdv.util.MipmapTransforms;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.generic.sequence.ImgLoaderHint;
import mpicbg.spim.data.generic.sequence.ImgLoaderHints;
import mpicbg.spim.data.sequence.*;
import net.imglib2.*;
import net.imglib2.cache.queue.BlockingFetchQueues;
import net.imglib2.cache.queue.FetcherThreads;
import net.imglib2.cache.volatiles.CacheHints;
import net.imglib2.cache.volatiles.LoadingStrategy;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.NativeImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.cell.CellImg;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.type.volatiles.VolatileUnsignedLongType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static bdv.img.hdf5.Util.getResolutionsPath;
import static bdv.img.hdf5.Util.getSubdivisionsPath;


public class Hdf5UnsignedLongImageLoader implements ViewerImgLoader, MultiResolutionImgLoader
{
	protected File hdf5File;

	/**
	 * The {@link Hdf5ImageLoader} can be constructed with an existing
	 * {@link IHDF5Reader} which if non-null will be used instead of creating a
	 * new one on {@link #hdf5File}.
	 *
	 * <p>
	 * <em>Note that {@link #close()} will not close the existingHdf5Reader!</em>
	 */
	protected IHDF5Reader existingHdf5Reader;

	protected IHDF5UnsignedLongAccess hdf5Access;

	protected VolatileGlobalCellCache cache;

	protected FetcherThreads fetchers;

	protected Hdf5VolatileUnsignedLongArrayLoader longLoader;

	/**
	 * Maps setup id to {@link SetupImgLoader}.
	 */
	protected final HashMap< Integer, SetupImgLoader > setupImgLoaders;

	/**
	 * List of partitions if the dataset is split across several files
	 */
	protected final ArrayList< Partition > partitions;

	protected int maxNumLevels;

	/**
	 * Maps {@link ViewLevelId} (timepoint, setup, level) to
	 * {@link DimsAndExistence}. Every entry is either null or the existence and
	 * dimensions of one image. This is filled in when an image is loaded for
	 * the first time.
	 */
	protected final HashMap< ViewLevelId, DimsAndExistence > cachedDimsAndExistence;

	protected final AbstractSequenceDescription< ?, ?, ? > sequenceDescription;

	/**
	 *
	 * @param hdf5File
	 * @param hdf5Partitions
	 * @param sequenceDescription
	 *            the {@link AbstractSequenceDescription}. When loading images,
	 *            this may be used to retrieve additional information for a
	 *            {@link ViewId}, such as setup name, {@link Angle},
	 *            {@link Channel}, etc.
	 */
	public Hdf5UnsignedLongImageLoader( final File hdf5File, final ArrayList< Partition > hdf5Partitions, final AbstractSequenceDescription< ?, ?, ? > sequenceDescription )
	{
		this( hdf5File, hdf5Partitions, sequenceDescription, true );
	}

	public Hdf5UnsignedLongImageLoader( final File hdf5File, final ArrayList< Partition > hdf5Partitions, final AbstractSequenceDescription< ?, ?, ? > sequenceDescription, final boolean doOpen )
	{
		this( hdf5File, null, hdf5Partitions, sequenceDescription, doOpen );
	}

	protected Hdf5UnsignedLongImageLoader( final File hdf5File, final IHDF5Reader existingHdf5Reader, final ArrayList< Partition > hdf5Partitions, final AbstractSequenceDescription< ?, ?, ? > sequenceDescription, final boolean doOpen )
	{
		this.existingHdf5Reader = existingHdf5Reader;
		this.hdf5File = hdf5File;
		setupImgLoaders = new HashMap<>();
		cachedDimsAndExistence = new HashMap<>();
		this.sequenceDescription = sequenceDescription;
		partitions = new ArrayList<>();
		if ( hdf5Partitions != null )
			partitions.addAll( hdf5Partitions );
		if ( doOpen )
			open();
	}

	private boolean isOpen = false;

	private void open()
	{
		if ( ! isOpen )
		{
			synchronized ( this )
			{
				if ( isOpen )
					return;
				isOpen = true;

				final IHDF5Reader hdf5Reader = ( existingHdf5Reader != null ) ? existingHdf5Reader : HDF5Factory.openForReading( hdf5File );

				maxNumLevels = 0;
				final List< ? extends BasicViewSetup > setups = sequenceDescription.getViewSetupsOrdered();
				for ( final BasicViewSetup setup : setups )
				{
					final int setupId = setup.getId();

					final double[][] resolutions = hdf5Reader.readDoubleMatrix( getResolutionsPath( setupId ) );
					final AffineTransform3D[] transforms = new AffineTransform3D[ resolutions.length ];
					for ( int level = 0; level < resolutions.length; level++ )
						transforms[ level ] = MipmapTransforms.getMipmapTransformDefault( resolutions[ level ] );
					final int[][] subdivisions = hdf5Reader.readIntMatrix( getSubdivisionsPath( setupId ) );

					if ( resolutions.length > maxNumLevels )
						maxNumLevels = resolutions.length;

					setupImgLoaders.put( setupId, new SetupImgLoader( setupId, new MipmapInfo( resolutions, transforms, subdivisions ) ) );
				}

				cachedDimsAndExistence.clear();

				try
				{
					hdf5Access = new HDF5UnsignedLongAccessHack( hdf5Reader );
				}
				catch ( final Exception e )
				{
					e.printStackTrace();
					hdf5Access = null;
					// TODO: below is not public
					//hdf5Access = new HDF5Access( hdf5Reader );
				}


				longLoader = new Hdf5VolatileUnsignedLongArrayLoader( hdf5Access );

				final BlockingFetchQueues< Callable< ? > > queue = new BlockingFetchQueues( maxNumLevels, 1 );
				fetchers = new FetcherThreads( queue, 1 );
				cache = new VolatileGlobalCellCache( queue );
			}
		}
	}

	/**
	 * Clear the cache and close the hdf5 file. Images that were obtained from
	 * this loaders before {@link #close()} will stop working. Requesting images
	 * after {@link #close()} will cause the hdf5 file to be reopened (with a
	 * new cache).
	 */
	public void close()
	{
		if ( isOpen )
		{
			synchronized ( this )
			{
				if ( !isOpen )
					return;
				isOpen = false;

				cache.clearCache();
				hdf5Access.closeAllDataSets();

				// only close reader if we constructed it ourselves
				if ( existingHdf5Reader == null )
					hdf5Access.close();
			}
		}
	}

	public void initCachedDimensionsFromHdf5( final boolean background )
	{
		open();
		final long t0 = System.currentTimeMillis();
		final List< TimePoint > timepoints = sequenceDescription.getTimePoints().getTimePointsOrdered();
		final List< ? extends BasicViewSetup > setups = sequenceDescription.getViewSetupsOrdered();
		for ( final TimePoint timepoint : timepoints )
		{
			final int t = timepoint.getId();
			for ( final BasicViewSetup setup : setups )
			{
				final int s = setup.getId();
				final int numLevels = getSetupImgLoader( s ).numMipmapLevels();
				for ( int l = 0; l < numLevels; ++l )
					getDimsAndExistence( new ViewLevelId( t, s, l ) );
			}
			if ( background )
				synchronized ( this )
				{
					try
					{
						wait( 100 );
					}
					catch ( final InterruptedException e )
					{}
				}
		}
		final long t1 = System.currentTimeMillis() - t0;
		System.out.println( "initCachedDimensionsFromHdf5 : " + t1 + " ms" );
	}

	public File getHdf5File()
	{
		return hdf5File;
	}

	public ArrayList< Partition > getPartitions()
	{
		return partitions;
	}

	@Override
	public VolatileGlobalCellCache getCacheControl()
	{
		open();
		return cache;
	}

	public Hdf5VolatileUnsignedLongArrayLoader getLongArrayLoader()
	{
		open();
		return longLoader;
	}

	/**
	 * Checks whether the given image data is present in the hdf5. Missing data
	 * may be caused by missing partition files
	 *
	 * @return true, if the given image data is present.
	 */
	public boolean existsImageData( final ViewLevelId id )
	{
		return getDimsAndExistence( id ).exists();
	}

	public DimsAndExistence getDimsAndExistence( final ViewLevelId id )
	{
		open();
		DimsAndExistence dims = cachedDimsAndExistence.get( id );
		if ( dims == null )
		{
			// pause Fetcher threads for 5 ms. There will be more calls to
			// getImageDimension() because this happens when a timepoint is
			// loaded, and all setups for the timepoint are loaded then. We
			// don't want to interleave this with block loading operations.
			fetchers.pauseFor( 5 );
			dims = hdf5Access.getDimsAndExistence( id );
			cachedDimsAndExistence.put( id, dims );
		}
		return dims;
	}

	public void printMipmapInfo()
	{
		open();
		for ( final BasicViewSetup setup : sequenceDescription.getViewSetupsOrdered() )
		{
			final int setupId = setup.getId();
			System.out.println( "setup " + setupId );
			final MipmapInfo mipmapInfo = getSetupImgLoader( setupId ).getMipmapInfo();
			final double[][] reslevels = mipmapInfo.getResolutions();
			final int[][] subdiv = mipmapInfo.getSubdivisions();
			final int numLevels = mipmapInfo.getNumLevels();
			System.out.println( "    resolutions:");
			for ( int level = 0; level < numLevels; ++level )
			{
				final double[] res = reslevels[ level ];
				System.out.println( "    " + level + ": " + net.imglib2.util.Util.printCoordinates( res ) );
			}
			System.out.println( "    subdivisions:");
			for ( int level = 0; level < numLevels; ++level )
			{
				final int[] res = subdiv[ level ];
				System.out.println( "    " + level + ": " + net.imglib2.util.Util.printCoordinates( res ) );
			}
			System.out.println( "    level sizes:" );
			final int timepointId = sequenceDescription.getTimePoints().getTimePointsOrdered().get( 0 ).getId();
			for ( int level = 0; level < numLevels; ++level )
			{
				final DimsAndExistence dims = getDimsAndExistence( new ViewLevelId( timepointId, setupId, level ) );
				final long[] dimensions = dims.getDimensions();
				System.out.println( "    " + level + ": " + net.imglib2.util.Util.printCoordinates( dimensions ) );
			}
		}
	}

	/**
	 * normalize img to 0...1
	 */
	protected static void normalize( final IterableInterval< FloatType > img )
	{
		float currentMax = img.firstElement().get();
		float currentMin = currentMax;
		for ( final FloatType t : img )
		{
			final float f = t.get();
			if ( f > currentMax )
				currentMax = f;
			else if ( f < currentMin )
				currentMin = f;
		}

		final float scale = ( float ) ( 1.0 / ( currentMax - currentMin ) );
		for ( final FloatType t : img )
			t.set( ( t.get() - currentMin ) * scale );
	}

	@Override
	public SetupImgLoader getSetupImgLoader( final int setupId )
	{
		open();
		return setupImgLoaders.get( setupId );
	}

	public class SetupImgLoader extends AbstractViewerSetupImgLoader< UnsignedLongType, VolatileUnsignedLongType > implements MultiResolutionSetupImgLoader< UnsignedLongType >
	{
		private final int setupId;

		/**
		 * Description of available mipmap levels for the setup. Contains for
		 * each mipmap level, the subsampling factors and subdivision block
		 * sizes.
		 */
		private final MipmapInfo mipmapInfo;

		protected SetupImgLoader( final int setupId, final MipmapInfo mipmapInfo  )
		{
			super( new UnsignedLongType(), new VolatileUnsignedLongType() );
			this.setupId = setupId;
			this.mipmapInfo = mipmapInfo;
		}

		private RandomAccessibleInterval< UnsignedLongType > loadImageCompletely( final int timepointId, final int level )
		{
			open();

			final ViewLevelId id = new ViewLevelId( timepointId, setupId, level );
			if ( ! existsImageData( id ) )
			{
				System.err.println(	String.format(
						"image data for timepoint %d setup %d level %d could not be found. Partition file missing?",
						id.getTimePointId(), id.getViewSetupId(), id.getLevel() ) );
				return getMissingDataImage( id, type );
			}

			Img< UnsignedLongType > img = null;
			final DimsAndExistence dimsAndExistence = getDimsAndExistence( new ViewLevelId( timepointId, setupId, level ) );
			final long[] dimsLong = dimsAndExistence.exists() ? dimsAndExistence.getDimensions() : null;
			final int n = dimsLong.length;
			final int[] dimsInt = new int[ n ];
			final long[] min = new long[ n ];
			if ( Intervals.numElements( new FinalDimensions( dimsLong ) ) <= Integer.MAX_VALUE )
			{
				// use ArrayImg
				for ( int d = 0; d < dimsInt.length; ++d )
					dimsInt[ d ] = ( int ) dimsLong[ d ];
				long[] data = null;
				try
				{
					data = hdf5Access.readLongMDArrayBlockWithOffset( timepointId, setupId, level, dimsInt, min );
				}
				catch ( final InterruptedException e )
				{}
				img = ArrayImgs.unsignedLongs( data, dimsLong );
			}
			else
			{
				final int[] cellDimensions = computeCellDimensions(
						dimsLong,
						mipmapInfo.getSubdivisions()[ level ] );
				final CellImgFactory< UnsignedLongType > factory = new CellImgFactory<>( type, cellDimensions );
				@SuppressWarnings( "unchecked" )
				final CellImg< UnsignedLongType, LongArray > cellImg = ( CellImg< UnsignedLongType, LongArray > ) factory.create( dimsLong );
				final Cursor< Cell< LongArray > > cursor = cellImg.getCells().cursor();
				while ( cursor.hasNext() )
				{
					final Cell< LongArray > cell = cursor.next();
					final long[] dataBlock = cell.getData().getCurrentStorageArray();
					cell.dimensions( dimsInt );
					cell.min( min );
					try
					{
						hdf5Access.readLongMDArrayBlockWithOffset( timepointId, setupId, level, dimsInt, min, dataBlock );
					}
					catch ( final InterruptedException e )
					{}
				}
				img = cellImg;
			}
			return img;
		}

		private int[] computeCellDimensions( final long[] dimsLong, final int[] chunkSize )
		{
			final int n = dimsLong.length;

			final long[] dimsInChunks = new long[ n ];
			int elementsPerChunk = 1;
			for ( int d = 0; d < n; ++d )
			{
				dimsInChunks[ d ] = ( dimsLong[ d ] + chunkSize[ d ] - 1 ) / chunkSize[ d ];
				elementsPerChunk *= chunkSize[ d ];
			}

			final int[] cellDimensions = new int[ n ];
			long s = Integer.MAX_VALUE / elementsPerChunk;
			for ( int d = 0; d < n; ++d )
			{
				final long ns = s / dimsInChunks[ d ];
				if ( ns > 0 )
					cellDimensions[ d ] = chunkSize[ d ] * ( int ) ( dimsInChunks[ d ] );
				else
				{
					cellDimensions[ d ] = chunkSize[ d ] * ( int ) ( s % dimsInChunks[ d ] );
					for ( ++d; d < n; ++d )
						cellDimensions[ d ] = chunkSize[ d ];
					break;
				}
				s = ns;
			}
			return cellDimensions;
		}

		@Override
		public RandomAccessibleInterval< UnsignedLongType > getImage( final int timepointId, final int level, final ImgLoaderHint... hints )
		{
			if ( Arrays.asList( hints ).contains( ImgLoaderHints.LOAD_COMPLETELY ) )
				return loadImageCompletely( timepointId, level );

			return prepareCachedImage( timepointId, level, LoadingStrategy.BLOCKING, type );
		}

		@Override
		public RandomAccessibleInterval< VolatileUnsignedLongType > getVolatileImage( final int timepointId, final int level, final ImgLoaderHint... hints )
		{
			return prepareCachedImage( timepointId, level, LoadingStrategy.BUDGETED, volatileType );
		}

		/**
		 * (Almost) create a {@link CellImg} backed by the cache.
		 * The created image needs a {@link NativeImg#setLinkedType(net.imglib2.type.Type) linked type} before it can be used.
		 * The type should be either {@link UnsignedLongType} and {@link VolatileUnsignedLongType}.
		 */
		protected < T extends NativeType< T > > RandomAccessibleInterval< T > prepareCachedImage( final int timepointId, final int level, final LoadingStrategy loadingStrategy, final T type )
		{
			open();

			final ViewLevelId id = new ViewLevelId( timepointId, setupId, level );
			if ( ! existsImageData( id ) )
			{
				System.err.println(	String.format(
						"image data for timepoint %d setup %d level %d could not be found. Partition file missing?",
						id.getTimePointId(), id.getViewSetupId(), id.getLevel() ) );
				return getMissingDataImage( id, type );
			}

			final long[] dimensions = getDimsAndExistence( id ).getDimensions();
			final int[] cellDimensions = mipmapInfo.getSubdivisions()[ level ];
			final CellGrid grid = new CellGrid( dimensions, cellDimensions );

			final int priority = mipmapInfo.getMaxLevel() - level;
			final CacheHints cacheHints = new CacheHints( loadingStrategy, priority, false );

			return cache.createImg( grid, timepointId, setupId, level, cacheHints, longLoader, type );
		}

		/**
		 * For images that are missing in the hdf5, a constant image is created. If
		 * the dimension of the missing image is known (see
		 * {@link #getDimsAndExistence(ViewLevelId)}) then use that. Otherwise
		 * create a 1x1x1 image.
		 */
		protected < T > RandomAccessibleInterval< T > getMissingDataImage( final ViewLevelId id, final T constant )
		{
			final long[] d = getDimsAndExistence( id ).getDimensions();
			return Views.interval( new ConstantRandomAccessible<>( constant, 3 ), new FinalInterval( d ) );
		}

		@Override
		public RandomAccessibleInterval< FloatType > getFloatImage( final int timepointId, final boolean normalize, final ImgLoaderHint... hints )
		{
			return getFloatImage( timepointId, 0, normalize, hints );
		}

		@Override
		public RandomAccessibleInterval< FloatType > getFloatImage( final int timepointId, final int level, final boolean normalize, final ImgLoaderHint... hints )
		{
			final RandomAccessibleInterval< UnsignedLongType > ulongImg = getImage( timepointId, level, hints );

			// copy unsigned long img to float img

			// create float img
			final FloatType f = new FloatType();
			final ImgFactory< FloatType > imgFactory;
			if ( Intervals.numElements( ulongImg ) <= Integer.MAX_VALUE )
			{
				imgFactory = new ArrayImgFactory<>( f );
			}
			else
			{
				final long[] dimsLong = new long[ ulongImg.numDimensions() ];
				ulongImg.dimensions( dimsLong );
				final int[] cellDimensions = computeCellDimensions(
						dimsLong,
						mipmapInfo.getSubdivisions()[ level ] );
				imgFactory = new CellImgFactory<>( f, cellDimensions );
			}
			final Img< FloatType > floatImg = imgFactory.create( ulongImg );

			// set up executor service
			final int numProcessors = Runtime.getRuntime().availableProcessors();
			final ExecutorService taskExecutor = Executors.newFixedThreadPool( numProcessors );
			final ArrayList< Callable< Void > > tasks = new ArrayList<>();

			// set up all tasks
			final int numPortions = numProcessors * 2;
			final long threadChunkSize = floatImg.size() / numPortions;
			final long threadChunkMod = floatImg.size() % numPortions;

			for ( int portionID = 0; portionID < numPortions; ++portionID )
			{
				// move to the starting position of the current thread
				final long startPosition = portionID * threadChunkSize;

				// the last thread may has to run longer if the number of pixels cannot be divided by the number of threads
				final long loopSize = ( portionID == numPortions - 1 ) ? threadChunkSize + threadChunkMod : threadChunkSize;

				if ( Views.iterable( ulongImg ).iterationOrder().equals( floatImg.iterationOrder() ) )
				{
					tasks.add( new Callable< Void >()
					{
						@Override
						public Void call() throws Exception
						{
							final Cursor< UnsignedLongType > in = Views.iterable( ulongImg ).cursor();
							final Cursor< FloatType > out = floatImg.cursor();

							in.jumpFwd( startPosition );
							out.jumpFwd( startPosition );

							for ( long j = 0; j < loopSize; ++j )
								out.next().set( in.next().getRealFloat() );

							return null;
						}
					} );
				}
				else
				{
					tasks.add( new Callable< Void >()
					{
						@Override
						public Void call() throws Exception
						{
							final Cursor< UnsignedLongType > in = Views.iterable( ulongImg ).localizingCursor();
							final RandomAccess< FloatType > out = floatImg.randomAccess();

							in.jumpFwd( startPosition );

							for ( long j = 0; j < loopSize; ++j )
							{
								final UnsignedLongType vin = in.next();
								out.setPosition( in );
								out.get().set( vin.getRealFloat() );
							}

							return null;
						}
					} );
				}
			}

			try
			{
				// invokeAll() returns when all tasks are complete
				taskExecutor.invokeAll( tasks );
				taskExecutor.shutdown();
			}
			catch ( final InterruptedException e )
			{
				return null;
			}

			if ( normalize )
				// normalize the image to 0...1
				normalize( floatImg );

			return floatImg;
		}

		public MipmapInfo getMipmapInfo()
		{
			return mipmapInfo;
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

		@Override
		public Dimensions getImageSize( final int timepointId )
		{
			return getImageSize( timepointId, 0 );
		}

		@Override
		public Dimensions getImageSize( final int timepointId, final int level )
		{
			final ViewLevelId id = new ViewLevelId( timepointId, setupId, level );
			final DimsAndExistence dims = getDimsAndExistence( id );
			if ( dims.exists() )
				return new FinalDimensions( dims.getDimensions() );
			else
				return null;
		}

		@Override
		public VoxelDimensions getVoxelSize( final int timepointId )
		{
			// the voxel size is not stored in the hdf5
			return null;
		}
	}
}

