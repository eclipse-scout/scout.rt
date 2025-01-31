/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form;

import java.util.function.Consumer;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.IWrappedFormField;

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
   * constructor after the form initConfig and postInitConfig.
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
   *     the form
   * @param strategy
   *     one of {@link ITabBox#MARK_STRATEGY_EMPTY}, {@link ITabBox#MARK_STRATEGY_SAVE_NEEDED}
   * @since 3.8.2
   */
  public static void setTabBoxMarkStrategy(IForm form, int strategy) {
    form.visit((Consumer<ITabBox>) box -> box.setMarkStrategy(strategy), ITabBox.class);
  }

  /**
   * @return the form which is not a wrapped form {@link IWrappedFormField}
   */
  public static IForm findRootForm(IForm form) {
    if (form == null) {
      return null;
    }
    while (form.getOuterForm() != null) {
      form = form.getOuterForm();
    }
    return form;
  }
}
