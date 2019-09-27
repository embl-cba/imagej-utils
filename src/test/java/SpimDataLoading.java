import bdv.BigDataViewer;
import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.SourceAndConverter;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imglib2.realtransform.AffineTransform3D;

import java.io.File;
import java.util.ArrayList;

public class SpimDataLoading
{
	public static void main( String[] args )
	{
		final File file = new File( SpimDataLoading.class.getResource( "bdv_mipmap-raw.xml" ).getFile() );

		final SpimData spimData = openSpimData( file );
		final ArrayList< ConverterSetup > converterSetups = new ArrayList<>();
		final ArrayList< SourceAndConverter< ? > > sources = new ArrayList<>();
		BigDataViewer.initSetups( spimData, converterSetups, sources );
		final AffineTransform3D model = spimData.getViewRegistrations().getViewRegistrationsOrdered().get( 0 ).getModel();
		int a = 1;
	}


	public static SpimData openSpimData( File file )
	{
		try
		{
			SpimData spimData = new XmlIoSpimData().load( file.toString() );
			return spimData;
		}
		catch ( SpimDataException e )
		{
			System.out.println( file.toString() );
			e.printStackTrace();
			return null;
		}
	}
}
