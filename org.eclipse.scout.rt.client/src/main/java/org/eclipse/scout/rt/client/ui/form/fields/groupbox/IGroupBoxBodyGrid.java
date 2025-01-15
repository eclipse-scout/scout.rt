/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
 * @author awe
 * @see VerticalSmartGroupBoxBodyGrid
 * @see HorizontalGroupBoxBodyGrid
 * @since 4.0.0 M6 25.02.2014
 */
public interface IGroupBoxBodyGrid extends ICompositeFieldGrid<IGroupBox> {

}
