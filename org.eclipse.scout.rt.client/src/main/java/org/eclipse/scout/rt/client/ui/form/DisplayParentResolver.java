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
package org.eclipse.scout.rt.client.ui.form;

import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Resolves the {@link IDisplayParent} for {@link IForm}s, {@link IMessageBox}s and {@link IFileChooser}s.
 *
 * @since 5.1
 */
@ApplicationScoped
public class DisplayParentResolver {

  /**
   * Resolves the {@link IDisplayParent} for the given {@link IForm}.
   */
  public IDisplayParent resolve(final IForm form) {
    if (form.getDisplayHint() == IForm.DISPLAY_HINT_VIEW) {
      return ClientSessionProvider.currentSession().getDesktop(); // by default, a view has the Desktop as its 'displayParent'.
    }
    else {
      return findClosestDisplayParent();
    }
  }

  /**
   * Resolves the {@link IDisplayParent} for the given {@link IMessageBox}.
   */
  public IDisplayParent resolve(final IMessageBox messageBox) {
    return findClosestDisplayParent();
  }

  /**
   * Resolves the {@link IDisplayParent} for the given {@link IFileChooser}.
   */
  public IDisplayParent resolve(final IFileChooser fileChooser) {
    return findClosestDisplayParent();
  }

  /**
   * Resolves to the closest {@link IDisplayParent} from the current calling context.
   */
  protected IDisplayParent findClosestDisplayParent() {
    final ClientRunContext currentRunContext = ClientRunContexts.copyCurrent();

    // Check whether a Form is currently the 'displayParent'. If being a wrapped Form, return its outer Form.
    IForm currentForm = currentRunContext.getForm();
    if (currentForm != null) {
      while (currentForm.getOuterForm() != null) {
        currentForm = currentForm.getOuterForm();
      }
      // Forms that are not started must not be used as display parent, because everything that is attached
      // to them would not be visible as well. This might lead to blocking conditions that will never be released
      // (e.g. when a message box is opened with a form as display parent that is not showing).
      // Note: We cannot use form.isShowing(), because some forms are started but not automatically
      // added to the desktop (e.g. forms managed by form tool buttons).
      if (currentForm.isFormStarted()) {
        return currentForm;
      }
    }

    // Check whether an Outline is currently the 'displayParent'.
    final IOutline currentOutline = currentRunContext.getOutline();
    if (currentOutline != null) {
      return currentOutline;
    }

    // Use the desktop as 'displayParent'.
    return currentRunContext.getDesktop();
  }
}
