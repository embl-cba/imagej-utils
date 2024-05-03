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
package de.embl.cba.bdv.utils.behaviour;

import bdv.util.Bdv;
import de.embl.cba.bdv.utils.converters.RandomARGBConverter;
import de.embl.cba.bdv.utils.*;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

public class BehaviourRandomColorLutSeedChangeEventHandler
{
	final Bdv bdv;
	final RandomARGBConverter randomARGBLUT;
	final String sourceName;

	private String trigger = "ctrl L";

	private final Behaviours behaviours;

	public BehaviourRandomColorLutSeedChangeEventHandler( Bdv bdv, RandomARGBConverter randomARGBConverter, String sourceName )
	{
		this.bdv = bdv;
		this.randomARGBLUT = randomARGBConverter;
		this.sourceName = sourceName;

		this.behaviours = new Behaviours( new InputTriggerConfig() );
		behaviours.install( this.bdv.getBdvHandle().getTriggerbindings(), "bdv-random-color-shuffling-" + sourceName );

		installRandomColorShufflingBehaviour( );
	}


	private void installRandomColorShufflingBehaviour( )
	{
		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) ->
		{
			shuffleRandomLUT();
		}, sourceName + "-shuffle-random-colors", trigger );
	}


	private void shuffleRandomLUT()
	{
		randomARGBLUT.setSeed( randomARGBLUT.getSeed() + 1 );
		BdvUtils.repaint( bdv );
	}

}
