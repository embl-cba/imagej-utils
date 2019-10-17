import bdv.util.RandomAccessibleIntervalSource4D;
import de.embl.cba.bdv.utils.sources.ModifiableRandomAccessibleIntervalSource4D;
import de.embl.cba.bdv.utils.wrap.Wraps;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.HyperStackConverter;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.ArrayList;

public class TestImagePlusWrapAsSource4D
{

	// TODO: Write a proper test!

	public static < R extends RealType< R > & NativeType< R > >
	void main( String[] args )
	{
		// 5D
		final ImagePlus imagePlus01 = IJ.openImage(
				TestImagePlusWrapAsSource4D.class.getResource(
						"imagePlus-20x-20y-2c-5z-3t.zip" ).getFile() );

		final ArrayList< ModifiableRandomAccessibleIntervalSource4D< R > > wrap01 =
				Wraps.imagePlusAsSource4DChannelList( imagePlus01 );

		// Only one channel
		final ImagePlus imagePlusC1 = IJ.openImage(
				TestImagePlusWrapAsSource4D.class.getResource(
						"imagePlus-20x-20y-1c-5z-3t.zip" ).getFile() );

		final ArrayList< ModifiableRandomAccessibleIntervalSource4D< R > > wrapC1 =
				Wraps.imagePlusAsSource4DChannelList( imagePlusC1 );

		// Only one channel, only one time-point
		final ImagePlus imagePlusC1T1 = IJ.openImage(
				TestImagePlusWrapAsSource4D.class.getResource(
						"imagePlus-20x-20y-1c-5z-1t.zip" ).getFile() );

		final ArrayList< ModifiableRandomAccessibleIntervalSource4D< R > > wrapC1T1 =
				Wraps.imagePlusAsSource4DChannelList( imagePlusC1T1 );

		int a = 1;

	}
}
