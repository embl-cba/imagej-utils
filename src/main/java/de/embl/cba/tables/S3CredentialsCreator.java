package de.embl.cba.tables;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

public class S3CredentialsCreator {

    /**
     * It seems that the way S3 works is that when a user has no credentials it means anonymous,
     * but as soon as you provide some credentials it tries to get access with those,
     * which indeed don't have access for that specific bucket.
     * So it seems the way to go is to define in the application whether
     * you want to use anonymous access or credentials based access
     */
    public enum S3Authentication {
        Anonymous,
        Protected
    }

    public static AWSCredentialsProvider getCredentialsProvider(S3Authentication authentication )
    {
        switch ( authentication )
        {
            case Anonymous:
                return new AWSStaticCredentialsProvider( new AnonymousAWSCredentials() );
            case Protected:
                final DefaultAWSCredentialsProviderChain credentialsProvider = new DefaultAWSCredentialsProviderChain();
                checkCredentialsExistence( credentialsProvider );
                return credentialsProvider;
            default:
                throw new UnsupportedOperationException( "Authentication not supported: " + authentication );
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
}
