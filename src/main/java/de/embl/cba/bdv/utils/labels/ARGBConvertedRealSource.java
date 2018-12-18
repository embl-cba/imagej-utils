package de.embl.cba.bdv.utils.labels;

import bdv.AbstractViewerSetupImgLoader;
import bdv.ViewerImgLoader;
import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import mpicbg.spim.data.SpimData;
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
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.*;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;

import java.util.HashSet;
import java.util.Set;

public class ARGBConvertedRealSource< R extends RealType< R >, V extends AbstractVolatileRealType< R, V > > implements Source< VolatileARGBType >

{
    private final long setupId;
    private final SpimData spimData;
    private Converter< V, VolatileARGBType > volatileARGBConverter;

    private AbstractViewerSetupImgLoader< R, V > setupImgLoader;
    final private InterpolatorFactory< VolatileARGBType, RandomAccessible< VolatileARGBType > >[] interpolatorFactories;
    private AffineTransform3D viewRegistration;
    private AffineTransform3D[] mipmapTransforms;
    {
        interpolatorFactories = new InterpolatorFactory[]{
                new NearestNeighborInterpolatorFactory< VolatileARGBType >(),
                new ClampingNLinearInterpolatorFactory< VolatileARGBType >()
        };
    }

    public ARGBConvertedRealSource( SpimData spimdata, final int setupId, Converter< V, VolatileARGBType > volatileARGBConverter )
    {
        this.setupId = setupId;
        this.spimData = spimdata;
        this.volatileARGBConverter = volatileARGBConverter;
        this.viewRegistration = spimData.getViewRegistrations().getViewRegistration( 0, 0 ).getModel().copy();
        ViewerImgLoader imgLoader = ( ViewerImgLoader ) this.spimData.getSequenceDescription().getImgLoader();
        this.setupImgLoader = ( AbstractViewerSetupImgLoader ) imgLoader.getSetupImgLoader( setupId );
        this.mipmapTransforms = this.setupImgLoader.getMipmapTransforms();

        final Type type = setupImgLoader.getImageType().createVariable();

        final Type volatileType = setupImgLoader.getVolatileImageType().createVariable();

        try
        {
            if (! ( volatileType instanceof VolatileUnsignedByteType
                    || volatileType instanceof VolatileUnsignedShortType
                    || volatileType instanceof VolatileUnsignedLongType )) {
                throw new Exception("Data type not supported for creating a label source: " + volatileType.toString() );
            }
        }
        catch ( Exception e)
        {
            e.printStackTrace();
        }

    }

    public void setVolatileARGBConverter( Converter< V, VolatileARGBType > volatileARGBConverter )
    {
        this.volatileARGBConverter = volatileARGBConverter;
    }

    public Converter< V, VolatileARGBType > getVolatileARGBConverter()
    {
        return volatileARGBConverter;
    }

    public RandomAccessibleInterval< R > getWrappedSource( int t, int mipMapLevel )
    {
        return setupImgLoader.getImage( t, mipMapLevel );
    }

    @Override
    public boolean isPresent( final int t )
    {
        boolean flag = t >= 0 && t < this.spimData.getSequenceDescription().getTimePoints().size();
        return flag;
    }

    @Override
    public RandomAccessibleInterval< VolatileARGBType > getSource( final int t, final int mipMapLevel )
    {
        return Converters.convert(
                        setupImgLoader.getVolatileImage( t, mipMapLevel ),
                        volatileARGBConverter,
                        new VolatileARGBType() );
    }

    @Override
    public RealRandomAccessible< VolatileARGBType > getInterpolatedSource(final int t, final int level, final Interpolation method) {
        final ExtendedRandomAccessibleInterval<VolatileARGBType, RandomAccessibleInterval<VolatileARGBType>> extendedSource =
                Views.extendValue(getSource(t, level), new VolatileARGBType(0));
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
        final AffineTransform3D sourceTransform = viewRegistration.copy().concatenate( mipmapTransforms[ level ] );
        transform.set( sourceTransform );
    }

    @Override
    public VolatileARGBType getType() {
        return new VolatileARGBType();
    }

    @Override
    public String getName() {
        return "labels";
    }

    @Override
    public VoxelDimensions getVoxelDimensions() {
        return null;
    }

    @Override
    public int getNumMipmapLevels() {
        return setupImgLoader.getMipmapTransforms().length;
    }


}
