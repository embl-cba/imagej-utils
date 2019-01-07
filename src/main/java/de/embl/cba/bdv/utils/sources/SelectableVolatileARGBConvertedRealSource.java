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

public class SelectableVolatileARGBConvertedRealSource extends VolatileARGBConvertedRealSource
{
    private SelectableVolatileARGBConverter selectableVolatileARGBConverter;
    final private InterpolatorFactory< VolatileARGBType, RandomAccessible< VolatileARGBType > >[] interpolatorFactories;

    private AffineTransform3D[] mipmapTransforms;
    {
        interpolatorFactories = new InterpolatorFactory[]{
                new NearestNeighborInterpolatorFactory< VolatileARGBType >(),
                new ClampingNLinearInterpolatorFactory< VolatileARGBType >()
        };
    }

    public SelectableVolatileARGBConvertedRealSource( Source< RealType > source )
    {
        super( source, new SelectableVolatileARGBConverter(  ) );
    }

    public SelectableVolatileARGBConvertedRealSource(
            Source< RealType > source,
            SelectableVolatileARGBConverter selectableVolatileARGBConverter )
    {
        super( source, selectableVolatileARGBConverter );
        this.selectableVolatileARGBConverter = selectableVolatileARGBConverter;
    }

    public void setSelectableVolatileARGBConverter( SelectableVolatileARGBConverter selectableVolatileARGBConverter )
    {
        this.selectableVolatileARGBConverter = selectableVolatileARGBConverter;
    }

    public SelectableVolatileARGBConverter getSelectableVolatileARGBConverter()
    {
        return selectableVolatileARGBConverter;
    }

}
