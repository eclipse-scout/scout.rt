/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.mobile.ui.desktop;

import java.awt.Rectangle;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.action.tool.IToolButton;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractFormToolButton;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTableForm;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * @since 3.8.0
 */
public class MobileDesktopUtility {

  public static boolean isToolForm(IForm form) {
    if (getToolButtonFor(form) != null) {
      return true;
    }

    return false;
  }

  public static IToolButton getToolButtonFor(IForm form) {
    for (IToolButton toolButton : getDesktop().getToolButtons()) {
      if (toolButton instanceof AbstractFormToolButton) {
        IForm toolForm = ((AbstractFormToolButton) toolButton).getForm();
        if (form == toolForm) {
          return toolButton;
        }
      }
    }

    return null;
  }

  public static void closeToolForm(IForm form) throws ProcessingException {
    IToolButton toolButton = getToolButtonFor(form);
    if (toolButton != null) {
      toolButton.setSelected(false);
    }
  }

  public static void closeAllToolForms() {
    for (IToolButton toolButton : getDesktop().getToolButtons()) {
      if (toolButton.isVisible()) {
        toolButton.setSelected(false);
      }
    }
  }

  public static void closeOpenForms() throws ProcessingException {
    final IForm[] viewStack = getDesktop().getViewStack();
    for (IForm form : viewStack) {
      closeForm(form);
    }
  }

  public static void closeForm(IForm form) throws ProcessingException {
    if (form == null) {
      return;
    }

    if (MobileDesktopUtility.isToolForm(form)) {
      MobileDesktopUtility.closeToolForm(form);
    }
    else if (form.isAutoAddRemoveOnDesktop()) {
      form.doClose();
    }
    else {
      removeFormFromDesktop(form);
    }
  }

  public static void removeFormFromDesktop(IForm form) {
    if (form instanceof IOutlineTableForm) {
      getDesktop().setOutlineTableFormVisible(false);
    }
    else {
      getDesktop().removeForm(form);
    }
  }

  public static void addFormToDesktop(IForm form) {
    if (form instanceof IOutlineTableForm) {
      getDesktop().setOutlineTableFormVisible(true);
    }
    else {
      getDesktop().addForm(form);
    }
  }

  private static IDesktop getDesktop() {
    return ClientSyncJob.getCurrentSession().getDesktop();
  }

  public static boolean isAnyViewVisible(String displayViewId) {
    if (displayViewId == null) {
      return false;
    }

    IForm[] viewStack = getDesktop().getViewStack();
    for (IForm form : viewStack) {
      if (displayViewId.equals(form.getDisplayViewId())) {
        return true;
      }
    }

    return false;
  }

  public static void setFormWidthHint(IForm form, int widthHint) {
    form.setCacheBounds(true);

    Rectangle formBounds = ClientUIPreferences.getInstance(ClientJob.getCurrentSession()).getFormBounds(form);
    if (formBounds != null && formBounds.getWidth() == widthHint) {
      return;
    }

    formBounds = new Rectangle(-1, -1, widthHint, -1);
    ClientUIPreferences.getInstance(ClientJob.getCurrentSession()).setFormBounds(form, formBounds);
  }

}
