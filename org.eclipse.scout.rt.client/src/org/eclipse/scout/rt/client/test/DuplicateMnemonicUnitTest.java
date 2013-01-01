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

import java.util.ArrayList;

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
public class DuplicateMnemonicUnitTest extends AbstractClientTest {

  @Override
  public void run() throws Exception {
    checkMenus("top level", ClientTestUtility.getDesktop().getMenus());
  }

  public void checkMenus(String subTitle, IMenu[] menus) throws ProcessingException {
    setSubTitle(subTitle);
    ArrayList<Character> mnemonics = new ArrayList<Character>();
    for (IMenu menu : menus) {
      Character m = menu.getMnemonic();
      if (m != null && m.charValue() > 0) {
        if (mnemonics.contains(m)) {
          addWarningStatus("duplicate mnemonic " + m);
        }
        else {
          addOkStatus("mnemonic " + m);
        }
        mnemonics.add(m);
      }
    }
    // children
    for (IMenu menu : menus) {
      if (menu.getChildActions().size() > 0 && menu.isVisible()) {
        IMenu[] childs = menu.getChildActions().toArray(new IMenu[menu.getChildActions().size()]);
        checkMenus(menu.getText() + " [" + menu.getClass().getSimpleName() + "]", childs);
      }
    }
  }

  @Override
  protected String getConfiguredTitle() {
    return "menubar: duplicate mnemonic on any level";
  }

}
