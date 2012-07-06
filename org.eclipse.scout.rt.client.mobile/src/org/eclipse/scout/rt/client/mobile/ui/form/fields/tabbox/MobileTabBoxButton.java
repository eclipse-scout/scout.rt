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
package org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.FormFieldPropertyDelegator;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;

/**
 * Represents a tab item.
 * <p>
 * Opens the corresponding group box inside a new {@link MobileTabBoxForm} if it gets clicked.
 * 
 * @since 3.9.0
 */
public class MobileTabBoxButton extends AbstractButton {
  private FormFieldPropertyDelegator m_propertyDelegator;

  public MobileTabBoxButton(IGroupBox groupBox) {
    super(false);

    m_propertyDelegator = new FormFieldPropertyDelegator<IGroupBox, IButton>(groupBox, this);
    callInitializer();
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    m_propertyDelegator.init();
  }

  public IGroupBox getWrappedGroupBox() {
    return (IGroupBox) m_propertyDelegator.getSender();
  }

  @Override
  protected boolean getConfiguredProcessButton() {
    return false;
  }

  @Override
  protected boolean getConfiguredFillHorizontal() {
    return true;
  }

  @Override
  protected int getConfiguredHorizontalAlignment() {
    return 0;
  }

  @Override
  protected int getConfiguredWidthInPixel() {
    return 300;
  }

  @Override
  protected void execClickAction() throws ProcessingException {
    MobileTabBoxForm form = new MobileTabBoxForm(getWrappedGroupBox());
    form.start();
  }

}
