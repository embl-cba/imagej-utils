package de.embl.cba.bdv.utils.loaders.imaris;

import static mpicbg.spim.data.XmlHelpers.loadPath;
import static mpicbg.spim.data.XmlKeys.IMGLOADER_FORMAT_ATTRIBUTE_NAME;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import bdv.img.hdf5.MipmapInfo;
import bdv.img.hdf5.Util;
import bdv.img.imaris.HDF5AccessHack;
import bdv.img.imaris.IHDF5Access;
import bdv.spimdata.SequenceDescriptionMinimal;
import bdv.util.MipmapTransforms;
import ch.systemsx.cisd.hdf5.*;
import mpicbg.spim.data.XmlHelpers;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.generic.sequence.ImgLoaderIo;
import mpicbg.spim.data.generic.sequence.XmlIoBasicImgLoader;

import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.TimePoints;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.realtransform.AffineTransform3D;
import org.jdom2.Element;

@ImgLoaderIo( format = "bdv.imaris", type = ImarisImageLoader2.class )
public class XmlIoImarisImageLoader implements XmlIoBasicImgLoader< ImarisImageLoader2 >
{
	private DataTypes.DataType< ?, ?, ? > dataType;
	private MipmapInfo mipmapInfo;
	private SequenceDescriptionMinimal seq;
	private long[][] dimensions;

	@Override
	public Element toXml( final ImarisImageLoader2 imgLoader, final File basePath )
	{
		final Element elem = new Element( "ImageLoader" );
		elem.setAttribute( IMGLOADER_FORMAT_ATTRIBUTE_NAME, this.getClass().getAnnotation( ImgLoaderIo.class ).format() );
		elem.addContent( XmlHelpers.pathElement( "hdf5", imgLoader.getHdf5File(), basePath ) );
		return elem;
	}

	@Override
	public ImarisImageLoader2 fromXml( final Element elem, final File basePath, final AbstractSequenceDescription< ?, ?, ? > sequenceDescription )
	{
		final File hdf5File = loadPath( elem, "hdf5", basePath );
		try
		{
			// - The sequenceDescription information is both in the xml as well as in the imaris hdf5 file; which one to take here?
			// - the code in parseImarisFile() is from Imaris, where should it be?
			// - How much information shall we put into the xml?
			// - I guess we would need some class that is responsible for producing the xml?! (not only for Imaris files, but also for others, e.g. pyramidal Tiff).
			// - Something like BdvXmlCreator? Input: Some image file, Output: xml
			parseImarisFile( hdf5File );
		} catch ( IOException e )
		{
			e.printStackTrace();
		}

		return new ImarisImageLoader2( dataType, hdf5File, mipmapInfo, dimensions, this.seq );
	}

