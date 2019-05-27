package de.embl.cba.bdv.utils.command;

import de.embl.cba.bdv.utils.io.BdvXmlToVoxelGridImageConverter;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.FileWidget;

import java.io.File;

@Plugin(type = Command.class,
		menuPath = "Plugins>BigDataTools>Convert>Bdv Affine Image to Voxel Grid Image")
public class BdvAffineImageToVoxelGridImageCommand < T extends RealType< T > & NativeType< T > >
		implements Command
{

	@Parameter( label = "Bdv reference image for voxel spacing and image size", style = FileWidget.OPEN_STYLE )
	public File bdvReferenceImage;

	@Parameter( label = "Bdv source image", style = FileWidget.OPEN_STYLE )
	public File bdvSourceImage;

	@Parameter( label = "Output file", style = FileWidget.SAVE_STYLE )
	public File outputPath;

	// TODO: replace by enum, once possible
	@Parameter( label = "Output file format", choices = { "Tiff", "Bdv"} )
	public String outputFileFormat;

	// TODO: replace by enum, once possible
	@Parameter( label = "Interpolation type", choices = { "NearestNeighbor", "NLinear"} )
	public String interpolationType;

	@Override
	public void run()
	{
		String outputPathWithoutExtension = outputPath.toString().substring(0, outputPath.toString().lastIndexOf('.'));

		final BdvXmlToVoxelGridImageConverter< T > converter = new BdvXmlToVoxelGridImageConverter<>(
				bdvReferenceImage.getAbsolutePath(),
				bdvSourceImage.getAbsolutePath(),
				BdvXmlToVoxelGridImageConverter.InterpolationType.valueOf( interpolationType ) );

		converter.run(
				BdvXmlToVoxelGridImageConverter.FileFormat.valueOf( outputFileFormat ),
				outputPathWithoutExtension );
	}

}
