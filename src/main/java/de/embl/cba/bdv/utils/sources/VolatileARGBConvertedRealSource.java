package de.embl.cba.bdv.utils.sources;

import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.converters.SelectableVolatileARGBConverter;
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
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;

public class VolatileARGBConvertedRealSource < R extends RealType< R > > implements Source< VolatileARGBType >
{
    private final Source source;
    private Converter< RealType, VolatileARGBType > converter;
    final private InterpolatorFactory< VolatileARGBType, RandomAccessible< VolatileARGBType > >[] interpolatorFactories;

    private AffineTransform3D[] mipmapTransforms;
    {
        interpolatorFactories = new InterpolatorFactory[]{
                new NearestNeighborInterpolatorFactory< VolatileARGBType >(),
                new ClampingNLinearInterpolatorFactory< VolatileARGBType >()
        };
    }

    public VolatileARGBConvertedRealSource( Source< RealType > source, Converter< RealType, VolatileARGBType > converter )
    {
        this.source = source;
        this.converter = converter;
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
    }

    @Override
    public VolatileARGBType getType() {
        return new VolatileARGBType();
    }

    @Override
    public String getName() {
        return source.getName();
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

    public void setSelectableVolatileARGBConverter( Converter< RealType, VolatileARGBType > selectableVolatileARGBConverter )
    {
        this.converter = selectableVolatileARGBConverter;
    }

    public Converter< RealType, VolatileARGBType > getSelectableVolatileARGBConverter()
    {
        return converter;
    }

    public SelectableVolatileARGBConverter getSelectableConverter()
    {
        if ( converter instanceof SelectableVolatileARGBConverter )
        {
            return (SelectableVolatileARGBConverter) converter;
        }
        else
        {
            return null;
        }
    }

    public RandomAccessibleInterval< RealType > getWrappedRealSource( int t, int mipMapLevel )
    {
        return source.getSource( t, mipMapLevel );
    }

    public Source< RealType > getWrappedRealSource( )
    {
        return source;
    }

}
