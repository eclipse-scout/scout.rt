/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action.menu;

import static org.junit.Assert.assertNotNull;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.NestedMenusTest.P_Desktop.HelpMenu.AboutMenu;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.ScoutInfoForm;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Accessing a nested menu yields null because child menus are not traversed.
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class NestedMenusTest {

  @Test
  public void testFindNestedMenuInDesktop() throws Exception {
    IDesktop desktop = new P_Desktop();
    AboutMenu menu = desktop.getMenu(P_Desktop.HelpMenu.AboutMenu.class);
    assertNotNull(menu);
  }

  public static class P_Desktop extends AbstractDesktop implements IDesktop {

    public P_Desktop() {
    }

    @Override
    protected void execOpened() {
    }

    @Order(10)
    public class FileMenu extends AbstractMenu {

      @Override
      protected String getConfiguredText() {
        return TEXTS.get("FileMenu");
      }

      @Order(100)
      public class ExitMenu extends AbstractMenu {

        @Override
        protected String getConfiguredText() {
          return TEXTS.get("ExitMenu");
        }

        @Override
        protected void execAction() {
          ClientSessionProvider.currentSession(TestEnvironmentClientSession.class).stop();
        }
      }

      @Order(110)
      public class DebugMenu extends AbstractMenu {

        @Override
        protected String getConfiguredText() {
          return "Debug";
        }

        @Override
        protected void execAction() {
        }
      }
    }

    @Order(20)
    public class ToolsMenu extends AbstractMenu {

      @Override
      protected String getConfiguredText() {
        return TEXTS.get("ToolsMenu");
      }
    }

    @Order(30)
    public class HelpMenu extends AbstractMenu {

      @Override
      protected String getConfiguredText() {
        return TEXTS.get("HelpMenu");
      }

      @Order(10)
      public class AboutMenu extends AbstractMenu {

        @Override
        protected String getConfiguredText() {
          return TEXTS.get("AboutMenu");
        }

        @Override
        protected void execAction() {
          ScoutInfoForm form = new ScoutInfoForm();
          form.startModify();
        }
      }
    }
  }

}
