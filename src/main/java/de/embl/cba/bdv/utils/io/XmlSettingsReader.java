/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2021 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.bdv.utils.io;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.MinMaxGroup;
import bdv.tools.brightness.SetupAssignments;
import net.imglib2.ops.parse.token.Int;
import net.imglib2.type.numeric.ARGBType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XmlSettingsReader
{
	private ArrayList< MinMaxGroup > minMaxGroups;
	private ArrayList< Integer > colors;
	private static final double minIntervalSize = 0;

	public XmlSettingsReader()
	{
	}

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

	private void loadSettings( final String xmlFilename ) throws IOException, JDOMException
	{
		final SAXBuilder sax = new SAXBuilder();
		final Document doc = sax.build( xmlFilename );
		final Element root = doc.getRootElement();

		readSetupAssignmentsFromXml( root );
	}

	private void readSetupAssignmentsFromXml( final Element parent )
	{
		final Element elemSetupAssignments = parent.getChild( "SetupAssignments" );
		if ( elemSetupAssignments == null )
			return;
		final Element elemConverterSetups = elemSetupAssignments.getChild( "ConverterSetups" );
		final List< Element > converterSetupNodes = elemConverterSetups.getChildren( "ConverterSetup" );

//		if ( converterSetupNodes.size() != setups.size() )
//			throw new IllegalArgumentException();

		final Element elemMinMaxGroups = elemSetupAssignments.getChild( "MinMaxGroups" );
		final List< Element > minMaxGroupNodes = elemMinMaxGroups.getChildren( "MinMaxGroup" );
		minMaxGroups = new ArrayList<>(  );
		for ( int i = 0; i < minMaxGroupNodes.size(); ++i )
			minMaxGroups.add( null );
		for ( final Element elem : minMaxGroupNodes  )
		{
			final int id = Integer.parseInt( elem.getChildText( "id" ) );
			final double fullRangeMin = Double.parseDouble( elem.getChildText( "fullRangeMin" ) );
			final double fullRangeMax = Double.parseDouble( elem.getChildText( "fullRangeMax" ) );
			final double rangeMin = Double.parseDouble( elem.getChildText( "rangeMin" ) );
			final double rangeMax = Double.parseDouble( elem.getChildText( "rangeMax" ) );
			final double currentMin = Double.parseDouble( elem.getChildText( "currentMin" ) );
			final double currentMax = Double.parseDouble( elem.getChildText( "currentMax" ) );
			minMaxGroups.set( id, new MinMaxGroup( fullRangeMin, fullRangeMax, rangeMin, rangeMax, currentMin, currentMax, minIntervalSize ) );
		}

		colors = new ArrayList<>(  );
		for ( final Element elem : converterSetupNodes )
		{
			final int id = Integer.parseInt( elem.getChildText( "id" ) );
			final double min = Double.parseDouble( elem.getChildText( "min" ) );
			final double max = Double.parseDouble( elem.getChildText( "max" ) );
			final int color = Integer.parseInt( elem.getChildText( "color" ) );
			final int groupId = Integer.parseInt( elem.getChildText( "groupId" ) );
			colors.add( color );
		}
	}

	public ArrayList< MinMaxGroup > getMinMaxGroups()
	{
		return minMaxGroups;
	}

	public ArrayList< Integer > getColors()
	{
		return colors;
	}

	private ConverterSetup getSetupById( final int id )
	{
//		for ( final ConverterSetup setup : setups )
//			if ( setup.getSetupId() == id )
//				return setup;
		return null;
	}

}
