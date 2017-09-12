/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.form.IForm;

public class DesktopEventFilterCondition {

  private final int m_type;
  private IForm m_form;
  private boolean m_checkDisplayParents;

  public DesktopEventFilterCondition(int type) {
    m_type = type;
  }

  public int getType() {
    return m_type;
  }

  public IForm getForm() {
    return m_form;
  }

  public void setForm(IForm form) {
    m_form = form;
  }

  public boolean isCheckDisplayParents() {
    return m_checkDisplayParents;
  }

  public void setCheckDisplayParents(boolean checkDisplayParents) {
    m_checkDisplayParents = checkDisplayParents;
  }

}
