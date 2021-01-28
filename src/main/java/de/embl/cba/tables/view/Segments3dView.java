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
package de.embl.cba.tables.view;

import bdv.viewer.Source;
import customnode.CustomTriangleMesh;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.objects3d.FloodFill;
import de.embl.cba.tables.Logger;
import de.embl.cba.tables.Utils;
import de.embl.cba.tables.color.ColorUtils;
import de.embl.cba.tables.color.ColoringModel;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.ij3d.AnimatedViewAdjuster;
import de.embl.cba.tables.ij3d.UniverseUtils;
import de.embl.cba.tables.image.ImageSourcesModel;
import de.embl.cba.tables.imagesegment.ImageSegment;
import de.embl.cba.tables.mesh.MeshExtractor;
import de.embl.cba.tables.mesh.MeshUtils;
import de.embl.cba.tables.select.SelectionListener;
import de.embl.cba.tables.select.SelectionModel;
import ij3d.Content;
import ij3d.Image3DUniverse;
import ij3d.UniverseListener;
import isosurface.MeshEditor;
import net.imglib2.FinalInterval;
import net.imglib2.FinalRealInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.scijava.java3d.View;
import org.scijava.vecmath.Color3f;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Segments3dView < T extends ImageSegment >
{
	private final List< T > segments;
	private final SelectionModel< T > selectionModel;
	private final SelectionColoringModel< T > selectionColoringModel;
	private final ImageSourcesModel imageSourcesModel;

	private Image3DUniverse universe;
	private T recentFocus;
	private ConcurrentHashMap< T, Content > segmentToContent;
	private ConcurrentHashMap< Content, T > contentToSegment;
	private double transparency;
	private boolean isListeningToUniverse;
	private int meshSmoothingIterations;
	private int segmentFocusAnimationDurationMillis;
	private boolean contentModificationInProgress;
	private double segmentFocusZoomLevel;
	private double segmentFocusDxyMin;
	private double segmentFocusDzMin;
	private long maxNumSegmentVoxels;
	private String objectsName;
	private Component parentComponent;
	private boolean showSelectedSegments = true;
	private ConcurrentHashMap< T, CustomTriangleMesh > segmentToTriangleMesh;
	private ExecutorService executorService;
	private boolean recomputeMeshes = false;
	private double voxelSpacing = 0; // 0 = auto

	public Segments3dView(
			final List< T > segments,
			final SelectionModel< T > selectionModel,
			final SelectionColoringModel< T > selectionColoringModel,
			ImageSourcesModel imageSourcesModel )
	{
		this( segments,
				selectionModel,
				selectionColoringModel,
				imageSourcesModel,
				null );
	}

	public Segments3dView(
			final List< T > segments,
			final SelectionModel< T > selectionModel,
			final SelectionColoringModel< T > selectionColoringModel,
			ImageSourcesModel imageSourcesModel,
			Image3DUniverse universe )
	{
		this.segments = segments;
		this.selectionModel = selectionModel;
		this.selectionColoringModel = selectionColoringModel;
		this.imageSourcesModel = imageSourcesModel;
		this.universe = universe;

		this.transparency = 0.0;
		this.meshSmoothingIterations = 5;
		this.segmentFocusAnimationDurationMillis = 750;
		this.segmentFocusZoomLevel = 0.8;
		this.segmentFocusDxyMin = 20.0;
		this.segmentFocusDzMin = 20.0;
		this.maxNumSegmentVoxels = 100 * 100 * 100;

		this.objectsName = "";

		this.executorService = Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() );

		this.segmentToContent = new ConcurrentHashMap<>();
		this.contentToSegment = new ConcurrentHashMap<>();

		registerAsSelectionListener( this.selectionModel );
		registerAsColoringListener( this.selectionColoringModel );
	}

	public void setObjectsName( String objectsName )
	{
		if ( objectsName == null )
			throw new RuntimeException( "Cannot set objects name in Segments3dView to null." );

		this.objectsName = objectsName;
	}

	public void setParentComponent( Component parentComponent )
	{
		this.parentComponent = parentComponent;
	}

	public void setTransparency( double transparency )
	{
		this.transparency = transparency;
	}

	public void setMeshSmoothingIterations( int iterations )
	{
		this.meshSmoothingIterations = iterations;
	}

	public void setSegmentFocusAnimationDurationMillis( int duration )
	{
		this.segmentFocusAnimationDurationMillis = duration;
	}

	public void setSegmentFocusZoomLevel( double segmentFocusZoomLevel )
	{
		this.segmentFocusZoomLevel = segmentFocusZoomLevel;
	}

	public void setSegmentFocusDxyMin( double segmentFocusDxyMin )
	{
		this.segmentFocusDxyMin = segmentFocusDxyMin;
	}

	public void setSegmentFocusDzMin( double segmentFocusDzMin )
	{
		this.segmentFocusDzMin = segmentFocusDzMin;
	}

	public void setMaxNumSegmentVoxels( long maxNumSegmentVoxels )
	{
		this.maxNumSegmentVoxels = maxNumSegmentVoxels;
	}

	public Image3DUniverse getUniverse()
	{
		return universe;
	}


	private void registerAsColoringListener( ColoringModel< T > coloringModel )
	{
		coloringModel.listeners().add( () -> adaptSegmentColors() );
	}

	private void adaptSegmentColors()
	{
		for ( T segment : segmentToContent.keySet() )
		{
			executorService.submit( () ->
			{
				final Color3f color3f = getColor3f( segment );
				final Content content = segmentToContent.get( segment );
				content.setColor( color3f );
			});
		}
	}

	public void registerAsSelectionListener( SelectionModel< T > selectionModel )
	{
		selectionModel.listeners().add( new SelectionListener< T >()
		{
			@Override
			public synchronized void selectionChanged()
			{
				if ( ! showSelectedSegments ) return;

				updateView( false );
			}

			@Override
			public synchronized void focusEvent( T selection )
			{
				if ( ! showSelectedSegments ) return;

				initUniverseAndListener();
				if ( universe.getContents().size() == 0 ) return;
				if ( selection == recentFocus ) return;
				if ( ! segmentToContent.containsKey( selection ) ) return;

				recentFocus = selection;

				final AnimatedViewAdjuster adjuster =
						new AnimatedViewAdjuster(
								universe,
								AnimatedViewAdjuster.ADJUST_BOTH );

				adjuster.apply(
						segmentToContent.get( selection ),
						30,
						segmentFocusAnimationDurationMillis,
						segmentFocusZoomLevel,
						segmentFocusDxyMin,
						segmentFocusDzMin );
			}
		} );
	}

	private synchronized void updateView( boolean recomputeMeshes )
	{
		contentModificationInProgress = true;
		updateSelectedSegments( recomputeMeshes );
		removeUnselectedSegments();
		contentModificationInProgress = false;
	}

	private void removeUnselectedSegments( )
	{
		final Set< T > selectedSegments = selectionModel.getSelected();
		final Set< T > currentSegments = segmentToContent.keySet();
		final Set< T > remove = new HashSet<>();

		for ( T segment : currentSegments )
			if ( ! selectedSegments.contains( segment ) )
				remove.add( segment );

		for( T segment : remove )
			removeSegmentFrom3DView( segment );
	}

	private synchronized void updateSelectedSegments( boolean recomputeMeshes )
	{
		final Set< T > selected = selectionModel.getSelected();

		initUniverseAndListener();

		for ( T segment : selected )
		{
			if ( ! segmentToContent.containsKey( segment ) || recomputeMeshes )
			{
				if ( recomputeMeshes ) removeSegmentFrom3DView( segment );
				final CustomTriangleMesh mesh = createSmoothCustomTriangleMesh( segment, voxelSpacing, recomputeMeshes );
				addMeshToUniverse( segment, mesh );
			}
		}
	}

	/**
	 * TODO: On Windows 10 this seems to throw an error
	 *
	 * @param segments
	 */
	private synchronized void showSelectedSegmentsMultiThreaded( Set< T > segments )
	{
		initUniverseAndListener();

		final ArrayList< Future > futures = new ArrayList<>();
		for ( T segment : segments )
			if ( ! segmentToContent.containsKey( segment ) )
				futures.add(
						executorService.submit( () ->
								{
									final CustomTriangleMesh mesh = createSmoothCustomTriangleMesh( segment, voxelSpacing, recomputeMeshes );
									if ( mesh != null )
										addMeshToUniverse( segment, mesh  );
									else
										Logger.info( "Error creating mesh of segment " + segment.labelId() );
								}
						) );

		Utils.fetchFutures( futures );
	}

	private synchronized void removeSegmentFrom3DView( T segment )
	{
		final Content content = segmentToContent.get( segment );
		if ( content != null && universe != null )
		{
			universe.removeContent( content.getName() );
			segmentToContent.remove( segment );
			contentToSegment.remove( content );
		}
	}

	private CustomTriangleMesh createSmoothCustomTriangleMesh( T segment, double voxelSpacing, boolean recomputeMesh )
	{
		CustomTriangleMesh triangleMesh = createCustomTriangleMesh( segment, voxelSpacing, recomputeMesh );
		MeshEditor.smooth2( triangleMesh, meshSmoothingIterations );
		return triangleMesh;
	}

	private CustomTriangleMesh createCustomTriangleMesh( T segment, double voxelSpacing, boolean recomputeMesh )
	{
		createAndSetMesh( segment, voxelSpacing, recomputeMesh );
		CustomTriangleMesh triangleMesh = MeshUtils.asCustomTriangleMesh( segment.getMesh() );
		triangleMesh.setColor( getColor3f( segment ) );
		return triangleMesh;
	}

	private void createAndSetMesh( T segment, double voxelSpacing, boolean recomputeMesh )
	{
		if ( segment.getMesh() == null || recomputeMesh )
		{
			final float[] mesh = createMesh( segment, voxelSpacing );
			if ( mesh == null ) throw new RuntimeException( "Could not create mesh for segment of image " + segment.labelId() );
			segment.setMesh( mesh );
		}
	}

	private float[] createMesh( ImageSegment segment, double voxelSpacing )
	{
		final Source< ? > labelsSource = imageSourcesModel.sources().get( segment.imageId() ).source();

		Integer level = getLevel( segment, labelsSource, voxelSpacing );
		double[] voxelSpacings = Utils.getVoxelSpacings( labelsSource ).get( level );
		UniverseUtils.logVoxelSpacing( labelsSource, voxelSpacings );

		final RandomAccessibleInterval< ? extends RealType< ? > > labelsRAI = getLabelsRAI( segment, level );

		if ( segment.boundingBox() == null ) setSegmentBoundingBox( segment, labelsRAI, voxelSpacings );

		FinalInterval boundingBox = intervalInVoxelUnits( segment.boundingBox(), voxelSpacings );
		final long numElements = Intervals.numElements( boundingBox );

		if ( voxelSpacing == 0 ) // auto-resolution
		{
			if ( numElements > maxNumSegmentVoxels )
			{
				Logger.error( "3D View:\n" +
						"The bounding box of the selected segment has " + numElements + " voxels.\n" +
						"The maximum enabled number is " + maxNumSegmentVoxels + ".\n" +
						"Thus the image segment will not be displayed in 3D." );
				return null;
			}
		}

		final MeshExtractor meshExtractor = new MeshExtractor(
				Views.extendZero( ( RandomAccessibleInterval ) labelsRAI ),
				boundingBox,
				new AffineTransform3D(),
				new int[]{ 1, 1, 1 },
				() -> false );

		final float[] meshCoordinates = meshExtractor.generateMesh( segment.labelId() );

		for ( int i = 0; i < meshCoordinates.length; )
		{
			meshCoordinates[ i++ ] *= voxelSpacings[ 0 ];
			meshCoordinates[ i++ ] *= voxelSpacings[ 1 ];
			meshCoordinates[ i++ ] *= voxelSpacings[ 2 ];
		}

		if ( meshCoordinates.length == 0 )
		{
			Logger.warn( "Could not find any pixels for segment with label " + segment.labelId()
					+ "\nwithin bounding box " + boundingBox );
			return null;
		}

		return meshCoordinates;
	}

	private Integer getLevel( ImageSegment segment, Source< ? > labelsSource, double voxelSpacing )
	{
		Integer level;

		if ( voxelSpacing != 0 )
		{
			level = UniverseUtils.getLevel( labelsSource, voxelSpacing );
		}
		else // auto-resolution
		{
			if ( segment.boundingBox() == null )
			{
				Logger.error( "3D View:\n" +
						"Automated resolution level selection is enabled, but the segment has no bounding box.\n" +
						"This combination is currently not possible." );
				level = null;
			}
			else
			{
				final ArrayList< double[] > voxelSpacings = Utils.getVoxelSpacings( labelsSource );

				for ( level = 0; level < voxelSpacings.size(); level++ )
				{
					FinalInterval boundingBox = intervalInVoxelUnits( segment.boundingBox(), voxelSpacings.get( level ) );

					final long numElements = Intervals.numElements( boundingBox );

					if ( numElements <= maxNumSegmentVoxels )
						break;
				}
			}
		}

		return level;
	}

	public boolean showSelectedSegments()
	{
		return showSelectedSegments;
	}

	public synchronized void showSelectedSegments( boolean showSelectedSegments, boolean recomputeMeshes )
	{
		this.showSelectedSegments = showSelectedSegments;

		if ( showSelectedSegments )
		{
			updateView( recomputeMeshes );
		}
		else
		{
			removeSelectedSegmentsFromView();
		}
	}

	private void removeSelectedSegmentsFromView()
	{
		final Set< T > selected = selectionModel.getSelected();

		for ( T segment : selected )
		{
			removeSegmentFrom3DView( segment );
		}
	}

	private void setSegmentBoundingBox(
			ImageSegment segment,
			RandomAccessibleInterval< ? extends RealType< ? > > labelsRAI,
			double[] voxelSpacing )
	{
		final long[] voxelCoordinate = getSegmentCoordinateVoxels( segment, voxelSpacing );

		final FloodFill floodFill = new FloodFill(
				labelsRAI,
				new DiamondShape( 1 ),
				1000 * 1000 * 1000L );

		floodFill.run( voxelCoordinate );
		final RandomAccessibleInterval mask = floodFill.getCroppedRegionMask();

		final int numDimensions = segment.numDimensions();
		final double[] min = new double[ numDimensions ];
		final double[] max = new double[ numDimensions ];
		for ( int d = 0; d < numDimensions; d++ )
		{
			min[ d ] = mask.min( d ) * voxelSpacing[ d ];
			max[ d ] = mask.max( d ) * voxelSpacing[ d ];
		}

		segment.setBoundingBox( new FinalRealInterval( min, max ) );
	}

	private long[] getSegmentCoordinateVoxels(
			ImageSegment segment,
			double[] calibration )
	{
		final long[] voxelCoordinate = new long[ segment.numDimensions() ];
		for ( int d = 0; d < segment.numDimensions(); d++ )
			voxelCoordinate[ d ] = ( long ) (
					segment.getDoublePosition( d ) / calibration[ d ] );
		return voxelCoordinate;
	}

	private FinalInterval intervalInVoxelUnits(
			FinalRealInterval realInterval,
			double[] calibration )
	{
		final long[] min = new long[ 3 ];
		final long[] max = new long[ 3 ];
		for ( int d = 0; d < 3; d++ )
		{
			min[ d ] = (long) ( realInterval.realMin( d ) / calibration[ d ] );
			max[ d ] = (long) ( realInterval.realMax( d ) / calibration[ d ] );
		}
		return new FinalInterval( min, max );
	}

	private RandomAccessibleInterval< ? extends RealType< ? > >
	getLabelsRAI( ImageSegment segment, int level )
	{
		final Source< ? > labelsSource
				= imageSourcesModel.sources().get( segment.imageId() ).source();

		final RandomAccessibleInterval< ? extends RealType< ? > > rai =
				BdvUtils.getRealTypeNonVolatileRandomAccessibleInterval(
						labelsSource, 0, level );

		return rai;
	}

	private void addMeshToUniverse( T segment, CustomTriangleMesh mesh )
	{
		if ( mesh == null )
			throw new RuntimeException( "Mesh of segment " + objectsName + "_" + segment.labelId() + " is null." );

		if ( universe == null )
			throw new RuntimeException( "Universe is null." );

		final Content content = universe.addCustomMesh( mesh, objectsName + "_" + segment.labelId() );

		content.setTransparency( ( float ) transparency );
		content.setLocked( true );

		segmentToContent.put( segment, content );
		contentToSegment.put( content, segment );

		universe.setAutoAdjustView( false );
	}

	public synchronized void initUniverseAndListener()
	{
		if ( universe == null )
			universe = new Image3DUniverse();

		UniverseUtils.showUniverseWindow( universe, parentComponent );

		if ( ! isListeningToUniverse )
			isListeningToUniverse = addUniverseListener();
	}

	private boolean addUniverseListener()
	{
		universe.addUniverseListener( new UniverseListener()
		{

			@Override
			public void transformationStarted( View view )
			{

			}

			@Override
			public void transformationUpdated( View view )
			{

				// TODO: maybe try to synch this with the Bdv View

				//				final Transform3D transform3D = new Transform3D();
//			view.getUserHeadToVworld( transform3D );

//				final Transform3D transform3D = new Transform3D();
//			universe.getVworldToCamera( transform3D );
//				System.out.println( transform3D );

//				final Transform3D transform3DInverse = new Transform3D();
//				universe.getVworldToCameraInverse( transform3DInverse );
//				System.out.println( transform3DInverse );

//				final TransformGroup transformGroup =
//						universe.getViewingPlatform()
//								.getMultiTransformGroup().getTransformGroup(
//										DefaultUniverse.ZOOM_TG );
//
//				final Transform3D transform3D = new Transform3D();
//				transformGroup.getTransform( transform3D );
//
//				System.out.println( transform3D );
			}

			@Override
			public void transformationFinished( View view )
			{

			}

			@Override
			public void contentAdded( Content c )
			{

			}

			@Override
			public void contentRemoved( Content c )
			{

			}

			@Override
			public void contentChanged( Content c )
			{

			}

			@Override
			public void contentSelected( Content c )
			{
				if ( c == null ) return;

				if ( ! contentToSegment.containsKey( c ) )
					return;

				final T segment = contentToSegment.get( c );

				if ( selectionModel.isFocused( segment ) )
					return;
				else
				{
					recentFocus = segment; // This avoids "self-focusing"
					selectionModel.focus( segment );
				}

			}

			@Override
			public void canvasResized()
			{

			}

			@Override
			public void universeClosed()
			{

			}
		} );

		return true;
	}

	private Color3f getColor3f( T imageSegment )
	{
		final ARGBType argbType = new ARGBType();
		selectionColoringModel.convert( imageSegment, argbType );
		return new Color3f( ColorUtils.getColor( argbType ) );
	}

	public void setVoxelSpacing( double voxelSpacing )
	{
		this.voxelSpacing = voxelSpacing;
	}

	public double getVoxelSpacing()
	{
		return voxelSpacing;
	}

	public void close()
	{
		// TODO
	}
}
