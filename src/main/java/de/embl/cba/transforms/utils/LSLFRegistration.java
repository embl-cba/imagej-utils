/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2021 EMBL
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
package de.embl.cba.transforms.utils;

import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvOptions;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import de.embl.cba.util.CopyUtils;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import mpicbg.spim.data.generic.sequence.ImgLoaders;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.interpolation.randomaccess.ClampingNLinearInterpolatorFactory;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import spim.fiji.spimdata.imgloaders.XmlIoStackImgLoaderIJ;

import java.util.ArrayList;
import java.util.Arrays;

public class LSLFRegistration < T extends RealType< T > & NativeType< T > >
{
	public static final String LINEAR_INTERPOLATION = "Linear";
	private static InterpolatorFactory interpolatorFactory;
	private ArrayList< RandomAccessibleInterval > images;
	private String imagePathTarget;
	private String imagePathSource;
	private final String bdvXmlPath;
	private ArrayList< AffineTransform3D > transforms;
	private long[] min;
	private long[] max;
	private final long[] subSampling;
	private ArrayList< String > inputImagePaths;
	private static FinalInterval crop;
	private boolean showImages;
	private int numImages;

	public LSLFRegistration(
			String imagePathTarget, // not used?
			String imagePathSource,
			String bdvXmlPath,
			long[] imageIntervalMin,
			long[] imageIntervalMax,
			long[] subSampling,
			InterpolatorFactory interpolatorFactory,
			boolean showImages )
	{
		this.imagePathTarget = imagePathTarget;
		this.imagePathSource = imagePathSource;
		this.showImages = showImages;
		inputImagePaths = new ArrayList<>();
		inputImagePaths.add( imagePathTarget );
		inputImagePaths.add( imagePathSource );
		this.numImages = inputImagePaths.size();

		this.bdvXmlPath = bdvXmlPath;
		this.min = imageIntervalMin;
		this.max = imageIntervalMax;
		this.subSampling = subSampling;
		this.interpolatorFactory = interpolatorFactory;

		// needed when packaging code into an executable jar
		ImgLoaders.registerManually( XmlIoStackImgLoaderIJ.class );
	}


	/**
	 * To call this class from the command line type:
	 *
	 * java -jar /Users/tischer/Documents/transforms-utils/target/transforms-utils-0.2.01-jar-with-dependencies.jar "/Volumes/cba/exchange/LS_LF_comparison/LenseLeNet_Microscope/OnlyTiffStacksAndAffineMatrixProvided/LF_stack.tif" "/Volumes/cba/exchange/LS_LF_comparison/LenseLeNet_Microscope/OnlyTiffStacksAndAffineMatrixProvided/LS_stack.tif" "/Volumes/cba/exchange/LS_LF_comparison/LenseLeNet_Microscope/XML_fromMultiviewRegistrationPlugin/dataset.xml" "0,0,0" "500,1000,300" "1,1,20" "Linear"
	 *
	 *
	 * To build above jar type:
	 * mvn clean package
	 *
	 */
	public static < T extends RealType< T > & NativeType< T > >
	void main( String[] args ) throws SpimDataException
	{
		// parse args
		int i = 0;
		final String imagePathTarget = args[ i++ ];
		final String imagePathSource = args[ i++ ];
		final String bdvXmlPath = args[ i++ ];
		long[] min = Arrays.stream(args[ i++ ].split(","))
				.mapToLong(Long::parseLong).toArray();
		long[] max = Arrays.stream(args[ i++ ].split(","))
				.mapToLong(Long::parseLong).toArray();
		long[] subSampling = Arrays.stream(args[ i++ ].split(","))
				.mapToLong(Long::parseLong).toArray();

		InterpolatorFactory interpolatorFactory;
		if( args[ 6 ].equals( LINEAR_INTERPOLATION ) )
		{
			interpolatorFactory = new ClampingNLinearInterpolatorFactory();
		}
		else
		{
			System.err.println( "interpolation method not supported" );
			return;
		}

		// call method
		final LSLFRegistration< T > registration = new LSLFRegistration<>(
				imagePathTarget,
				imagePathSource,
				bdvXmlPath,
				min,
				max,
				subSampling,
				interpolatorFactory,
				false );

		registration.run();
	}

	public void run() throws SpimDataException
	{
		loadTransformsFromBdvXml( bdvXmlPath );

		loadImages();

		final ArrayList< RandomAccessibleInterval > transformed =
				createTransformedImages( images, transforms, min, max );

		final ArrayList< RandomAccessibleInterval< T > > subSampled
				= createSubSampledImages( transformed );

		final ArrayList< RandomAccessibleInterval< T > > finalImages
				= forceImagesIntoRAM( subSampled );

		if ( showImages )
		{
			showImagesInBdv( finalImages );
			showImagesInImageJ( finalImages );
		}

		saveImages( finalImages );
	}

