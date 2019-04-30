package de.embl.cba.bdv.utils.sources;

import bdv.BigDataViewer;
import bdv.ViewerImgLoader;
import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import mpicbg.spim.data.generic.sequence.ImgLoaderHint;
import mpicbg.spim.data.sequence.MultiResolutionImgLoader;
import mpicbg.spim.data.sequence.SetupImgLoader;
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
	private final File file;
	private final String name;
	private Source< T > volatileSource;
	private SpimData spimData;
	private List< ConverterSetup > converterSetups;
	private List< SourceAndConverter< ? > > sources;

	public LazySpimSource( String name, File file )
	{
		this.name = name;
		this.file = file;
	}

	private Source< T > wrappedVolatileSource()
	{
		if ( spimData == null ) initSpimData();

		if ( volatileSource == null )
			volatileSource = ( Source< T > ) sources.get( 0 ).asVolatile().getSpimSource();

		return volatileSource;
	}


	private void initSpimData()
	{
		spimData = openSpimData( file );
		converterSetups = new ArrayList<>();
		sources = new ArrayList<>();
		BigDataViewer.initSetups( spimData, converterSetups, sources );
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

	public RandomAccessibleInterval< T > getNonVolatileSource( int t, int level )
	{
		if ( spimData == null ) initSpimData();

		final MultiResolutionImgLoader setupImgLoader =
				( MultiResolutionImgLoader ) spimData.getSequenceDescription().getImgLoader();

		final RandomAccessibleInterval< T > image =
				( RandomAccessibleInterval ) setupImgLoader.getSetupImgLoader( 0 ).getImage( t, level );

		return image;
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
		return wrappedVolatileSource().getSource( t, level );
	}

	@Override
	public RealRandomAccessible< T > getInterpolatedSource( int t, int level, Interpolation method )
	{
		return wrappedVolatileSource().getInterpolatedSource( t, level, method );
	}

	@Override
	public void getSourceTransform( int t, int level, AffineTransform3D transform )
	{
		wrappedVolatileSource().getSourceTransform( t, level, transform  );
	}

	@Override
	public T getType()
	{
		return wrappedVolatileSource().getType();
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public VoxelDimensions getVoxelDimensions()
	{
		return wrappedVolatileSource().getVoxelDimensions();
	}

	@Override
	public int getNumMipmapLevels()
	{
		return wrappedVolatileSource().getNumMipmapLevels();
	}
}
