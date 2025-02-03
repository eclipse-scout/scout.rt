/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
