package de.embl.cba.bdv.utils.io;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.MinMaxGroup;
import bdv.tools.brightness.SetupAssignments;
import net.imglib2.type.numeric.ARGBType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class XmlSettingsReader
{
	public boolean tryLoadSettings( final String xmlFilename )
	{
		File proposedSettingsFile = null;
		if( xmlFilename.startsWith( "http://" ) )
		{
			// load settings.xml from the BigDataServer
			final String settings = xmlFilename + "settings";
			{
				try
				{
					loadSettings( settings );
					return true;
				}
				catch ( final FileNotFoundException e )
				{}
				catch ( final Exception e )
				{
					e.printStackTrace();
				}
			}
		}
		else if ( xmlFilename.endsWith( ".xml" ) )
		{
			final String settings = xmlFilename.substring( 0, xmlFilename.length() - ".xml".length() ) + ".settings" + ".xml";
			proposedSettingsFile = new File( settings );
			if ( proposedSettingsFile.isFile() )
			{
				try
				{
					loadSettings( settings );
					return true;
				}
				catch ( final Exception e )
				{
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public void loadSettings( final String xmlFilename ) throws IOException, JDOMException
	{
		final SAXBuilder sax = new SAXBuilder();
		final Document doc = sax.build( xmlFilename );
		final Element root = doc.getRootElement();
//		viewer.stateFromXml( root );
//		setupAssignments.restoreFromXml( root );
//		manualTransformation.restoreFromXml( root );
//		bookmarks.restoreFromXml( root );
//		activeSourcesDialog.update();
//		viewer.requestRepaint();
	}

	/**
	 * Restore the state of this {@link SetupAssignments} from XML. Note, that
	 * this only restores the assignments of setups to groups and group
	 * settings. The list of {@link ConverterSetup}s is not restored.
	 */
	public void readSetupAssignmentsFromXml( final Element parent )
	{
		final Element elemSetupAssignments = parent.getChild( "SetupAssignments" );
		if ( elemSetupAssignments == null )
			return;
		final Element elemConverterSetups = elemSetupAssignments.getChild( "ConverterSetups" );
		final List< Element > converterSetupNodes = elemConverterSetups.getChildren( "ConverterSetup" );
//		if ( converterSetupNodes.size() != setups.size() )
//			throw new IllegalArgumentException();
//
//		final Element elemMinMaxGroups = elemSetupAssignments.getChild( "MinMaxGroups" );
//		final List< Element > minMaxGroupNodes = elemMinMaxGroups.getChildren( "MinMaxGroup" );
//		minMaxGroups.clear();
//		for ( int i = 0; i < minMaxGroupNodes.size(); ++i )
//			minMaxGroups.add( null );
//		for ( final Element elem : minMaxGroupNodes  )
//		{
//			final int id = Integer.parseInt( elem.getChildText( "id" ) );
//			final double fullRangeMin = Double.parseDouble( elem.getChildText( "fullRangeMin" ) );
//			final double fullRangeMax = Double.parseDouble( elem.getChildText( "fullRangeMax" ) );
//			final double rangeMin = Double.parseDouble( elem.getChildText( "rangeMin" ) );
//			final double rangeMax = Double.parseDouble( elem.getChildText( "rangeMax" ) );
//			final double currentMin = Double.parseDouble( elem.getChildText( "currentMin" ) );
//			final double currentMax = Double.parseDouble( elem.getChildText( "currentMax" ) );
//			minMaxGroups.set( id, new MinMaxGroup( fullRangeMin, fullRangeMax, rangeMin, rangeMax, currentMin, currentMax, minIntervalSize ) );
//		}
//
//		for ( final Element elem : converterSetupNodes )
//		{
//			final int id = Integer.parseInt( elem.getChildText( "id" ) );
//			final double min = Double.parseDouble( elem.getChildText( "min" ) );
//			final double max = Double.parseDouble( elem.getChildText( "max" ) );
//			final int color = Integer.parseInt( elem.getChildText( "color" ) );
//			final int groupId = Integer.parseInt( elem.getChildText( "groupId" ) );
//			final ConverterSetup setup = getSetupById( id );
//			setup.setDisplayRange( min, max );
//			setup.setColor( new ARGBType( color ) );
//			final MinMaxGroup group = minMaxGroups.get( groupId );
//			setupToGroup.put( setup, group );
//			group.addSetup( setup );
//		}
//
//		if ( updateListener != null )
//			updateListener.update();
	}

	private ConverterSetup getSetupById( final int id )
	{
//		for ( final ConverterSetup setup : setups )
//			if ( setup.getSetupId() == id )
//				return setup;
		return null;
	}

}
