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
package org.eclipse.scout.rt.client.mobile.transformation;

import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.TEXTS;

public class ToolFormCloseAction extends AbstractMenu {
  private IForm m_form;

  public ToolFormCloseAction(IForm form) {
    m_form = form;
  }

  @Override
  protected String getConfiguredText() {
    return TEXTS.get("CloseButton");
  }

  @Override
  protected void execAction() {
    MobileDesktopUtility.closeToolForm(m_form);
  }
}
