package develop;

import org.scijava.listeners.Listeners;

public class DebugListenersListError
{
	public static void main( String[] args )
	{
		final Listeners.List< Object > list = new Listeners.List<>();
		list.list.forEach( l -> l.toString() );
		final int size = list.list.size();
	}
}
