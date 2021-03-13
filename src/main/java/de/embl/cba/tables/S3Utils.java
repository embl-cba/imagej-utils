package de.embl.cba.tables;


import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import ij.gui.GenericDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class S3Utils {

    public static AmazonS3 getS3Client( String uri ) {
        final String endpoint = getEndpoint( uri );
        final String region = "us-west-2";
        final AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(endpoint, region);

        // final S3CredentialsCreator.S3Authentication authentication = S3CredentialsCreator.S3Authentication.Anonymous;
        final S3CredentialsCreator.S3Authentication authentication = S3CredentialsCreator.S3Authentication.Protected;
        AmazonS3 s3 = AmazonS3ClientBuilder
                .standard()
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(endpointConfiguration)
                .withCredentials(S3CredentialsCreator.getCredentialsProvider(authentication))
                .build();
        return s3;
    }

    public static String[] getBucketAndObject( String uri ) {
        final String[] split = uri.split("/");
        String bucket = split[3];
        String object = Arrays.stream( split ).skip( 4 ).collect( Collectors.joining( "/") );
        return new String[] {bucket, object};
    }

    public static String getEndpoint( String uri ) {
        final String[] split = uri.split("/");
        String endpoint = Arrays.stream( split ).limit( 3 ).collect( Collectors.joining( "/" ) );
        return endpoint;
    }

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
        final AmazonS3 s3 = getS3Client( directory );
        final String[] bucketAndObject = getBucketAndObject( directory );

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
