package de.embl.cba.util;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public abstract class PathMapper
{

    public static List< String > asEMBLClusterMounted( List< Path > paths )
    {
        ArrayList< String > newPaths = new ArrayList<>();

        for ( Path path : paths )
        {
            newPaths.add( asEMBLClusterMounted( path.toString() ) );
        }

        return newPaths;
    }

    public static String asEMBLClusterMounted( String string )
    {
        return asEMBLClusterMounted( Paths.get( string ) );
    }

    public static String asEMBLClusterMounted( File file )
    {
        return asEMBLClusterMounted( file.toPath() );
    }

    public static String asEMBLClusterMounted( Path path )
    {

        String newPathString = path.toString();

        if ( newPathString.length() < 3 )
        {
            return newPathString;
        }

        if ( OSUtils.isMac() )
        {
            newPathString = newPathString.replace( "/Volumes/", "/g/" );
        }
        else if ( OSUtils.isWindows() )
        {
            try
            {
                Runtime runTime = Runtime.getRuntime();
                Process process = null;
                process = runTime.exec( "net use" );

                InputStream inStream = process.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader( inStream );
                BufferedReader bufferedReader = new BufferedReader( inputStreamReader );
                String line = null;
                String[] components = null;

                System.out.println( "Input path string: " + newPathString );
                while ( null != ( line = bufferedReader.readLine() ) )
                {
                    System.out.println( line );
                    components = line.split( "\\s+" );
                    if ( components.length > 2 )
                    {
                        System.out.println( components[ 1 ] + ": " + components[2] );

                        if ( components[ 1 ].equals( newPathString.substring( 0, 2 )  ) )
                        {
                            newPathString = newPathString.replace( components[ 1 ], components[ 2 ] );
                        }

                    }

                }

                newPathString = newPathString.replace( "\\", "/" );
                newPathString = newPathString.replaceFirst( "//[^/]*/", "/g/" );

            }
            catch ( IOException e )
            {
                System.out.println( e.toString() );
            }
        }

        return newPathString;

    }

}
