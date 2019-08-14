package tests;

import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.sources.LazySpimSource;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestNonVolatileAccess
{
	@Test
	public < R extends RealType< R > > void lazySpimSourceAccess()
	{
		final LazySpimSource< R > source = new LazySpimSource<>( "source",
				TestNonVolatileAccess.class.getResource( "../test-data/labels-ulong.xml" ).getFile() );

		final RandomAccessibleInterval< ? extends RealType< ? > > rai =
				BdvUtils.getRealTypeNonVolatileRandomAccessibleInterval( source, 0, 0 );

		final RandomAccess< ? extends RealType< ? > > randomAccess = rai.randomAccess();
 		randomAccess.setPosition( new long[]{10,10,10} );
		final RealType< ? > realType = randomAccess.get();
		System.out.println( realType.getRealDouble() );
		assertEquals( 2.0, realType.getRealDouble(), 0.0 );
	}

	public static void main( String[] args )
	{
		new TestNonVolatileAccess().lazySpimSourceAccess( );
	}

}
