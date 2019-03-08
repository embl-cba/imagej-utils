package de.embl.cba.bdv.utils.sources;

import bdv.BigDataViewer;
import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.NumericType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LazySpimSource< T extends NumericType< T > > implements Source< T >
{
	private final String name;
	private final File file;
	private Source< T > source;

	public LazySpimSource( String name, File file )
	{
		this.name = name;
		this.file = file;
	}

	public Source< T > wrappedSource()
	{
		if ( source == null )
		{
			final SpimData spimData = openSpimData( file );
			final List< ConverterSetup > converterSetups = new ArrayList<>();
			final List< SourceAndConverter< ? > > sources = new ArrayList<>();
			BigDataViewer.initSetups( spimData, converterSetups, sources );

			source = ( Source< T > ) sources.get( 0 ).asVolatile().getSpimSource();
		}

		return source;
	}

	private SpimData openSpimData( File file )
	{
		try
		{
			SpimData spimData = new XmlIoSpimData().load( file.toString() );
			return spimData;
		}
		catch ( SpimDataException e )
		{
			System.out.println( file.toString() );
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean isPresent( int t )
	{
		if ( t == 0 ) return true;
		return false;
	}

	@Override
	public RandomAccessibleInterval< T > getSource( int t, int level )
	{
		return wrappedSource().getSource( t, level );
	}

	@Override
	public RealRandomAccessible< T > getInterpolatedSource( int t, int level, Interpolation method )
	{
		return wrappedSource().getInterpolatedSource( t, level, method );
	}

	@Override
	public void getSourceTransform( int t, int level, AffineTransform3D transform )
	{
		wrappedSource().getSourceTransform( t, level, transform  );
	}

	@Override
	public T getType()
	{
		return wrappedSource().getType();
	}

	@Override
	public String getName()
	{
		return wrappedSource().getName();
	}

	@Override
	public VoxelDimensions getVoxelDimensions()
	{
		return wrappedSource().getVoxelDimensions();
	}

	@Override
	public int getNumMipmapLevels()
	{
		return wrappedSource().getNumMipmapLevels();
	}
}
