package users.maxim;

import de.embl.cba.bdv.utils.viewer.MultipleImageViewer;
import net.imagej.ImageJ;

import java.io.File;
import java.util.ArrayList;

import static de.embl.cba.bdv.utils.FileUtils.getFileList;

public class ExploreXRayData
{
	public static void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

		final String excludeTransformedRegExp = "^(?!.*?(?:transformed)).*xml$";
		final ArrayList< File > paths = getFileList( new File( "/Volumes/cba/exchange/maxim/ver2/2sources" ),
				excludeTransformedRegExp );

//		final ArrayList< File > subSet = new ArrayList<>();
//		subSet.add( paths.get( 0 ) );
//		subSet.add( paths.get( 1 ) );

		final MultipleImageViewer viewer = new MultipleImageViewer( paths );
		viewer.setOpService( imageJ.op() );
		viewer.showImages( MultipleImageViewer.BlendingMode.Avg );
//		viewer.saveSettingsXmlForCurrentSource();
		viewer.prealignCurrentPlatynereisXRaySource( false );
		viewer.saveSettingsXmlForCurrentSource();
	}
}
