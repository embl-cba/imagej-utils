package playground;

import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.sources.LazySpimSource;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

import java.io.File;

public class TryNonVolatileAccess
{
	public static < R extends RealType< R > >  void main( String[] args )
	{

		final LazySpimSource< R > source = new LazySpimSource<>( "source",
				TryNonVolatileAccess.class.getResource( "../labels-ulong.xml" ).getFile() );

		final RandomAccessibleInterval< ? extends RealType< ? > > rai =
				BdvUtils.getRealTypeNonVolatileRandomAccessibleInterval( source, 0, 0 );

		final RandomAccess< ? extends RealType< ? > > randomAccess = rai.randomAccess();
 		randomAccess.setPosition( new long[]{10,10,10} );
		final RealType< ? > realType = randomAccess.get();
		System.out.println( realType.getRealDouble() );

	}
}
