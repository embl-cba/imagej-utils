package de.embl.cba.bdv.utils;

import ij.IJ;

public class Logger
{
	public static void log( String msg )
	{
		IJ.log( msg );
	}

	public static void error( String msg ) { IJ.error( msg  );}
}
