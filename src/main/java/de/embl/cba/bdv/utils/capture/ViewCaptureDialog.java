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
package de.embl.cba.bdv.utils.capture;

import bdv.util.BdvHandle;
import ij.gui.NonBlockingGenericDialog;

import static de.embl.cba.bdv.utils.BdvUtils.getViewerVoxelSpacing;

public class ViewCaptureDialog implements Runnable
{
	private BdvHandle bdvHandle;

	public ViewCaptureDialog( BdvHandle bdvHandle )
	{
		this.bdvHandle = bdvHandle;
	}

	@Override
	public void run()
	{
		final String pixelUnit = "micrometer";
		final NonBlockingGenericDialog gd = new NonBlockingGenericDialog( "Pixel Spacing" );
		gd.addNumericField( "Pixel Spacing", getViewerVoxelSpacing( bdvHandle ), 3,  10, pixelUnit );
		gd.addCheckbox( "Show raw data", true );
		gd.showDialog();
		if( gd.wasCanceled() ) return;
		final double pixelSpacing = gd.getNextNumber();
		final boolean showRawData = gd.getNextBoolean();

		// TODO: make own class of captureView!
		final ViewCaptureResult viewCaptureResult = BdvViewCaptures.captureView(
				bdvHandle,
				pixelSpacing,
				pixelUnit,
				false );
		viewCaptureResult.rgbImage.show();

		if ( showRawData )
			viewCaptureResult.rawImagesStack.show();

	}
}
