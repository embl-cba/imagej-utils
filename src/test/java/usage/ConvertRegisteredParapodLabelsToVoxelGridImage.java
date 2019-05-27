package usage;

import de.embl.cba.bdv.utils.command.BdvAffineImageToVoxelGridImageCommand;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.io.File;

public class ConvertRegisteredParapodLabelsToVoxelGridImage
{
	public static < T extends RealType< T > & NativeType< T > > void main( String[] args )
	{
		final BdvAffineImageToVoxelGridImageCommand< T > command = new BdvAffineImageToVoxelGridImageCommand<>();
		command.bdvReferenceImage = new File("/Volumes/arendt/EM_6dpf_segmentation/EM-Prospr/AChE-MED.xml" );
		command.bdvSourceImage = new File("/Volumes/arendt/EM_6dpf_segmentation/EM-Prospr/ulong/em-segmented-cells-parapod-fib-labels.xml" );
		command.outputFileFormat = "Tiff";
		command.interpolationType = "NearestNeighbor";
		command.outputPath = new File( "/Users/tischer/Desktop/pao.tiff" );
		command.run();
	}
}
