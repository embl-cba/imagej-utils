package de.embl.cba.bdv.utils.io;

import bdv.export.ProgressWriter;
import ij.io.LogStream;

import java.io.PrintStream;


public class ProgressWriterBdv implements ProgressWriter
{
	protected final PrintStream out = new LogStream();
	protected final PrintStream err = new LogStream();

	public ProgressWriterBdv()
	{
		// TODO: find a way that this does not log into log window
	}

	@Override
	public PrintStream out()
	{
		return this.out;
	}

	@Override
	public PrintStream err()
	{
		return this.err;
	}

	@Override
	public void setProgress( double completionRatio )
	{

	}
}
