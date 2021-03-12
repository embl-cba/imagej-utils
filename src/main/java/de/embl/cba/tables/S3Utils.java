package de.embl.cba.tables;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import ij.gui.GenericDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public abstract class S3Utils {
    public static String selectS3PathFromDirectory( String directory, String objectName ) throws IOException
    {
        final ArrayList< String > filePaths = getS3FilePaths( directory );
        final String[] fileNames = filePaths.stream().map( File::new ).map( File::getName ).toArray( String[]::new );

        final GenericDialog gd = new GenericDialog( "Select " + objectName );
        gd.addChoice( objectName, fileNames, fileNames[ 0 ] );
        gd.showDialog();
        if ( gd.wasCanceled() ) return null;
        final String fileName = gd.getNextChoice();
        String newFilePath = FileAndUrlUtils.combinePath( directory, fileName );

        return newFilePath;
    }

    public static ArrayList< String > getS3FilePaths( String directory )
    {
        final AmazonS3 s3 = FileAndUrlUtils.getS3Client( directory );
        final String[] bucketAndObject = FileAndUrlUtils.getBucketAndObject( directory );

        final String bucket = bucketAndObject[0];
        final String prefix = (bucketAndObject[1] == "") ? "" : (bucketAndObject[1] + "/");

        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(bucket)
                .withPrefix(prefix)
                .withDelimiter("/");
        ListObjectsV2Result files = s3.listObjectsV2(request);
        final ArrayList< String > paths = new ArrayList<>();
        for(S3ObjectSummary summary: files.getObjectSummaries()) {
            paths.add(summary.getKey());
        }
        return paths;
    }
}
