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
package org.eclipse.scout.rt.client.mobile.ui.form.outline;

import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;

public class MainPageForm extends PageForm implements IMainPageForm {

  public MainPageForm(IPage<?> page, PageFormManager manager, PageFormConfig config) {
    super(page, manager, config);
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    getPageTableField().setActionBarVisible(false);
  }

  @Override
  protected boolean getConfiguredFooterVisible() {
    return true;
  }

}
