package de.embl.cba.tables;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.google.api.client.http.HttpStatusCodes;
import ij.gui.GenericDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class S3Utils {

    // TODO if we decide to use this, we should get rid of S3CredentialsCreator and use this method in mobie
    // then we can also get rid of the authentication field in the bdv.xml
    public static AmazonS3 getS3Client( String uri ) {
        final String endpoint = getEndpoint( uri );
        final String region = "us-west-2";
        final AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(endpoint, region);

        final String[] bucketAndObject = getBucketAndObject(uri);

        // first we create a client with anon credentials and see if we can access the bucket like this
        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider( new AnonymousAWSCredentials() );
        AmazonS3 s3 = AmazonS3ClientBuilder
                .standard()
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(endpointConfiguration)
                .withCredentials(credentialsProvider)
                .build();

        // check if we can access the access
        HeadBucketRequest headBucketRequest = new HeadBucketRequest(bucketAndObject[0]);
        try {
            HeadBucketResult headBucketResult = s3.headBucket(headBucketRequest);
            return s3;
        }
        catch (AmazonServiceException e) {
            switch(e.getStatusCode()) {
                // if we get a 403 response (access forbidden), we try again with credentials
                case HttpStatusCodes.STATUS_CODE_FORBIDDEN:
                    credentialsProvider = new DefaultAWSCredentialsProviderChain();
                    checkCredentialsExistence(credentialsProvider);
                    s3 = AmazonS3ClientBuilder
                            .standard()
                            .withPathStyleAccessEnabled(true)
                            .withEndpointConfiguration(endpointConfiguration)
                            .withCredentials(credentialsProvider)
                            .build();
                    // check if we have access permissions now
                    try {
                        HeadBucketResult headBucketResult = s3.headBucket(headBucketRequest);
                    } catch(AmazonServiceException e2) {
                        throw e2;
                    }
                    return s3;
                    // otherwise the bucket does not exist or has been permanently moved; throw the exception
                default:
                    throw e;
            }
        }
    }

    public static void checkCredentialsExistence( AWSCredentialsProvider credentialsProvider )
    {
        try
        {
            credentialsProvider.getCredentials();
        }
        catch ( Exception e )
        {
            throw  new RuntimeException( e ); // No credentials could be found
        }
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
