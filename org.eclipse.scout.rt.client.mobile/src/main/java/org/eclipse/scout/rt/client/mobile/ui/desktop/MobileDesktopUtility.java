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
package org.eclipse.scout.rt.client.mobile.ui.desktop;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.action.tool.IToolButton;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractFormToolButton;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;

/**
 * @since 3.9.0
 */
public class MobileDesktopUtility {

  public static void activateOutline(IOutline outline) {
    IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();

    if (desktop.getOutline() != outline) {
      desktop.activateOutline(outline);
    }

    if (!outline.isRootNodeVisible()) {
      throw new IllegalStateException("Root node must be visible for this drill down approach.");
    }

    if (outline.getSelectedNode() == null) {
      outline.selectNode(outline.getRootPage());
    }
  }

  public static boolean isToolForm(IForm form) {
    if (getToolButtonFor(form) != null) {
      return true;
    }

    return false;
  }

  public static IToolButton getToolButtonFor(IForm form) {
    for (IToolButton toolButton : getDesktop().getToolButtons()) {
      if (toolButton instanceof AbstractFormToolButton) {
        IForm toolForm = ((AbstractFormToolButton<?>) toolButton).getForm();
        if (form == toolForm) {
          return toolButton;
        }
      }
    }

    return null;
  }

  public static void openToolForm(IForm form) {
    IToolButton toolButton = getToolButtonFor(form);
    if (toolButton != null) {
      toolButton.setSelected(true);
    }
    //Double check to make sure it really will be added
    if (!getDesktop().isShowing(form)) {
      getDesktop().showForm(form);
    }
  }

  public static void closeToolForm(IForm form) {
    IToolButton toolButton = getToolButtonFor(form);
    if (toolButton != null) {
      toolButton.setSelected(false);
    }
    //Double check to make sure it really will be removed
    if (getDesktop().isShowing(form)) {
      getDesktop().hideForm(form);
    }
  }

  public static void closeAllToolForms() {
    for (IToolButton toolButton : getDesktop().getToolButtons()) {
      if (toolButton.isVisible()) {
        toolButton.setSelected(false);
      }
    }
  }

  public static void closeOpenForms() {
    for (IForm view : getDesktop().getViews()) {
      closeForm(view);
    }
  }

  public static void closeForm(IForm form) {
    if (form == null) {
      return;
    }

    if (MobileDesktopUtility.isToolForm(form)) {
      MobileDesktopUtility.closeToolForm(form);
    }
    else if (form.isShowOnStart()) {
      form.doClose();
    }
    else {
      removeFormFromDesktop(form);
    }
  }

  public static void removeFormFromDesktop(IForm form) {
    getDesktop().hideForm(form);
  }

  public static void removeFormsFromDesktop(Class<? extends IForm> formClass, String displayViewId, IForm excludedForm) {
    if (displayViewId == null) {
      return;
    }

    for (IForm view : getDesktop().getViews()) {
      if (view != excludedForm && formClass.isInstance(view) && displayViewId.equals(view.getDisplayViewId())) {
        getDesktop().hideForm(view);
      }
    }
  }

  public static void addFormToDesktop(IForm form) {
    if (isToolForm(form)) {
      openToolForm(form);
    }
    else {
      getDesktop().showForm(form);
    }
  }

  private static IDesktop getDesktop() {
    return ClientSessionProvider.currentSession().getDesktop();
  }

  public static boolean isAnyViewVisible(String displayViewId) {
    if (displayViewId == null) {
      return false;
    }

    for (IForm view : getDesktop().getViews()) {
      if (displayViewId.equals(view.getDisplayViewId())) {
        return true;
      }
    }

    return false;
  }

  public static boolean setFormWidthHint(IForm form, int widthHint) {
    GridData gridDataHints = form.getRootGroupBox().getGridDataHints();
    if (gridDataHints.widthInPixel == widthHint) {
      return false;
    }

    gridDataHints.widthInPixel = widthHint;
    form.getRootGroupBox().setGridDataHints(gridDataHints);
    return true;
  }

}
