/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.menu.checkbox;

import org.eclipse.scout.rt.client.extension.ui.action.menu.checkbox.ICheckBoxMenuExtension;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("9ab423b0-8347-41d6-bc74-6835da982d84")
public abstract class AbstractCheckBoxMenu extends AbstractMenu implements ICheckBoxMenu {

  private boolean m_supportedChangeToggleBehaviour;

  public AbstractCheckBoxMenu() {
    super();
  }

  public AbstractCheckBoxMenu(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    // enable setToggleAction for super init
    try {
      m_supportedChangeToggleBehaviour = true;
      super.initConfig();
    }
    finally {
      m_supportedChangeToggleBehaviour = false;
    }
  }

  @Override
  protected final boolean getConfiguredToggleAction() {
    return true;
  }

  @Override
  public final void setToggleAction(boolean b) {
    if (m_supportedChangeToggleBehaviour) {
      super.setToggleAction(b);
    }
    else {
      throw new UnsupportedOperationException("setToggleAction on " + AbstractCheckBoxMenu.class.getSimpleName() + " is not supported!");
    }
    // void here
  }

  protected static class LocalCheckBoxMenuExtension<OWNER extends AbstractCheckBoxMenu> extends LocalMenuExtension<OWNER> implements ICheckBoxMenuExtension<OWNER> {

    public LocalCheckBoxMenuExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected ICheckBoxMenuExtension<? extends AbstractCheckBoxMenu> createLocalExtension() {
    return new LocalCheckBoxMenuExtension<>(this);
  }
}
