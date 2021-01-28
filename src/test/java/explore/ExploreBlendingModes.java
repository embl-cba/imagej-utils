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
package explore;

import bdv.util.*;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.io.SPIMDataReaders;
import de.embl.cba.bdv.utils.render.AccumulateEMAndFMProjectorARGB;
import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.bdv.utils.sources.Sources;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.type.volatiles.VolatileARGBType;

public class ExploreBlendingModes
{
	public static void main( String[] args ) throws SpimDataException
	{
		/**
		 * EM Source
		 */

		final Source< VolatileARGBType > argbSource0 = SPIMDataReaders.openAsVolatileARGBTypeSource(
				ExploreBlendingModes.class.getResource( "../mri-stack.xml" ).getFile(), 0 );

		final BdvHandle bdvHandle = showSource( null, argbSource0, Metadata.Modality.EM );

		/**
		 * EM Source
		 */

		final Source< VolatileARGBType > argbSource1 = SPIMDataReaders.openAsVolatileARGBTypeSource(
				ExploreBlendingModes.class.getResource( "../mri-stack-shifted.xml" ).getFile(), 0 );

		showSource( bdvHandle, argbSource1, Metadata.Modality.EM );

		/**
		 * FM Source
		 */

		final Source< VolatileARGBType > argbSource2 = SPIMDataReaders.openAsVolatileARGBTypeSource(
				ExploreBlendingModes.class.getResource( "../mri-stack-shifted1.xml" ).getFile(), 0 );

		showSource( bdvHandle, argbSource2, Metadata.Modality.FM );
	}

	public static BdvHandle showSource(
			BdvHandle bdvHandle,
			Source< VolatileARGBType > source,
			Metadata.Modality em )
	{
		final Metadata metadata = new Metadata( source.getName() );
		metadata.modality = em;
		Sources.sourceToMetadata.put( source, metadata );

		return BdvFunctions.show( source, BdvOptions.options().addTo( bdvHandle ).accumulateProjectorFactory(  AccumulateEMAndFMProjectorARGB.factory ) ).getBdvHandle();
	}

}
