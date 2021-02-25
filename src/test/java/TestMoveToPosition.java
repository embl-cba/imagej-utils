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
import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.sources.ARGBConvertedRealSource;
import examples.Examples;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.realtransform.AffineTransform3D;

public class TestMoveToPosition
{
	public static void main( String[] args ) throws SpimDataException, InterruptedException
	{
		final ARGBConvertedRealSource labelsSource = Examples.getSelectable3DSource();

		final Bdv bdv = BdvFunctions.show( labelsSource ).getBdvHandle();

		// rotate somehow to check that 'moveToPosition' also works with rotated starting transform
		final AffineTransform3D transform3D = new AffineTransform3D();
		bdv.getBdvHandle().getViewerPanel().getState().getViewerTransform( transform3D );
		transform3D.translate( new double[]{200,30,30} );
		transform3D.rotate( 2,37 );
		transform3D.scale( 1.5 );
		transform3D.translate( new double[]{200,30,30} );

		bdv.getBdvHandle().getViewerPanel().setCurrentViewerTransform( transform3D );

		Thread.sleep( 1000 );

		BdvUtils.moveToPosition( bdv, new double[]{0,0,0}, 0,1000 );
	}
}
