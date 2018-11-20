package de.embl.cba.bdv.utils.labels.luts;

import com.sun.jna.IntegerType;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.AbstractIntegerType;

public interface LabelsSource < T extends AbstractIntegerType >
{
	void incrementSeed();

	RandomAccessibleInterval< T > getIndexImg( final int t, final int mipMapLevel );

	void select( long i );

	void selectNone();
}
