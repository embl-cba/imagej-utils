package usage;

import de.embl.cba.bdv.utils.command.BdvToVoxelGridImageCommand;
import de.embl.cba.bdv.utils.io.BdvToVoxelGridImageConverter;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.io.File;

public class ConvertRegisteredParapodLabelsToVoxelGridImageUsingMain
{
	public static < T extends RealType< T > & NativeType< T > > void main( String[] args )
	{

		BdvToVoxelGridImageConverter.main(
				new String[]{
						"/Volumes/arendt/EM_6dpf_segmentation/EM-Prospr/AChE-MED.xml",
						"/Volumes/arendt/EM_6dpf_segmentation/EM-Prospr/ulong/em-segmented-cells-parapod-fib-labels.xml",
						"NearestNeighbor",
						"Tiff",
						"/Users/tischer/Desktop/pao" }
				);

//
//		final BdvToVoxelGridImageCommand< T > command = new BdvToVoxelGridImageCommand<>();
//		command.bdvReferenceImage = new File("/Volumes/arendt/EM_6dpf_segmentation/EM-Prospr/AChE-MED.xml" );
//		command.bdvSourceImage = new File("/Volumes/arendt/EM_6dpf_segmentation/EM-Prospr/ulong/em-segmented-cells-parapod-fib-labels.xml" );
//		command.outputFileFormat = "Tiff";
//		command.interpolationType = "NearestNeighbor";
//		command.outputPath = new File( "/Users/tischer/Desktop/pao" );
//		command.run();
	}
}
