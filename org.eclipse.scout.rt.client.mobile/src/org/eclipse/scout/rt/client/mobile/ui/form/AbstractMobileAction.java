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

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;

/**
 * @since 3.9.0
 */
public class AbstractMobileAction extends AbstractMenu implements IMobileAction {

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

  public static Integer getHorizontalAlignment(IAction action) {
    return (Integer) action.getProperty(PROP_HORIZONTAL_ALIGNMENT);
  }

  public static void setHorizontalAlignment(IAction action, Integer alignment) {
    action.setProperty(PROP_HORIZONTAL_ALIGNMENT, alignment);
  }

  protected int getConfiguredHorizontalAlignment() {
    return 1;
  }

}
