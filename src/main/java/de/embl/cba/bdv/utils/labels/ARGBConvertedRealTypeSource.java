package de.embl.cba.bdv.utils.labels;

import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.interpolation.randomaccess.ClampingNLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.AbstractVolatileRealType;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;

public class ARGBConvertedRealTypeSource implements Source< VolatileARGBType >

{
    private final Source source;
    //private final long setupId;
    private final String name;
    private Converter< RealType, VolatileARGBType > converter;

//    private AbstractViewerSetupImgLoader< R, V > setupImgLoader;
    final private InterpolatorFactory< VolatileARGBType, RandomAccessible< VolatileARGBType > >[] interpolatorFactories;

    //    private AffineTransform3D viewRegistration;

    private AffineTransform3D[] mipmapTransforms;
    {
        interpolatorFactories = new InterpolatorFactory[]{
                new NearestNeighborInterpolatorFactory< VolatileARGBType >(),
                new ClampingNLinearInterpolatorFactory< VolatileARGBType >()
        };
    }

    public ARGBConvertedRealTypeSource( Source< RealType > source, String name, Converter< RealType, VolatileARGBType > converter )
    {
        this.source = source;
        this.name = name;
        this.converter = converter;
        //this.viewRegistration = this.source.getViewRegistrations().getViewRegistration( 0, 0 ).getModel().copy();
        //ViewerImgLoader imgLoader = ( ViewerImgLoader ) this.source.getSequenceDescription().getImgLoader();
        //this.setupImgLoader = ( AbstractViewerSetupImgLoader ) imgLoader.getSetupImgLoader( setupId );
        //this.mipmapTransforms = this.setupImgLoader.getMipmapTransforms();
    }

    @Override
    public boolean isPresent( final int t )
    {
       return this.source.isPresent( t );
    }

    @Override
    public RandomAccessibleInterval< VolatileARGBType > getSource( final int t, final int mipMapLevel )
    {
        return Converters.convert(
                        source.getSource( t, mipMapLevel ),
                        converter,
                        new VolatileARGBType() );
    }

    @Override
    public RealRandomAccessible< VolatileARGBType > getInterpolatedSource(final int t, final int level, final Interpolation method)
    {
        final ExtendedRandomAccessibleInterval<VolatileARGBType, RandomAccessibleInterval<VolatileARGBType>> extendedSource =
                Views.extendValue( getSource(t, level), new VolatileARGBType(0));
        switch (method) {
            case NLINEAR:
                return Views.interpolate(extendedSource, interpolatorFactories[1]);
            default:
                return Views.interpolate(extendedSource, interpolatorFactories[0]);
        }
    }

    @Override
    public void getSourceTransform( int t, int level, AffineTransform3D transform )
    {
        source.getSourceTransform( t, level, transform );
//        final AffineTransform3D sourceTransform = viewRegistration.copy().concatenate( mipmapTransforms[ level ] );
//        transform.set( sourceTransform );
    }

    @Override
    public VolatileARGBType getType() {
        return new VolatileARGBType();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public VoxelDimensions getVoxelDimensions()
    {
        return source.getVoxelDimensions();
    }

    @Override
    public int getNumMipmapLevels()
    {
        return source.getNumMipmapLevels();
    }

    public void setConverter( Converter< RealType, VolatileARGBType > converter )
    {
        this.converter = converter;
    }

    public Converter< RealType, VolatileARGBType > getConverter()
    {
        return converter;
    }

    public RandomAccessibleInterval< RealType > getWrappedSource( int t, int mipMapLevel )
    {
        return source.getSource( t, mipMapLevel );
    }

}
