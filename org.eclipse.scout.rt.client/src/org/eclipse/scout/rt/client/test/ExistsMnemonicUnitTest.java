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
package org.eclipse.scout.rt.client.test;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.services.common.test.AbstractClientTest;
import org.eclipse.scout.rt.client.services.common.test.ClientTestUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;

/**
 * @deprecated Use Scout JUnit Testing Support: {@link org.eclipse.scout.testing.client.runner.ScoutClientTestRunner} or
 *             {@link org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner} to run Unit tests.
 */
@Deprecated
@SuppressWarnings("deprecation")
public class ExistsMnemonicUnitTest extends AbstractClientTest {

  @Override
  public void run() throws Exception {
    for (IMenu menu : ClientTestUtility.getDesktop().getMenus()) {
      checkMainMenus(menu);
    }
  }

  public void checkMainMenus(IMenu menu) throws ProcessingException {
    setSubTitle(menu.getText() + " [" + menu.getClass().getSimpleName() + "]");
    if (!menu.isSeparator() && menu.getMnemonic() == 0x0 && menu.isVisible()) {
      addWarningStatus("no mnemonic");
    }
    else {
      addOkStatus("mnemonic " + menu.getMnemonic());
    }
    // children
    for (IMenu sub : menu.getChildActions()) {
      checkMainMenus(sub);
    }
  }

  @Override
  protected String getConfiguredTitle() {
    return "menubar: exists mnemonic on all items";
  }

}
