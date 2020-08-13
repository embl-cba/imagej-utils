package de.embl.cba.morphometry;

import ij.IJ;

import java.io.File;

public class Logger
{
	public static boolean showDebugInformation = false;

	public static void debug( String s )
	{
		if ( showDebugInformation )
			IJ.log( s );
	}

	public static void log( String message )
	{
		IJ.log( message );
		System.out.println( message );

		if ( Utils.logFilePath != null )
		{
			File logFile = new File( Utils.logFilePath );

			if ( ! logFile.exists() )
			{
				Utils.createLogFile();
			}
			else
			{
				Utils.writeToLogFile( message + "\n" );
			}
		}

	}

	public static void error( String s )
	{
		IJ.showMessage( s );
		System.err.println( s );
	}

	public static void progress( final long total, final int count, final long startTimeMillis, String msg )
	{
		double secondsSpent = (1.0 * System.currentTimeMillis() - startTimeMillis ) / (1000.0);
		double secondsPerTask = secondsSpent / count;
		double secondsLeft = (total - count) * secondsPerTask;

		String unit = "s";
		double divisor = 1;

		if ( secondsSpent > 3 * 60 )
		{
			unit = "min";
			divisor = 60;
		}

		Logger.progress( msg,
				"" + count + "/" + total
						+ "; time ( spent, left, task ) [ " + unit + " ]: "
						+ ( int ) ( secondsSpent / divisor )
						+ ", " + ( int ) ( secondsLeft / divisor )
						+ ", " + String.format("%.3g", secondsPerTask / divisor)
						+ "; memory: "
						+ IJ.freeMemory() );
	}

	public static void progress( String msg, String progress )
	{
		progress = msg + ": " + progress;

		if ( IJ.getLog() != null )
		{
			String[] logs = IJ.getLog().split( "\n" );
			if ( logs.length > 1 )
			{
				if ( logs[ logs.length - 1 ].contains( msg ) )
				{
					progress = "\\Update:" + progress;
				}
			}
		}

		IJ.log( progress );
		System.out.println( progress );
	}

}
