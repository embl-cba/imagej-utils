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
package de.embl.cba.tables;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class FileAndUrlUtils
{
	enum ResourceType {
		FILE,  // resource is a file on the file system
		HTTP,  // resource supports http requests
		S3     // resource supports s3 API
 	}

 	public static ResourceType getType( String uri ) {
		if( uri.startsWith("https://s3") || uri.contains("s3.amazon.aws.com") ) {
			return ResourceType.S3;
		}
		else if( uri.startsWith("http") ) {
			return ResourceType.HTTP;
		}
		else {
			return ResourceType.FILE;
		}
	}

	public static List< File > getFileList( File directory, String fileNameRegExp )
	{
		final ArrayList< File > files = new ArrayList<>();
		populateFileList( directory, fileNameRegExp,files );
		return files;
	}

	public static void populateFileList( File directory, String fileNameRegExp, List< File > files ) {

		// Get all the files from a directory.
		File[] fList = directory.listFiles();

		if( fList != null )
		{
			for ( File file : fList )
			{
				if ( file.isFile() )
				{
					final Matcher matcher = Pattern.compile( fileNameRegExp ).matcher( file.getName() );

					if ( matcher.matches() )
						files.add( file );
				}
				else if ( file.isDirectory() )
				{
					populateFileList( file, fileNameRegExp, files );
				}
			}
		}
	}

	public static List< String > getFiles( File inputDirectory, String filePattern )
	{
		final List< File > fileList =
				de.embl.cba.tables.FileUtils.getFileList(
						inputDirectory, filePattern, false );

		Collections.sort( fileList, new FileAndUrlUtils.SortFilesIgnoreCase() );

		final List< String > paths = fileList.stream().map( x -> x.toString() ).collect( Collectors.toList() );

		return paths;
	}

	public static String getSeparator( String uri )
	{
		ResourceType type = getType( uri );
		String separator = null;
		switch (type) {
			case FILE:
				separator = File.separator;
				break;
			case HTTP:
				separator = "/";
				break;
			case S3:
				separator = "/";
				break;
		}
		return separator;
	}

	public static String combinePath( String... paths )
	{
		final String separator = getSeparator( paths[ 0 ] );

		String combined = paths[ 0 ];
		for ( int i = 1; i < paths.length; i++ )
		{
			if ( combined.endsWith( separator ) )
				combined = combined + paths[ i ];
			else
				combined = combined + separator + paths[ i ];
		}

		return combined;
	}

	public static String removeTrailingSlash( String path )
	{
		if ( path.endsWith( "/" ) ) path = path.substring(0, path.length() - 1);
		return path;
	}

	public static InputStream getInputStream( String uri ) throws IOException
	{
		ResourceType type = getType( uri );
		switch (type) {
			case HTTP:
				URL url = new URL( uri );
				return url.openStream();
			case FILE:
				return new FileInputStream( new File( uri ) );
			case S3:
				AmazonS3 s3 = S3Utils.getS3Client( uri );
				String[] bucketAndObject = S3Utils.getBucketAndObject( uri );
				return s3.getObject(bucketAndObject[0], bucketAndObject[1]).getObjectContent();
			default:
				throw new IOException( "Could not open uri: " + uri );
		}
	}

	public static String getParentLocation( String uri )
	{
		ResourceType type = getType( uri );
		switch (type) {
			case HTTP:
			case S3:
				try {
					URI uri1 = new URI(uri);
					URI parent = uri1.getPath().endsWith("/") ? uri1.resolve("..") : uri1.resolve(".");
					return parent.toString();
				} catch (URISyntaxException e) {
					throw new RuntimeException( "Invalid URL Syntax: " + uri );
				}
			case FILE:
				return new File(uri).getParent();
			default:
				throw new RuntimeException( "Invalid ur: " + uri );
		}

//		String tablesLocation = new File( path ).getParent();
//		if ( tablesLocation.contains( ":/" ) && ! tablesLocation.contains( "://" ) )
//			tablesLocation = tablesLocation.replace( ":/", "://" );
	}

	public static class SortFilesIgnoreCase implements Comparator<File>
	{
		public int compare( File o1, File o2 )
		{
			String s1 = o1.getName();
			String s2 = o2.getName();
			return s1.toLowerCase().compareTo(s2.toLowerCase());
		}
	}

	public static void openURI( String uri )
	{
		try
		{
			java.awt.Desktop.getDesktop().browse( new URI( uri ));
		} catch ( IOException e )
		{
			e.printStackTrace();
		} catch ( URISyntaxException e )
		{
			e.printStackTrace();
		}
	}

	public static boolean exists(String uri) {
		ResourceType type = getType( uri );
		switch (type) {
			case HTTP:
				try {
					HttpURLConnection con = (HttpURLConnection) new URL(uri).openConnection();
					con.setRequestMethod("HEAD");
					return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			case FILE:
				return new File( uri ).exists();
			case S3:
				AmazonS3 s3 = S3Utils.getS3Client( uri );
				String[] bucketAndObject = S3Utils.getBucketAndObject( uri );
				return s3.doesObjectExist(bucketAndObject[0], bucketAndObject[1]);
			default:
				return false;
		}
	}

}
