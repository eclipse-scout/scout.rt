/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.ext.table;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

/**
 * <h3>TableViewerEx</h3> publish the applyEditorValue method
 * <p>
 * {@inheritDoc}
 */
public class TableViewerEx extends TableViewer {
  private static final long serialVersionUID = 1L;

  /**
   * {@inheritDoc}
   */
  public TableViewerEx(Composite parent) {
    super(parent);
  }

  /**
   * {@inheritDoc}
   */
  public TableViewerEx(Composite parent, int style) {
    super(parent, style);
  }

  /**
   * {@inheritDoc}
   */
  public TableViewerEx(Table table) {
    super(table);
  }

  @Override
  protected ViewerRow internalCreateNewRowPart(int style, int rowIndex) {
    ViewerRow viewerRow = super.internalCreateNewRowPart(style, rowIndex);
    if (getTable().getData(WidgetUtil.CUSTOM_VARIANT) != null) {
      viewerRow.getItem().setData(WidgetUtil.CUSTOM_VARIANT, getTable().getData(WidgetUtil.CUSTOM_VARIANT));
    }
    return viewerRow;
  }

}
