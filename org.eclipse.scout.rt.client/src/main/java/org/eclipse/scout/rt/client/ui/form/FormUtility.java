/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;

public final class FormUtility {

  private FormUtility() {
  }

  public static String normalizeDisplayViewId(String viewId) {
    if (viewId == null) {
      return IForm.VIEW_ID_CENTER;
    }
    switch (viewId) {
      case IForm.VIEW_ID_NW:
      case IForm.VIEW_ID_W:
      case IForm.VIEW_ID_SW:

      case IForm.VIEW_ID_N:
      case IForm.VIEW_ID_CENTER:
      case IForm.VIEW_ID_S:

      case IForm.VIEW_ID_NE:
      case IForm.VIEW_ID_E:
      case IForm.VIEW_ID_SE:
        return viewId;
      default:
        return IForm.VIEW_ID_CENTER;
    }
  }

  /**
   * Complete the configurations of the complete field tree of the form. This method is normally called by the form's
   * constructor after the form initConfig and postInitConfig. This method is normally called before
   * {@link #initFormFields(IForm)}.
   */
  public static void rebuildFieldGrid(IForm form, boolean initMainBoxGridData) {
    IGroupBox rootGroupBox = form.getRootGroupBox();
    if (rootGroupBox == null) {
      return;
    }
    rebuildFieldGrid(rootGroupBox);
    if (initMainBoxGridData) {
      initRootBoxGridData(rootGroupBox);
    }
  }

  public static void rebuildFieldGrid(ICompositeField field) {
    field.visit(ICompositeField::rebuildFieldGrid, ICompositeField.class);
  }

  public static void initRootBoxGridData(ICompositeField rootBox) {
    // layout data for root group box
    GridData rootData = new GridData(rootBox.getGridDataHints());
    if (rootData.w == IFormField.FULL_WIDTH) {
      rootData.w = rootBox.getFieldGrid().getGridColumnCount();
    }
    rootBox.setGridDataInternal(rootData);
  }

  /**
   * With this method it's possible to set the mark strategy of all tab boxes of the given form.
   *
   * @param form
   *          the form
   * @param strategy
   *          one of {@link ITabBox#MARK_STRATEGY_EMPTY}, {@link ITabBox#MARK_STRATEGY_SAVE_NEEDED}
   * @since 3.8.2
   */
  public static void setTabBoxMarkStrategy(IForm form, int strategy) {
    form.<ITabBox> visit(box -> box.setMarkStrategy(strategy), ITabBox.class);
  }
}
