package de.embl.cba.morphometry.geometry.ellipsoids;

import de.embl.cba.transforms.utils.Transforms;
import ij.ImagePlus;
import mcib3d.geom.Object3D;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.image3d.ImageByte;
import net.imglib2.realtransform.AffineTransform3D;


public abstract class Ellipsoids3DImageSuite
{
	public static EllipsoidVectors fitEllipsoid( ImagePlus mask )
	{
		final ImageByte imageByte = new ImageByte( mask );
		Objects3DPopulation objects3DPopulation = new Objects3DPopulation( imageByte, 0 );
		final Object3D object = objects3DPopulation.getObject( 0 );

		final EllipsoidVectors ellipsoidVectors = new EllipsoidVectors();
		ellipsoidVectors.shortestAxis = object.getVectorAxis( 0 );
		ellipsoidVectors.middleAxis = object.getVectorAxis( 1 );
		ellipsoidVectors.longestAxis = object.getVectorAxis( 2 );
		ellipsoidVectors.center = object.getCenterAsArray();

		return ellipsoidVectors;
	}


	public static AffineTransform3D createShortestAxisAlignmentTransform( EllipsoidVectors ellipsoidVectors )
	{
		AffineTransform3D translation = new AffineTransform3D();
		translation.translate( ellipsoidVectors.center  );
		translation = translation.inverse();

		final double[] zAxis = new double[]{ 0, 0, 1 };
		final double[] shortestAxis = ellipsoidVectors.shortestAxis.getArray();
		AffineTransform3D rotation = Transforms.getRotationTransform3D( zAxis, shortestAxis );

		AffineTransform3D combinedTransform = translation.preConcatenate( rotation );

		return combinedTransform;
	}


	public static AffineTransform3D createAlignmentTransform( EllipsoidVectors ellipsoidVectors )
	{
		AffineTransform3D transform3D = new AffineTransform3D();
		transform3D.translate( ellipsoidVectors.center  );
		transform3D = transform3D.inverse();

		final double[] xAxis = new double[]{ 1, 0, 0 };
		final double[] longestAxis = ellipsoidVectors.longestAxis.getArray();
		AffineTransform3D longAxisRotation = Transforms.getRotationTransform3D( xAxis, longestAxis );
		transform3D = transform3D.preConcatenate( longAxisRotation );

		final double[] zAxis = new double[]{ 0, 0, 1 };
		final double[] shortestAxis = ellipsoidVectors.shortestAxis.getArray();
		final double[] shortestAxisInLongestAxisAlignedCoordinateSystem = new double[ 3 ];
		longAxisRotation.apply( shortestAxis, shortestAxisInLongestAxisAlignedCoordinateSystem );

		AffineTransform3D shortAxisRotation = Transforms.getRotationTransform3D( zAxis, shortestAxisInLongestAxisAlignedCoordinateSystem );
		transform3D = transform3D.preConcatenate( shortAxisRotation );

		return transform3D;
	}



}
