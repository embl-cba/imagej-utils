/*-
 * #%L
 * TODO
 * %%
 * Copyright (C) 2018 - 2020 EMBL
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

//	public static synchronized void progress(
//			String header,
//		    long startTimeMillis,
//			long counter,
//			long counterMax)
//	{
//
//		// Tasks
//		String countInfo = " " + counter + "/" + counterMax;
//
//		// Time
//		long milliseconds = ( System.currentTimeMillis() - startTimeMillis );
//		double minutes = 1.0 * milliseconds / ( 1000 * 60 );
//		double millisecondsPerTask = 1.0 * milliseconds / counter;
//		double minutesPerTask = 1.0 * minutes / counter;
//		long tasksLeft = counterMax - counter;
//		double minutesLeft = 1.0 * tasksLeft * minutesPerTask;
//
//		String timeInfo = String.format(
//				"Time (spent, to-go, per task) [min]: " +
//						"%.1f, %.1f, %.1f", minutes, minutesLeft, minutesPerTask);
//
//		// Memory
//		long megaBytes = IJ.currentMemory() / 1000000L;
//		long megaBytesAvailable = IJ.maxMemory() / 1000000L;
//
//		String memoryInfo = "Memory (current, avail) [MB]: "
//				+ megaBytes + ", " + megaBytesAvailable;
//
//
//		ArrayList < String > texts = new ArrayList<>( );
//		texts.add( header );
//		texts.add( countInfo );
//		texts.add( timeInfo );
//		texts.add( memoryInfo );
//
//		String jointText = "";
//		for ( String text : texts )
//			jointText += text + "; ";
//
//		IJ.log( jointText );
//	}


	public static synchronized void progress( String msg, final long startTimeMillis, final int count, final long total )
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

		progress( msg,
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
