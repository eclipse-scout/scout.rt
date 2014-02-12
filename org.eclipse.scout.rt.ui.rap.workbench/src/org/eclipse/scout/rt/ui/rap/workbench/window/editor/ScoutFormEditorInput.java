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
package org.eclipse.scout.rt.ui.rap.workbench.window.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * @deprecated will be removed with the M-release.
 */
@Deprecated
public class ScoutFormEditorInput implements IEditorInput {

  private final IRwtEnvironment m_uiEnvironment;
  private final IForm m_scoutObject;

  public ScoutFormEditorInput(IForm scoutObject, IRwtEnvironment uiEnvironment) {
    m_scoutObject = scoutObject;
    m_uiEnvironment = uiEnvironment;
  }

  private IRwtEnvironment getUiEnvironment() {
    return m_uiEnvironment;
  }

  public IForm getScoutObject() {
    return m_scoutObject;
  }

  @Override
  public boolean exists() {
    return true;
  }

  @Override
  public ImageDescriptor getImageDescriptor() {
    final Image icon = getUiEnvironment().getIcon(getScoutObject().getIconId());
    return ImageDescriptor.createFromImage(icon);
  }

  @Override
  public String getName() {
    return StringUtility.nvl(getScoutObject().getTitle(), "");
  }

  @Override
  public IPersistableElement getPersistable() {
    return null;
  }

  @Override
  public String getToolTipText() {
    return StringUtility.nvl(getScoutObject().getSubTitle(), "");
  }

  @Override
  public Object getAdapter(Class adapter) {
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ScoutFormEditorInput)) {
      return false;
    }
    ScoutFormEditorInput other = (ScoutFormEditorInput) obj;
    IForm form = this.getScoutObject();
    IForm otherForm = other.getScoutObject();
    if (form.getClass().getName().equals(otherForm.getClass().getName())
        && form.getHandler().isOpenExclusive() && otherForm.getHandler().isOpenExclusive()) {
      try {
        Object key = form.computeExclusiveKey();
        Object otherKey = otherForm.computeExclusiveKey();
        if (key == null || otherKey == null) {
          return false;
        }
        return key.equals(otherKey);
      }
      catch (ProcessingException e) {
        return false;
      }
    }
    return form.equals(otherForm);
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash = hash * 37 + getScoutObject().hashCode();
    return hash;
  }
}
