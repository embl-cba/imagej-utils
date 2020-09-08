package de.embl.cba.morphometry.skeleton;

import de.embl.cba.morphometry.Algorithms;
import de.embl.cba.morphometry.Logger;
import de.embl.cba.morphometry.regions.Regions;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;

import java.util.ArrayList;

public class SkeletonCreator< T extends RealType< T > & NativeType< T > >
{

	final ArrayList< RandomAccessibleInterval< BitType > > masks;
	private final OpService opService;

	private ArrayList< RandomAccessibleInterval< BitType > > skeletons;
	private int closingRadius = 0;

	public SkeletonCreator( ArrayList< RandomAccessibleInterval< BitType > > masks,
							OpService opService )
	{
		this.masks = masks;
		this.opService = opService;
	}

	public void setClosingRadius( int closingRadius )
	{
		this.closingRadius = closingRadius;
	}

	public void run()
	{

		int tMin = 0;  // at this point the movie is already cropped in time, such that we can process the full movie
		int tMax = masks.size() - 1;

		skeletons = new ArrayList<>( );

		for ( int t = tMin; t <= tMax; ++t )
		{
			Logger.log( "Creating skeletons, frame " + ( t + 1 ) + " / " + ( ( tMax - tMin ) + 1 ) );

			final ImgLabeling< Integer, IntType > imgLabeling =
					Regions.asImgLabeling(
							masks.get( t ),
							ConnectedComponents.StructuringElement.FOUR_CONNECTED );

			final RandomAccessibleInterval< BitType > skeletons =
					Algorithms.createObjectSkeletons(
							imgLabeling,
							closingRadius, // TODO: Make a parameter
							opService );

			this.skeletons.add( skeletons );
		}

	}

	public ArrayList< RandomAccessibleInterval< BitType > > getSkeletons()
	{
		return skeletons;
	}

}
