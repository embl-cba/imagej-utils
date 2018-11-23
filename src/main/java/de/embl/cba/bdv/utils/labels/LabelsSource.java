package de.embl.cba.bdv.utils.labels;

import bdv.viewer.Source;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.AbstractIntegerType;
import net.imglib2.type.volatiles.VolatileARGBType;

public interface LabelsSource < R extends RealType > extends Source< VolatileARGBType >
{
	void incrementSeed();

	RandomAccessibleInterval< R > getWrappedSource( final int t, final int mipMapLevel );

	void select( double i );

	void selectNone();
}
