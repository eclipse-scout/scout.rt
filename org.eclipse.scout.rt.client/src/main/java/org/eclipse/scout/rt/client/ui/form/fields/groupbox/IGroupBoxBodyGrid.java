/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.groupbox;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.HorizontalGroupBoxBodyGrid;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.VerticalSmartGroupBoxBodyGrid;

/**
 * This class is responsible to calculate {@link GridData} for all fields in a {@link IGroupBox}. Considering each
 * fields {@link IGroupBox#getGridDataHints()} the {@link IGroupBox#setGridDataInternal(GridData)} must be set.
 * 
 * @author Andreas Hoegger
 * @since 4.0.0 M6 25.02.2014
 * @see {@link VerticalSmartGroupBoxBodyGrid}, {@link HorizontalGroupBoxBodyGrid}
 */
public interface IGroupBoxBodyGrid {

  /**
   * validate the grid data of all fields in the given group box
   */
  void validate(IGroupBox groupBox);

  /**
   * @return the column count of the calculated grid
   */
  int getGridColumnCount();

  /**
   * @return the row count of the calculated grid
   */
  int getGridRowCount();

}
