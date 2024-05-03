/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2024 EMBL
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

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.SliderPanelDouble;
import bdv.util.BoundedValueDouble;

import java.util.List;

public class BrightnessUpdateListener implements BoundedValueDouble.UpdateListener
{
	final private List< ConverterSetup > converterSetups;
 	final private BoundedValueDouble min;
	final private BoundedValueDouble max;
	private final SliderPanelDouble minSlider;
	private final SliderPanelDouble maxSlider;

	public BrightnessUpdateListener( BoundedValueDouble min,
									 BoundedValueDouble max,
									 SliderPanelDouble minSlider,
									 SliderPanelDouble maxSlider,
									 List< ConverterSetup > converterSetups )
	{
		this.min = min;
		this.max = max;
		this.minSlider = minSlider;
		this.maxSlider = maxSlider;
		this.converterSetups = converterSetups;
	}

	@Override
	public void update()
	{
		minSlider.update();
		maxSlider.update();
		for ( ConverterSetup converterSetup : converterSetups )
		{
			converterSetup.setDisplayRange( min.getCurrentValue(), max.getCurrentValue() );
		}
	}
}
