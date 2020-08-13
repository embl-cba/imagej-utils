package de.embl.cba.morphometry;

public class PositionAndValue implements Comparable< PositionAndValue >
{
	double[] position;

	public double[] getPosition()
	{
		return position;
	}

	public double getValue()
	{
		return value;
	}

	double value;

	public int compareTo( PositionAndValue positionAndValue ) {

		double difference = this.value - positionAndValue.value;

		if ( difference > 0 ) return 1;
		if ( difference < 0 ) return -1;
		else return 0;

	}
}
