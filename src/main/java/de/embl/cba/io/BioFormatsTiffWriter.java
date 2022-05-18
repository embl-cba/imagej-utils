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
package de.embl.cba.io;

import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import loci.common.DebugTools;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.ImageWriter;
import loci.formats.meta.IMetadata;
import loci.formats.out.TiffWriter;
import loci.formats.services.OMEXMLService;
import loci.formats.tiff.IFD;
import ome.units.quantity.Length;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.PositiveInteger;

import java.io.IOException;

public class BioFormatsTiffWriter
{
	private final ImagePlus imagePlus;

	public BioFormatsTiffWriter( ImagePlus imagePlus )
	{
		this.imagePlus = imagePlus;
	}

	public void write( String filePath )
	{
		if ( ! filePath.endsWith( "ome.tif" ) )
		{
			filePath += "ome.tif";
		}

		try
		{
			DebugTools.setRootLevel( "OFF" );
			ImageWriter writer = createImageWriter( imagePlus, filePath );
			TiffWriter tiffWriter = (TiffWriter) writer.getWriter();

			ImageStack stack = imagePlus.getStack();
			for ( int i = 0; i < stack.size(); i++)
			{
				IFD ifd = new IFD();

				if ( imagePlus.getBytesPerPixel() == 2 )
				{
					byte[] bytes = ShortToByteBigEndian( ( short[] ) stack.getProcessor( i + 1 ).getPixels() );
					tiffWriter.saveBytes( i, bytes, ifd );
				}
				else if ( imagePlus.getBytesPerPixel() == 1)
				{
					byte[] bytes = ( byte[] ) ( stack.getProcessor( i + 1 ).getPixels() );
					tiffWriter.saveBytes( i, bytes, ifd );
				}
			}

			tiffWriter.close();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			throw new RuntimeException( "Error writing file: " + filePath );
		}
	}

	private ImageWriter createImageWriter( ImagePlus imp, String filePath ) throws DependencyException, ServiceException, FormatException, IOException
	{
		ServiceFactory factory = new ServiceFactory();
		OMEXMLService service = factory.getInstance(OMEXMLService.class);
		IMetadata meta = service.createOMEXMLMetadata();
		meta.setImageID("Image:0", 0);
		meta.setPixelsID("Pixels:0", 0);
		meta.setPixelsBinDataBigEndian( Boolean.TRUE, 0, 0 );
		meta.setPixelsDimensionOrder( DimensionOrder.XYZCT, 0 );

		if (imp.getBytesPerPixel() == 2) {
			meta.setPixelsType( PixelType.UINT16, 0);
		} else if (imp.getBytesPerPixel() == 1) {
			meta.setPixelsType( PixelType.UINT8, 0);
		}

		meta.setPixelsSizeX( new PositiveInteger(imp.getWidth()), 0);
		meta.setPixelsSizeY( new PositiveInteger(imp.getHeight()), 0);
		meta.setPixelsSizeZ( new PositiveInteger(imp.getNSlices()), 0);
		meta.setPixelsSizeC( new PositiveInteger(1), 0);
		meta.setPixelsSizeT( new PositiveInteger(1), 0);

		Calibration calibration = imp.getCalibration();

		Length physicalSizeX = FormatTools.getPhysicalSizeX( calibration.pixelWidth, calibration.getXUnit() );
		Length physicalSizeY = FormatTools.getPhysicalSizeY( calibration.pixelHeight, calibration.getYUnit() );
		Length physicalSizeZ = FormatTools.getPhysicalSizeZ( calibration.pixelDepth, calibration.getZUnit() );

		meta.setPixelsPhysicalSizeX( physicalSizeX, 0 );
		meta.setPixelsPhysicalSizeY( physicalSizeY, 0 );
		meta.setPixelsPhysicalSizeZ( physicalSizeZ, 0 );

		for ( int planeIndex = 0; planeIndex < imp.getStack().size(); planeIndex++ )
		{
			meta.setPlanePositionX( FormatTools.getPhysicalSize( calibration.xOrigin, calibration.getXUnit() ), 0, planeIndex );
			meta.setPlanePositionY( FormatTools.getPhysicalSize( calibration.yOrigin, calibration.getYUnit() ), 0, planeIndex );
			meta.setPlanePositionZ( FormatTools.getPhysicalSize( calibration.zOrigin, calibration.getZUnit() ), 0, planeIndex );
		}

		int channel = 0;
		meta.setChannelID("Channel:0:" + channel, 0, channel);
		meta.setChannelSamplesPerPixel(new PositiveInteger(1), 0, channel);

		ImageWriter writer = new ImageWriter();
		writer.setValidBitsPerPixel( imp.getBytesPerPixel() * 8 );
		writer.setMetadataRetrieve( meta );
		writer.setId( filePath );
		writer.setWriteSequentially( true ); // ? is this necessary

		return writer;
	}

	public static byte[] ShortToByteBigEndian( short[] input ) {
		int short_index, byte_index;
		int iterations = input.length;

		byte[] buffer = new byte[input.length * 2];

		short_index = byte_index = 0;

		for (/*NOP*/; short_index != iterations; /*NOP*/) {
			// Big Endian: store higher byte first
			buffer[byte_index] = (byte) ((input[short_index] & 0xFF00) >> 8);
			buffer[byte_index + 1] = (byte) (input[short_index] & 0x00FF);

			++short_index;
			byte_index += 2;
		}
		return buffer;
	}
}
