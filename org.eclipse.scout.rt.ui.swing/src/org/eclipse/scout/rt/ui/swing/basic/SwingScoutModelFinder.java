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
package org.eclipse.scout.rt.ui.swing.basic;

import java.awt.Component;
import java.lang.reflect.Field;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * Queries the model for a component. Used for test tool support.
 */
public class SwingScoutModelFinder {

  /**
   * Returns the scout model or the given component or null, if it has not been created by scout.
   * Possible return types are {@link IPropertyObserver} or {@link ITreeNode}.
   */
  public Object getScoutModel(Component comp) {
    Component c = comp;
    if (c == null) {
      return null;
    }
    Object s = null;
    if (c instanceof JTree) {
      TreePath path = ((JTree) c).getSelectionPath();
      if (path != null) {
        Object x = path.getLastPathComponent();
        if (x instanceof ITreeNode) {
          s = x;
        }
      }
    }
    if (s == null) {
      Component tmp = c;
      while (tmp != null) {
        if (tmp instanceof JComponent) {
          s = SwingScoutComposite.getScoutModelOnWidget((JComponent) tmp);
          if (s != null) {
            break;
          }
        }
        tmp = tmp.getParent();
      }
    }

    if (s != null) {
      Object scoutObject = s;
      while (scoutObject != null) {
        if (scoutObject instanceof IFormField || scoutObject instanceof IForm || scoutObject instanceof IAction) {
          break; // ok
        }
        else {
          int nestedCount = scoutObject.getClass().getName().replaceAll("[^$]", "").trim().length();
          try {
            Field f = scoutObject.getClass().getDeclaredField("this$" + (nestedCount - 1));
            f.setAccessible(true);
            scoutObject = f.get(scoutObject);
            break;
          }
          catch (Throwable t) {
            scoutObject = null;
          }
        }
      }

      if (scoutObject != null) {
        return scoutObject;
      }
      else {
        return s;
      }
    }
    return null;
  }

  /**
   * Returns the scout model name without package prefix.
   */
  public String getScoutModelName(Component comp) {
    Object scoutModel = getScoutModel(comp);
    if (scoutModel != null) {
      String className = scoutModel.getClass().getName();
      String packagePrefix = scoutModel.getClass().getPackage().getName() + ".";
      return StringUtility.removePrefixes(className, packagePrefix);
    }
    return null;
  }
}
