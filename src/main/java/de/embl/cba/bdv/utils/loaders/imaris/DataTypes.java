package de.embl.cba.bdv.utils.loaders.imaris;

import bdv.img.cache.CacheArrayLoader;
import bdv.img.imaris.IHDF5Access;
import bdv.img.imaris.ImarisVolatileByteArrayLoader;
import bdv.img.imaris.ImarisVolatileFloatArrayLoader;
import bdv.img.imaris.ImarisVolatileShortArrayLoader;
import net.imglib2.Volatile;
import net.imglib2.img.basictypeaccess.volatiles.VolatileAccess;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileByteArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileFloatArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileShortArray;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.type.volatiles.VolatileFloatType;
import net.imglib2.type.volatiles.VolatileUnsignedByteType;
import net.imglib2.type.volatiles.VolatileUnsignedShortType;

class DataTypes
{
	static interface DataType<
			T extends NativeType< T >,
			V extends Volatile< T > & NativeType< V > ,
			A extends VolatileAccess >
	{
		public T getType();

		public V getVolatileType();

		public CacheArrayLoader< A > createArrayLoader( final IHDF5Access hdf5Access );
	}

	static DataTypes.DataType< UnsignedByteType, VolatileUnsignedByteType, VolatileByteArray > UnsignedByte =
			new DataTypes.DataType< UnsignedByteType, VolatileUnsignedByteType, VolatileByteArray >()
			{
				private final UnsignedByteType type = new UnsignedByteType();

				private final VolatileUnsignedByteType volatileType = new VolatileUnsignedByteType();

				@Override
				public UnsignedByteType getType()
				{
					return type;
				}

				@Override
				public VolatileUnsignedByteType getVolatileType()
				{
					return volatileType;
				}

				@Override
				public CacheArrayLoader< VolatileByteArray > createArrayLoader( final IHDF5Access hdf5Access )
				{
					return new ImarisVolatileByteArrayLoader( hdf5Access );
				}
			};

	static DataTypes.DataType< UnsignedShortType, VolatileUnsignedShortType, VolatileShortArray > UnsignedShort =
			new DataTypes.DataType< UnsignedShortType, VolatileUnsignedShortType, VolatileShortArray >()
			{
				private final UnsignedShortType type = new UnsignedShortType();

				private final VolatileUnsignedShortType volatileType = new VolatileUnsignedShortType();

				@Override
				public UnsignedShortType getType()
				{
					return type;
				}

				@Override
				public VolatileUnsignedShortType getVolatileType()
				{
					return volatileType;
				}

				@Override
				public CacheArrayLoader< VolatileShortArray > createArrayLoader( final IHDF5Access hdf5Access )
				{
					return new ImarisVolatileShortArrayLoader( hdf5Access );
				}
			};

	static DataTypes.DataType< FloatType, VolatileFloatType, VolatileFloatArray > Float =
			new DataTypes.DataType< FloatType, VolatileFloatType, VolatileFloatArray >()
			{
				private final FloatType type = new FloatType();

				private final VolatileFloatType volatileType = new VolatileFloatType();

				@Override
				public FloatType getType()
				{
					return type;
				}

				@Override
				public VolatileFloatType getVolatileType()
				{
					return volatileType;
				}

				@Override
				public CacheArrayLoader< VolatileFloatArray > createArrayLoader( final IHDF5Access hdf5Access )
				{
					return new ImarisVolatileFloatArrayLoader( hdf5Access );
				}
			};
}
