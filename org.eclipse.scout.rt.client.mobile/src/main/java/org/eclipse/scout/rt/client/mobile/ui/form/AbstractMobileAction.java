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
package org.eclipse.scout.rt.client.mobile.ui.form;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;

/**
 * @since 3.9.0
 */
public abstract class AbstractMobileAction extends AbstractMenu implements IMobileAction {

  public AbstractMobileAction() {
    super();
  }

  public AbstractMobileAction(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    setHorizontalAlignment(getConfiguredHorizontalAlignment());
  }

  @Override
  public int getHorizontalAlignment() {
    return getHorizontalAlignment(this);
  }

  @Override
  public void setHorizontalAlignment(int alignment) {
    setHorizontalAlignment(this, alignment);
  }

  public static int getHorizontalAlignment(IAction action) {
    Number n = (Number) action.getProperty(PROP_HORIZONTAL_ALIGNMENT);
    return n != null ? n.intValue() : 0;
  }

  public static void setHorizontalAlignment(IAction action, int alignment) {
    action.setProperty(PROP_HORIZONTAL_ALIGNMENT, alignment);
  }

  @Override
  protected int getConfiguredHorizontalAlignment() {
    return HORIZONTAL_ALIGNMENT_RIGHT;
  }

}
