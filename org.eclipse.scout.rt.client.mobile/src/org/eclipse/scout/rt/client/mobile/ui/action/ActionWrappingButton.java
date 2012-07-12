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
package org.eclipse.scout.rt.client.mobile.ui.action;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;

public class ActionWrappingButton extends AbstractButton {
  private ActionToButtonPropertyDelegator m_propertyDelegator;

  public ActionWrappingButton(IAction action) {
    super(false);
    m_propertyDelegator = new ActionToButtonPropertyDelegator(action, this);
    callInitializer();
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    m_propertyDelegator.init();
  }

  public IAction getWrappedAction() {
    return m_propertyDelegator.getSender();
  }

  @Override
  protected void execClickAction() throws ProcessingException {
    getWrappedAction().doAction();
  }

  @Override
  protected void execToggleAction(boolean selected) throws ProcessingException {
    getWrappedAction().setSelected(selected);
  }

}
