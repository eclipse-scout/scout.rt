/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action.menu;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.NestedMenusTest.P_Desktop.HelpMenu.AboutMenu;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.ScoutInfoForm;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.testing.client.runner.ScoutClientTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Accessing a nested menu yields null because child menus are not traversed.
 */
@RunWith(ScoutClientTestRunner.class)
public class NestedMenusTest {

  @Test
  public void testFindNestedMenuInDesktop() throws Exception {
    IDesktop desktop = new P_Desktop();
    AboutMenu menu = desktop.getMenu(P_Desktop.HelpMenu.AboutMenu.class);
    Assert.assertNotNull(menu);
  }

  public static class P_Desktop extends AbstractDesktop implements IDesktop {

    public P_Desktop() {
    }

    @Override
    protected void execOpened() throws ProcessingException {
    }

    @Order(10.0)
    public class FileMenu extends AbstractMenu {

      @Override
      public String getConfiguredText() {
        return TEXTS.get("FileMenu");
      }

      @Order(100.0)
      public class ExitMenu extends AbstractMenu {

        @Override
        public String getConfiguredText() {
          return TEXTS.get("ExitMenu");
        }

        @Override
        public void execAction() throws ProcessingException {
          ClientSyncJob.getCurrentSession(TestEnvironmentClientSession.class).stopSession();
        }
      }

      @Order(110.0)
      public class DebugMenu extends AbstractMenu {

        @Override
        public String getConfiguredText() {
          return "Debug";
        }

        @Override
        public void execAction() throws ProcessingException {
        }
      }
    }

    @Order(20.0)
    public class ToolsMenu extends AbstractMenu {

      @Override
      public String getConfiguredText() {
        return TEXTS.get("ToolsMenu");
      }
    }

    @Order(30.0)
    public class HelpMenu extends AbstractMenu {

      @Override
      public String getConfiguredText() {
        return TEXTS.get("HelpMenu");
      }

      @Order(10.0)
      public class AboutMenu extends AbstractMenu {

        @Override
        public String getConfiguredText() {
          return TEXTS.get("AboutMenu");
        }

        @Override
        public void execAction() throws ProcessingException {
          ScoutInfoForm form = new ScoutInfoForm();
          form.startModify();
        }
      }
    }
  }

}
