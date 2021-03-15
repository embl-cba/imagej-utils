package de.embl.cba.bdv.utils;

import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.SpimDataIOException;
import mpicbg.spim.data.generic.XmlIoAbstractSpimData;
import mpicbg.spim.data.registration.XmlIoViewRegistrations;
import mpicbg.spim.data.sequence.SequenceDescription;
import mpicbg.spim.data.sequence.XmlIoSequenceDescription;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.InputStream;

import static mpicbg.spim.data.XmlKeys.SPIMDATA_TAG;


public class CustomXmlIoSpimData extends XmlIoAbstractSpimData< SequenceDescription, SpimData> {
    public CustomXmlIoSpimData() {
        super(SpimData.class, new XmlIoSequenceDescription(), new XmlIoViewRegistrations());
    }

    // NOTE we still need to pass the xml filename here, so that bdv can determine the relative
    // paths for local files.
    public SpimData loadFromStream(InputStream in, String xmlFilename) throws SpimDataException {
        final SAXBuilder sax = new SAXBuilder();
        Document doc;
        try
        {
            doc = sax.build( in );
        }
        catch ( final Exception e )
        {
            throw new SpimDataIOException( e );
        }
        final Element root = doc.getRootElement();

        if ( root.getName() != SPIMDATA_TAG )
            throw new RuntimeException( "expected <" + SPIMDATA_TAG + "> root element. wrong file?" );

        return fromXml( root, new File( xmlFilename ) );
    }
}