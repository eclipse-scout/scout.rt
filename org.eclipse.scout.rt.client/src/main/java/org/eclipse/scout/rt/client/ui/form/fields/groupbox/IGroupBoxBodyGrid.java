package org.eclipse.scout.rt.client.ui.form.fields.groupbox;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeFieldGrid;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.HorizontalGroupBoxBodyGrid;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.VerticalSmartGroupBoxBodyGrid;

/**
 * This class is responsible to calculate {@link GridData} for all fields in a {@link IGroupBox}. Considering each
 * fields {@link IGroupBox#getGridDataHints()} the {@link IGroupBox#setGridDataInternal(GridData)} must be set.
 *
 * @author Andreas Hoegger
 * @since 4.0.0 M6 25.02.2014
 * @see {@link VerticalSmartGroupBoxBodyGrid}, {@link HorizontalGroupBoxBodyGrid}
 * @author awe
 */
public interface IGroupBoxBodyGrid extends ICompositeFieldGrid<IGroupBox> {

}
