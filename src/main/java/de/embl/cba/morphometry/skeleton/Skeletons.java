package de.embl.cba.morphometry.skeleton;

import de.embl.cba.morphometry.Utils;
import de.embl.cba.morphometry.regions.RegionAndSize;
import de.embl.cba.morphometry.regions.Regions;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.algorithm.morphology.table2d.Branchpoints;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.logic.BitType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.util.ArrayList;

public class Skeletons
{
	public static RandomAccessibleInterval< BitType > longestBranch(
			RandomAccessibleInterval< BitType > skeleton )
	{

		final RandomAccessibleInterval< BitType > longestBranch =
				Utils.copyAsArrayImg( skeleton );

		removeBranchpoints( longestBranch );

		Regions.onlyKeepLargestRegion( longestBranch,
				ConnectedComponents.StructuringElement.EIGHT_CONNECTED );

		return longestBranch;
	}

	public static ArrayList< Long > branchLengths(
			RandomAccessibleInterval< BitType > skeleton )
	{
		final RandomAccessibleInterval< BitType > branches =
				Utils.copyAsArrayImg( skeleton );

		removeBranchpoints( branches );

		final ArrayList< RegionAndSize > regions = Regions.getSizeSortedRegions( branches,
				ConnectedComponents.StructuringElement.EIGHT_CONNECTED );

		final ArrayList< Long > branchLengthPixels = new ArrayList<>();

		for ( RegionAndSize regionAndSize : regions )
			branchLengthPixels.add( regionAndSize.getSize() );

		return branchLengthPixels;
	}

	public static RandomAccessibleInterval< BitType > branchPoints(
			RandomAccessibleInterval< BitType > skeleton )
	{
		RandomAccessibleInterval< BitType > branchPoints =
				ArrayImgs.bits( Intervals.dimensionsAsLongArray( skeleton ) );

		Branchpoints.branchpoints(
				Views.extendBorder( Views.zeroMin( skeleton ) ),
				Views.flatIterable( branchPoints ) );

		Views.translate( branchPoints, Intervals.minAsLongArray( skeleton ) );

		return branchPoints;
	}

	static void removeBranchpoints( RandomAccessibleInterval< BitType > skeleton )
	{
		final RandomAccessibleInterval< BitType > branchpoints = branchPoints( skeleton );

		LoopBuilder.setImages( branchpoints, skeleton ).forEachPixel( ( b, s ) ->
		{
			if ( b.get() ) s.set( false );
		});
	}


	public static RandomAccessibleInterval< BitType > skeleton(
			RandomAccessibleInterval< BitType > input,
			OpService opService )
	{
		final RandomAccessibleInterval< BitType > thin = Utils.createEmptyCopy( input );
		opService.morphology().thinGuoHall( thin, input );
		return thin;
	}
}