	private void parseImarisFile( File basePath ) throws IOException
	{
		final IHDF5Reader reader = HDF5Factory.openForReading( basePath );
		final IHDF5Access access;
		try
		{
			access = new HDF5AccessHack( reader );
		}
		catch ( final Exception e )
		{
			throw new RuntimeException( e );
		}

		final HashMap< Integer, double[] > levelToResolution = new HashMap<>();
		final HashMap< Integer, int[] > levelToSubdivision = new HashMap<>();
		final HashMap< Integer, long[] > levelToDimensions = new HashMap<>();
		final HashMap< Integer, TimePoint > timepointMap = new HashMap<>();
		final HashMap< Integer, BasicViewSetup > setupMap = new HashMap<>();

		String path = "DataSetInfo/Image";
		final double[] extMax = new double[] {
				Double.parseDouble( access.readImarisAttributeString( path, "ExtMax0" ) ),
				Double.parseDouble( access.readImarisAttributeString( path, "ExtMax1" ) ),
				Double.parseDouble( access.readImarisAttributeString( path, "ExtMax2" ) ),
		};
		final double[] extMin = new double[] {
				Double.parseDouble( access.readImarisAttributeString( path, "ExtMin0" ) ),
				Double.parseDouble( access.readImarisAttributeString( path, "ExtMin1" ) ),
				Double.parseDouble( access.readImarisAttributeString( path, "ExtMin2" ) ),
		};
		final int[] imageSize = new int[] {
				Integer.parseInt( access.readImarisAttributeString( path, "X" ) ),
				Integer.parseInt( access.readImarisAttributeString( path, "Y" ) ),
				Integer.parseInt( access.readImarisAttributeString( path, "Z" ) ),
		};
		final String unit = access.readImarisAttributeString( path, "Unit" );
		final VoxelDimensions voxelSize = new FinalVoxelDimensions( unit, new double[] {
				( extMax[ 0 ] - extMin[ 0 ] ) / imageSize[ 0 ],
				( extMax[ 1 ] - extMin[ 1 ] ) / imageSize[ 1 ],
				( extMax[ 2 ] - extMin[ 2 ] ) / imageSize[ 2 ]
		} );

		dataType = null;
		final List< String > resolutionNames = reader.getGroupMembers( "DataSet" );
		for ( final String resolutionName : resolutionNames )
		{
			if ( !resolutionName.startsWith( "ResolutionLevel " ) )
			{
				throw new IOException( "unexpected content '" + resolutionName + "' while reading " + basePath );
			}
			else
			{
				final int level = Integer.parseInt( resolutionName.substring( "ResolutionLevel ".length() ) );
				final List< String > timepointNames = reader.getGroupMembers( "DataSet/" + resolutionName );
				for ( final String timepointName : timepointNames )
				{
					if ( !timepointName.startsWith( "TimePoint " ) )
					{
						throw new IOException( "unexpected content '" + timepointName + "' while reading " + basePath );
					}
					else
					{
						final int timepoint = Integer.parseInt( timepointName.substring( "TimePoint ".length() ) );
						if ( !timepointMap.containsKey( timepoint ) )
							timepointMap.put( timepoint, new TimePoint( timepoint ) );

						final List< String > channelNames = reader.getGroupMembers( "DataSet/" + resolutionName + "/" + timepointName );
						for ( final String channelName : channelNames )
						{
							if ( !channelName.startsWith( "Channel " ) )
							{
								throw new IOException( "unexpected content '" + channelName + "' while reading " + basePath );
							}
							else
							{
								final HDF5DataSetInformation info = reader.getDataSetInformation( "DataSet/" + resolutionName + "/" + timepointName + "/" + channelName + "/Data" );
								if (  dataType == null )
								{
									final HDF5DataTypeInformation ti = info.getTypeInformation();
									if ( ti.getDataClass().equals( HDF5DataClass.INTEGER ) )
									{
										switch ( ti.getElementSize() )
										{
											case 1:
												dataType = DataTypes.UnsignedByte;
												break;
											case 2:
												dataType = DataTypes.UnsignedShort;
												break;
											default:
												throw new IOException( "expected datatype" + ti );
										}
									}
									else if ( ti.getDataClass().equals( HDF5DataClass.FLOAT ) )
									{
										switch ( ti.getElementSize() )
										{
											case 4:
												dataType = DataTypes.Float;
												break;
											default:
												throw new IOException( "expected datatype" + ti );
										}
									}
								}

								final int channel = Integer.parseInt( channelName.substring( "Channel ".length() ) );
								if ( !setupMap.containsKey( channel ) )
								{
									final String defaultSetupName = "channel " + channel;
									final String name = access.readImarisAttributeString( "DataSetInfo/Channel " + channel, "Description", defaultSetupName );
									final BasicViewSetup setup = new BasicViewSetup( channel, name, new FinalDimensions( imageSize ), voxelSize );
									setupMap.put( channel, setup );
								}

								double[] resolution = levelToResolution.get( level );
								if ( resolution == null ) {
									path = "DataSet/" + resolutionName + "/" + timepointName + "/" + channelName;

									final long[] dims = new long[] {
											Integer.parseInt( access.readImarisAttributeString( path, "ImageSizeX" ) ),
											Integer.parseInt( access.readImarisAttributeString( path, "ImageSizeY" ) ),
											Integer.parseInt( access.readImarisAttributeString( path, "ImageSizeZ" ) ),
									};

									final int[] blockDims = new int[] { 16, 16, 16 };
									try
									{
										blockDims[ 0 ] = Integer.parseInt( access.readImarisAttributeString( path, "ImageBlockSizeX" ) );
										blockDims[ 1 ] = Integer.parseInt( access.readImarisAttributeString( path, "ImageBlockSizeY" ) );
										blockDims[ 2 ] = Integer.parseInt( access.readImarisAttributeString( path, "ImageBlockSizeZ" ) );
									} catch( final NumberFormatException e )
									{
										int[] chunkSizes = info.tryGetChunkSizes();
										if ( chunkSizes != null )
										{
											chunkSizes = Util.reorder( chunkSizes );
											for ( int d = 0; d < 3; ++d )
												blockDims[ d ] = chunkSizes[ d ];
										}
									}

									resolution = new double[] {
											imageSize[ 0 ] / dims[ 0 ],
											imageSize[ 1 ] / dims[ 1 ],
											imageSize[ 2 ] / dims[ 2 ],
									};

									levelToDimensions.put( level, dims );
									levelToResolution.put( level, resolution );
									levelToSubdivision.put( level, blockDims );
								}
							}
						}
					}
				}
			}
		}

		final int numLevels = levelToResolution.size();
		dimensions = new long[ numLevels ][];
		final double[][] resolutions = new double[ numLevels ][];
		final int[][] subdivisions = new int[ numLevels ][];
		final AffineTransform3D[] transforms = new AffineTransform3D[ numLevels ];
		for ( int level = 0; level < numLevels; ++level )
		{
			dimensions[ level ] = levelToDimensions.get( level );
			resolutions[ level ] = levelToResolution.get( level );
			subdivisions[ level ] = levelToSubdivision.get( level );
			transforms[ level ] = MipmapTransforms.getMipmapTransformDefault( resolutions[ level ] );
		}
		mipmapInfo = new MipmapInfo( resolutions, transforms, subdivisions );

		seq = new SequenceDescriptionMinimal( new TimePoints( timepointMap ), setupMap, null, null );
	}

}
