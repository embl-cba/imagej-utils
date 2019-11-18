package de.embl.cba.bdv.utils;

import ij.IJ;

import javax.swing.*;

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
}
