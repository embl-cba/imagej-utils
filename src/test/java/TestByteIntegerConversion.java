public class TestByteIntegerConversion
{
	public static void main( String[] args )
	{
		byte b = (byte) 200;
		System.out.println( b );

		int i0 = b;
		System.out.println( i0 );

		int i1 = b & 0xFF;
		System.out.println( i1 );

		int i2 = ( (int) b ) & 0xFF;
		System.out.println( i2 );

		final byte[] bytes = new byte[ 256 ];

		System.out.println( bytes[ b & 0xFF ]);
	}
}
