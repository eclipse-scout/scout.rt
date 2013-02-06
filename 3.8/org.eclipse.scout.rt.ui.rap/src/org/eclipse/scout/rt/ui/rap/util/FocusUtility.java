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
package org.eclipse.scout.rt.ui.rap.util;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @since 3.8.2
 */
public class FocusUtility {

  public static boolean isFocusable(Control control) {
    if (control == null) {
      return false;
    }

    boolean takesFocus = (control.getStyle() & SWT.NO_FOCUS) == 0;
    if (!takesFocus) {
      return false;
    }

    boolean visible = control.getVisible();
    IPropertyObserver model = RwtScoutComposite.getScoutModelOnWidget(control);
    if (model instanceof IFormField) {
      IFormField field = ((IFormField) model);
      visible &= field.isVisible() && areParentsVisible(field);
    }

    return visible;
  }

  private static boolean areParentsVisible(IFormField field) {
    ICompositeField parentField = field.getParentField();
    while (parentField != null) {
      if (!parentField.isVisible()) {
        return false;
      }

      parentField = parentField.getParentField();
    }

    return true;
  }

  public static Control findFirstFocusableControl(Composite parent) {
    if (parent == null) {
      return null;
    }

    Control[] controls = parent.getTabList();
    for (Control control : controls) {
      if (control instanceof Composite) {
        control = findFirstFocusableControl((Composite) control);
        if (control != null) {
          return control;
        }
      }
      else {
        if (FocusUtility.isFocusable(control)) {
          return control;
        }
      }
    }

    return null;
  }
}
