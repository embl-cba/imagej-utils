package de.embl.cba.bdv.utils.io;

import bdv.export.*;
import bdv.ij.util.PluginHelper;
import bdv.img.hdf5.Hdf5ImageLoader;
import bdv.img.hdf5.Partition;
import bdv.spimdata.SequenceDescriptionMinimal;
import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import mpicbg.spim.data.generic.sequence.BasicImgLoader;
import mpicbg.spim.data.generic.sequence.BasicSetupImgLoader;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.generic.sequence.ImgLoaderHint;
import mpicbg.spim.data.registration.ViewRegistration;
import mpicbg.spim.data.registration.ViewRegistrations;
import mpicbg.spim.data.sequence.*;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BdvRaiVolumeExport< T extends RealType< T >  & NativeType< T > >
{

	public void export(
			RandomAccessibleInterval< T > rai,
			String filePathWithoutExtension,
			double[] calibration,
			String calibrationUnit,
			double[] translation // TODO: replace by AffineTransform3D
	)
	{

		final File hdf5File = new File( filePathWithoutExtension + ".h5" );
		final File xmlFile = new File( filePathWithoutExtension + ".xml" );

		if ( rai.numDimensions() < 3 )
			rai = Views.addDimension( rai, 0, 0);

		// set up calibration
		String pixelUnit = getPixelUnit( calibrationUnit );
		final FinalVoxelDimensions voxelSize = new FinalVoxelDimensions( pixelUnit, calibration );
		final FinalDimensions imageSize = getFinalDimensions( rai );

		// propose reasonable mipmap settings
		final ExportMipmapInfo autoMipmapSettings =
				ProposeMipmaps.proposeMipmaps(
						new BasicViewSetup( 0, "", imageSize, voxelSize ) );


		final ProgressWriter progressWriter = new ProgressWriterBdv();
		progressWriter.out().println( "starting export..." );

		final BasicImgLoader imgLoader = new RaiImgLoader( rai, calibration, calibrationUnit );

		final int numTimepoints = 1;
		final int numChannels = 1;

		final AffineTransform3D sourceTransform = getSourceTransform3D( calibration, translation );

		// write hdf5
		final HashMap< Integer, BasicViewSetup > setups = new HashMap<>( numChannels );
		for ( int channelIndex = 0; channelIndex < numChannels; ++channelIndex )
		{
			String name =  "image";
			final BasicViewSetup setup = new BasicViewSetup( channelIndex, name, imageSize, voxelSize );
			setup.setAttribute( new Channel( channelIndex + 1 ) );
			setups.put( channelIndex, setup );
		}

		final ArrayList< TimePoint > timepoints = new ArrayList<>( numTimepoints );

		for ( int t = 0; t < numTimepoints; ++t )
			timepoints.add( new TimePoint( t ) );

		final SequenceDescriptionMinimal seq =
				new SequenceDescriptionMinimal(
						new TimePoints( timepoints ), setups, imgLoader, null );

		Map< Integer, ExportMipmapInfo > perSetupExportMipmapInfo;
		perSetupExportMipmapInfo = new HashMap<>();
		final ExportMipmapInfo mipmapInfo =
				new ExportMipmapInfo(
						autoMipmapSettings.getExportResolutions(),
						autoMipmapSettings.getSubdivisions() );

		for ( final BasicViewSetup setup : seq.getViewSetupsOrdered() )
			perSetupExportMipmapInfo.put( setup.getId(), mipmapInfo );


		final int numCellCreatorThreads = Math.max( 1, PluginHelper.numThreads() - 1 );
		final WriteSequenceToHdf5.LoopbackHeuristic loopbackHeuristic =
				( originalImg,
				  factorsToOriginalImg,
				  previousLevel,
				  factorsToPreviousLevel,
				  chunkSize ) ->
		{
			if ( previousLevel < 0 )
				return false;

			if ( WriteSequenceToHdf5.numElements( factorsToOriginalImg )
					/ WriteSequenceToHdf5.numElements( factorsToPreviousLevel ) >= 8 )
				return true;

			return false;
		};

		final WriteSequenceToHdf5.AfterEachPlane afterEachPlane = usedLoopBack ->
		{ };

		final ArrayList< Partition > partitions;
		partitions = null;
		WriteSequenceToHdf5.writeHdf5File(
				seq,
				perSetupExportMipmapInfo,
				true,
				hdf5File,
				loopbackHeuristic,
				afterEachPlane,
				numCellCreatorThreads,
				new SubTaskProgressWriter( progressWriter, 0, 0.95 ) );


		writeXml( hdf5File, xmlFile, progressWriter,
				numTimepoints, numChannels, sourceTransform, seq, partitions );


		progressWriter.out().println( "done" );
	}

	public AffineTransform3D getSourceTransform3D( double[] calibration, double[] translation )
	{
		final AffineTransform3D sourceTransform = new AffineTransform3D();

		sourceTransform.set(
				calibration[ 0 ], 0, 0, 0,
				0, calibration[ 1 ], 0, 0,
				0, 0, calibration[ 2 ], 0 );

		sourceTransform.translate( translation );
		return sourceTransform;
	}

	public void writeXml(
			File hdf5File,
			File xmlFile,
			ProgressWriter progressWriter,
			int numTimepoints,
			int numChannels,
			AffineTransform3D sourceTransform,
			SequenceDescriptionMinimal seq,
			ArrayList< Partition > partitions )
	{
		// write xml sequence description
		final Hdf5ImageLoader hdf5Loader = new Hdf5ImageLoader(
				hdf5File, partitions, null, false );
		final SequenceDescriptionMinimal seqh5 = new SequenceDescriptionMinimal( seq, hdf5Loader );

		final ArrayList< ViewRegistration > registrations = new ArrayList<>();
		for ( int t = 0; t < numTimepoints; ++t )
			for ( int s = 0; s < numChannels; ++s )
				registrations.add( new ViewRegistration( t, s, sourceTransform ) );

		final File basePath = xmlFile.getParentFile();
		final SpimDataMinimal spimData =
				new SpimDataMinimal( basePath, seqh5, new ViewRegistrations( registrations ) );

		try
		{
			new XmlIoSpimDataMinimal().save( spimData, xmlFile.getAbsolutePath() );
			progressWriter.setProgress( 1.0 );
		}
		catch ( final Exception e )
		{
			throw new RuntimeException( e );
		}
	}

	public String getPixelUnit( String calibrationUnit )
	{
		String punit = calibrationUnit;
		if ( punit == null || punit.isEmpty() ) punit = "px";
		return punit;
	}

	public FinalDimensions getFinalDimensions( RandomAccessibleInterval< T > rai )
	{
		long[] dimensions = new long[ rai.numDimensions() ];
		rai.dimensions( dimensions );
		return new FinalDimensions( dimensions );
	}

	class RaiImgLoader implements BasicImgLoader
	{
		final RandomAccessibleInterval< ? > rai;
		final double[] calibration;
		final String calibrationUnit;

		public RaiImgLoader( RandomAccessibleInterval< ? > rai,
							 double[] calibration,
							 String calibrationUnit )
		{
			this.rai = rai;
			this.calibration = calibration;
			this.calibrationUnit = calibrationUnit;
		}

		@Override
		public BasicSetupImgLoader< UnsignedShortType > getSetupImgLoader( int setupId )
		{
			return new SetupImgLoader< UnsignedShortType >()
			{

				@Override
				public RandomAccessibleInterval< FloatType > getFloatImage(
						int timepointId, boolean normalize, ImgLoaderHint... hints )
				{
					return null;
				}

				@Override
				public Dimensions getImageSize( int timepointId )
				{
					return new FinalDimensions(
									rai.dimension( 0 ),
									rai.dimension( 1 ),
									rai.dimension( 2 ) );
				}

				@Override
				public VoxelDimensions getVoxelSize( int timepointId )
				{
					return new VoxelDimensions()
					{
						@Override
						public String unit()
						{
							return calibrationUnit;
						}

						@Override
						public void dimensions( double[] dimensions )
						{
							for ( int d = 0; d < dimensions.length; ++d )
								dimensions[ d ] = calibration[ d ];
						}

						@Override
						public double dimension( int d )
						{
							return calibration[ d ];
						}

						@Override
						public int numDimensions()
						{
							return 3;
						}
					};
				}

				@Override
				public RandomAccessibleInterval< UnsignedShortType >
				getImage( int timepointId, ImgLoaderHint... hints )
				{
					if ( Util.getTypeFromInterval( rai ) instanceof UnsignedShortType )
						return (RandomAccessibleInterval) rai;

					if ( Util.getTypeFromInterval( rai ) instanceof UnsignedByteType )
					{
						return Converters.convert(
								rai,
								( i, o ) -> o.setInteger( ((UnsignedByteType) i).get() ),
								new UnsignedShortType( ) );
					}

					return null;

				}

				@Override
				public UnsignedShortType getImageType()
				{
					return new UnsignedShortType();
				}
			};
		}
	}



}