	private void saveImages( ArrayList< RandomAccessibleInterval< T > > finalImages )
	{
		for ( int i = 0; i < 2; i++ )
		{
			final FileSaver saver = new FileSaver( asImagePlus( finalImages.get( i ), "image_" + i ) );
			final String outputPath =
					inputImagePaths.get( i ).replace(
							".tif",
							"_registered.tif" );
			Logger.log( "Saving: " + outputPath );
			saver.saveAsTiff( outputPath );
		}
	}


	private void showImagesInImageJ( ArrayList< RandomAccessibleInterval< T > > finalImages )
	{
		for ( int i = 0; i < 2; i++ )
			asImagePlus( finalImages.get( i ) , "image_" + i ).show();
	}

	private ArrayList< RandomAccessibleInterval< T > > forceImagesIntoRAM( ArrayList< RandomAccessibleInterval< T > > subSampled )
	{
		final ArrayList< RandomAccessibleInterval< T > > finalImages = new ArrayList<>();
		for ( int i = 0; i < 2; i++ )
		{
			final int numThreads = Runtime.getRuntime().availableProcessors();

			Logger.log( "Creating output image: " + ( i + 1 ) + " / 2, using " + numThreads + " threads."  );
			final RandomAccessibleInterval< T > finalImage =
					CopyUtils.copyVolumeRaiMultiThreaded(
							subSampled.get( i ),
							numThreads );

			finalImages.add( finalImage );
		}
		return finalImages;
	}

	private ArrayList< RandomAccessibleInterval< T > > createSubSampledImages( ArrayList< RandomAccessibleInterval > transformed )
	{
		final ArrayList< RandomAccessibleInterval< T > > subSampled = new ArrayList<>();
		for ( int i = 0; i < 2; i++ )
		{
			subSampled.add(
				Views.subsample( transformed.get( i ), subSampling ) );
		}
		return subSampled;
	}

	private void showImagesInBdv( ArrayList< RandomAccessibleInterval< T > > images )
	{
		final BdvHandle bdv = BdvFunctions.show(
				images.get( 0 ),
				"reference " ).getBdvHandle();

		BdvFunctions.show(
				images.get( 1 ),
				"other",
				BdvOptions.options().addTo( bdv ) );
	}

	private static ArrayList< RandomAccessibleInterval >
	createTransformedImages(
			ArrayList< RandomAccessibleInterval > images,
			ArrayList< AffineTransform3D > transforms,
			long[] min,
			long[] max )
	{
		final ArrayList< RandomAccessibleInterval > transformed = new ArrayList<>();

		for ( int i = 0; i < 2; i++ )
		{
			// The transformedRA lives on a voxel grid
			// with a voxelSpacing as defined in the bdv.xml file,
			// combined with the affineTransformations

			final RandomAccessible transformedRA =
					Transforms.createTransformedRaView(
							images.get( i ),
							transforms.get( i ),
							interpolatorFactory );

			// Now we need to crop (in voxel units), which should correspond to isotropic
			// physical units, because that's the partially point of above affineTransformations

			crop = new FinalInterval( min, max );

			transformed.add( Views.interval( transformedRA, crop ) );
		}

		return transformed;
	}

	private void loadTransformsFromBdvXml( String xmlPath ) throws SpimDataException
	{
		Logger.log( "Load transforms from: " + bdvXmlPath );
		SpimData spimData = new XmlIoSpimData().load( xmlPath );

		transforms = new ArrayList<>(  );
		for ( int i = 0; i < numImages; i++ )
			transforms.add(
					spimData.getViewRegistrations()
							.getViewRegistration( 0, i ).getModel() );
	}

	private ArrayList< RandomAccessibleInterval > loadImages()
	{
		images = new ArrayList<>(  );
		Logger.log( "Open image: " + imagePathTarget );
		images.add( openImage( imagePathTarget ) );
		Logger.log( "Open image: " + imagePathSource );
		images.add( openImage( imagePathSource ) );
		return images;
	}

	private RandomAccessibleInterval< T > openImage( String imagePath )
	{
		return ImageJFunctions.wrap( IJ.openImage( imagePath ) );
	}

	public static ImagePlus asImagePlus( RandomAccessibleInterval rai, String title )
	{
		final ImagePlus wrap = ImageJFunctions.wrap(
				Views.permute(
						Views.addDimension( rai, 0, 0 ),
						2, 3 ), title );
		return wrap;
	}
}
