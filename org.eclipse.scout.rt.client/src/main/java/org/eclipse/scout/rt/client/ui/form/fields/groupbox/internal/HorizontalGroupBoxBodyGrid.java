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
package org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.matrix.HorizontalGridMatrix;

/**
 * @author Andreas Hoegger
 * @since 4.0.0 M6 25.02.2014
 */
public class HorizontalGroupBoxBodyGrid extends AbstractGroupBoxBodyGrid {

  @Override
  protected void layoutAllDynamic(List<IFormField> fields) {
    HorizontalGridMatrix matrix = new HorizontalGridMatrix(getGridColumnCount());
    matrix.computeGridData(fields);
    setGridRows(matrix.getRowCount());
  }

}
