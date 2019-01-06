package de.embl.cba.bdv.utils.behaviour;

import bdv.util.Bdv;
import de.embl.cba.bdv.utils.converters.RandomARGBConverter;
import de.embl.cba.bdv.utils.*;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

public class BehaviourRandomColorShufflingEventHandler
{
	final Bdv bdv;
	final RandomARGBConverter randomARGBLUT;
	final String sourceName;

	private String randomColorShufflingTrigger = "ctrl shift S";

	private final Behaviours behaviours;

	public BehaviourRandomColorShufflingEventHandler( Bdv bdv, RandomARGBConverter randomARGBConverter, String sourceName )
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
		}, sourceName + "-shuffle-random-colors", randomColorShufflingTrigger );
	}


	private void shuffleRandomLUT()
	{
		randomARGBLUT.setSeed( randomARGBLUT.getSeed() + 1 );
		BdvUtils.repaint( bdv );
	}

}
