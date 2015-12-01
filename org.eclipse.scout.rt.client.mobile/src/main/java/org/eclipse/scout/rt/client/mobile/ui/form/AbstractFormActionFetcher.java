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
package org.eclipse.scout.rt.client.mobile.ui.form;

import java.util.List;

import org.eclipse.scout.rt.client.mobile.ui.action.ActionButtonBarUtility;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

public abstract class AbstractFormActionFetcher implements IActionFetcher {
  private IForm m_form;

  public AbstractFormActionFetcher(IForm form) {
    m_form = form;
  }

  public IForm getForm() {
    return m_form;
  }

  protected List<IMobileAction> convertCustomProcessButtons() {
    List<IButton> customProcessButtons = getForm().getRootGroupBox().getCustomProcessButtons();

    if (CollectionUtility.hasElements(customProcessButtons)) {
      return ActionButtonBarUtility.convertButtonsToActions(getForm().getRootGroupBox().getCustomProcessButtons());
    }
    else {
      return CollectionUtility.emptyArrayList();
    }
  }
}
