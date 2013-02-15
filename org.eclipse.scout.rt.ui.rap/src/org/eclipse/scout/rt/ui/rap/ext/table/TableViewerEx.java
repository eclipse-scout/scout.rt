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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.rap.rwt.RWT;
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
  public void applyEditorValue() {
    super.applyEditorValue();
  }

  @Override
  protected void triggerEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
    //Make sure editor is closed when clicking on another cell. Mainly necessary when using the second mouse button to open the context menu
    for (CellEditor editor : getCellEditors()) {
      if (editor != null && editor.isActivated()) {
        applyEditorValue();
      }
    }
    super.triggerEditorActivationEvent(event);
  }

  @Override
  protected ViewerRow internalCreateNewRowPart(int style, int rowIndex) {
    ViewerRow viewerRow = super.internalCreateNewRowPart(style, rowIndex);
    if (getTable().getData(RWT.CUSTOM_VARIANT) != null) {
      viewerRow.getItem().setData(RWT.CUSTOM_VARIANT, getTable().getData(RWT.CUSTOM_VARIANT));
    }
    return viewerRow;
  }

}
