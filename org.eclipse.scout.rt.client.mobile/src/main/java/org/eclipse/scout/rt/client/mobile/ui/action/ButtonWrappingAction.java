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
package org.eclipse.scout.rt.client.mobile.ui.action;

import java.beans.PropertyChangeEvent;

import org.eclipse.scout.rt.client.mobile.ui.form.AbstractMobileAction;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;

/**
 * A {@link IActionNode} which wraps a {@link IButton}. <br/>
 * {@link PropertyChangeEvent}s fired by the button are delegated to the action
 * 
 * @since 3.9.0
 */
public class ButtonWrappingAction extends AbstractMobileAction {
  private ButtonToActionPropertyDelegator m_propertyDelegator;

  public ButtonWrappingAction(IButton wrappedButton) {
    super(false);

    m_propertyDelegator = new ButtonToActionPropertyDelegator(wrappedButton, this);

    callInitializer();
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    m_propertyDelegator.init();
  }

  @Override
  protected void execAction() {
    getWrappedButton().doClick();
  }

  @Override
  protected void execSelectionChanged(boolean selection) {
    getWrappedButton().setSelected(selection);
  }

  public IButton getWrappedButton() {
    return m_propertyDelegator.getSender();
  }

}
