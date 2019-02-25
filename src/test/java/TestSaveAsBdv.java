import ij.IJ;
import ij.ImagePlus;

public class TestSaveAsBdv
{
	public static void main( String[] args )
	{
		final ImagePlus imagePlus = IJ.openImage(
				TestSaveAsBdv.class.getResource(
						"imagePlus-20x-20y-1c-5z-3t.zip" ).getFile() );




	}
}
