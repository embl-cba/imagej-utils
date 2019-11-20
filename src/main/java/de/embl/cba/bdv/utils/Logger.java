package de.embl.cba.bdv.utils;

import ij.IJ;

import javax.swing.*;
import java.util.ArrayList;

public class Logger
{
	public static void log( String msg )
	{
		System.out.println( msg );
		IJ.log( msg );
	}

	public static void error( String msg )
	{
		System.err.println( msg );
		IJ.error( msg );
	}

	public static synchronized void progress(
			String header,
		    long startTimeMillis,
			long counter,
			long counterMax)
	{

		// Tasks
		String countInfo = " " + counter + "/" + counterMax;

		// Time
		long milliseconds = ( System.currentTimeMillis() - startTimeMillis );
		double minutes = 1.0 * milliseconds / ( 1000 * 60 );
		double millisecondsPerTask = 1.0 * milliseconds / counter;
		double minutesPerTask = 1.0 * minutes / counter;
		long tasksLeft = counterMax - counter;
		double minutesLeft = 1.0 * tasksLeft * minutesPerTask;

		String timeInfo = String.format(
				"Time (spent, to-go, per task) [min]: " +
						"%.1f, %.1f, %.1f", minutes, minutesLeft, minutesPerTask);

		// Memory
		long megaBytes = IJ.currentMemory() / 1000000L;
		long megaBytesAvailable = IJ.maxMemory() / 1000000L;

		String memoryInfo = "Memory (current, avail) [MB]: "
				+ megaBytes + ", " + megaBytesAvailable;


		ArrayList < String > texts = new ArrayList<>( );
		texts.add( header );
		texts.add( countInfo );
		texts.add( timeInfo );
		texts.add( memoryInfo );

		String jointText = "";
		for ( String text : texts )
			jointText += text + "; ";

		IJ.log( jointText );
	}
}
