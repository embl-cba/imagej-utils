package examples;

import bdv.util.BdvFunctions;
import mpicbg.spim.data.SpimDataException;

public class ExampleARGBConvertedLabelsSource
{
	public static void main( String[] args ) throws SpimDataException
	{
		BdvFunctions.show( Examples.getSelectable3DSource() );
	}

}
