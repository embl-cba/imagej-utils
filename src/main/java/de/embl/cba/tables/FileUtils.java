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

import de.embl.cba.tables.github.GitHubUtils;
import de.embl.cba.tables.github.GitLocation;
import ij.gui.GenericDialog;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.embl.cba.tables.S3Utils.*;
import static de.embl.cba.tables.github.GitHubUtils.getFileNames;
import static de.embl.cba.tables.github.GitHubUtils.selectGitHubPathFromDirectory;

public class FileUtils
{
	public enum FileLocation {
		Project,
		FileSystem
	}

	public static List< File > getFileList(
			File directory,
			String fileNameRegExp,
			boolean recursive )
	{
		final ArrayList< File > files = new ArrayList<>();

		populateFileList(
				directory,
				fileNameRegExp,
				files,
				recursive );

		return files;
	}

	public static boolean isGithub( String directory ) {
		return directory.contains( "raw.githubusercontent" );
	}

	public static boolean isS3( String directory ) {
		return directory.contains( "s3.amazon.aws.com" ) || directory.startsWith("https://s3");
	}

	public static String[] getFileNamesFromProject( String directory ) {
		if ( directory != null ) {
			if ( isGithub(directory) ) {
				return getFileNames( directory );
			} else if ( isS3(directory)) {
				return getS3FileNames( directory );
			} else {
				File[] files = new File(directory).listFiles();
				if ( files != null ) {
					String[] fileNames = new String[files.length];
					for ( int i = 0; i< files.length; i++) {
						fileNames[i] = files[i].getName();
					}
					return fileNames;
				} else {
					return null;
				}
			}
		} else {
			return null;
		}
	}

	// objectName is used for the dialog labels e.g. 'table', 'bookmark' etc...
	public static String selectPathFromProjectOrFileSystem (String directory, String objectName) throws IOException {
		FileLocation fileLocation = null;
		if ( directory != null )
		{
			final GenericDialog gd = new GenericDialog( "Choose source" );
			gd.addChoice( "Load from", new String[]{ FileLocation.Project.toString(),
					FileLocation.FileSystem.toString() }, FileLocation.Project.toString() );
			gd.showDialog();
			if ( gd.wasCanceled() ) return null;
			fileLocation = FileLocation.valueOf( gd.getNextChoice() );
		}

		String filePath = null;
		if ( directory != null && fileLocation.equals( FileLocation.Project ) && isGithub( directory ) )
		{
			// we choose from project and have a github project
			filePath = selectGitHubPathFromDirectory( directory, objectName );
			if ( filePath == null ) return null;
		}
		else if ( directory != null && fileLocation.equals( FileLocation.Project ) && isS3( directory ) )
		{
			// we choose from project and have a s3 project
			filePath = selectS3PathFromDirectory(directory, objectName);
			if ( filePath == null ) return null;
		}
		else
		{
			final JFileChooser jFileChooser = new JFileChooser( directory );

			if ( jFileChooser.showOpenDialog( null ) == JFileChooser.APPROVE_OPTION )
				filePath = jFileChooser.getSelectedFile().getAbsolutePath();
		}

		if ( filePath == null ) return null;

		if ( filePath.startsWith( "http" ) )
			filePath = resolveTableURL( URI.create( filePath ) );

		return filePath;
	}

	public static String resolveTableURL( URI uri )
	{
		while( isRelativePath( uri.toString() ) )
		{
			URI relativeURI = URI.create( getRelativePath( uri.toString() ) );
			uri = uri.resolve( relativeURI ).normalize();
		}

		return uri.toString();
	}

	public static String resolveTablePath ( String path )
	{
		try {
			while ( isRelativePath( path ) ) {
				String relativePath = getRelativePath(path);
				path = new File( new File( path ).getParent(), relativePath ).getCanonicalPath();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return path;
	}

	public static boolean isRelativePath( String tablePath )  {
		try ( final BufferedReader reader = Tables.getReader(tablePath) )
		{
			final String firstLine = reader.readLine();
			return firstLine.startsWith("..");
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			return false;
		}
	}

	public static String getRelativePath( String tablePath ) {
		try( final BufferedReader reader = Tables.getReader( tablePath ) )
		{
			String link = reader.readLine();
			return link;
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			return null;
		}
	}

	public static void populateFileList(
			File directory,
			String fileNameRegExp,
			List< File > files,
			boolean recursive ) {

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
					{
						files.add( file );
					}

				}
				else if ( file.isDirectory() )
				{
					if ( recursive )
						populateFileList( file, fileNameRegExp, files, recursive );
				}
			}
		}
	}

	public static boolean stringContainsItemFromList( String inputStr, ArrayList< String > items)
	{
		return items.parallelStream().anyMatch( inputStr::contains );
	}
}
