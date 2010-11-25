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
package org.eclipse.scout.rt.ui.swt.window.desktop.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class ScoutFormEditorInput implements IEditorInput {

  private final ISwtEnvironment m_environment;
  private final IForm m_scoutObject;

  public ScoutFormEditorInput(IForm scoutObject, ISwtEnvironment environment) {
    m_scoutObject = scoutObject;
    m_environment = environment;
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }

  public IForm getScoutObject() {
    return m_scoutObject;
  }

  public boolean exists() {
    return true;
  }

  public ImageDescriptor getImageDescriptor() {
    final Image icon = getEnvironment().getIcon(getScoutObject().getIconId());
    return ImageDescriptor.createFromImage(icon);
  }

  public String getName() {
    return StringUtility.nvl(getScoutObject().getTitle(), "");
  }

  public IPersistableElement getPersistable() {
    return null;
  }

  public String getToolTipText() {
    return StringUtility.nvl(getScoutObject().getSubTitle(), "");
  }

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
    return this.getScoutObject().equals(other.getScoutObject());
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash = hash * 37 + getScoutObject().hashCode();
    return hash;
  }
}
