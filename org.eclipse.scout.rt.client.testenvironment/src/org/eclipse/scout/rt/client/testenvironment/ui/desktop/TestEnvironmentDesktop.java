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
package org.eclipse.scout.rt.client.testenvironment.ui.desktop;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.keystroke.AbstractKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.outline.DefaultOutlineTableForm;
import org.eclipse.scout.rt.client.ui.form.outline.DefaultOutlineTreeForm;
import org.eclipse.scout.rt.shared.TEXTS;

/**
 * {@link IDesktop} for Client Test Environment
 * 
 * @author jbr
 */
public class TestEnvironmentDesktop extends AbstractDesktop implements IDesktop {
  public TestEnvironmentDesktop() {
  }

  @Override
  protected String getConfiguredTitle() {
    return "Test Environment Application";
  }

  @Override
  protected void execOpened() throws ProcessingException {
    // outline form:
    DefaultOutlineTreeForm treeForm = new DefaultOutlineTreeForm();
    treeForm.startView();
    // outline table:
    DefaultOutlineTableForm tableForm = new DefaultOutlineTableForm();
    tableForm.startView();
  }

  @Order(10.0)
  public class FileMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("FileMenu");
    }

    @Order(100.0)
    public class ExitMenu extends AbstractMenu {

      @Override
      protected String getConfiguredText() {
        return TEXTS.get("ExitMenu");
      }

      @Override
      protected void execAction() throws ProcessingException {
        ClientSyncJob.getCurrentSession(
            TestEnvironmentClientSession.class).stopSession();
      }
    }
  }

  @Order(40.0)
  public class F1Key extends AbstractKeyStroke {

    @Override
    protected String getConfiguredKeyStroke() {
      return "f1";
    }
  }

  @Order(50.0)
  public class F2Key extends AbstractKeyStroke {

    @Override
    protected String getConfiguredKeyStroke() {
      return "f2";
    }
  }

  @Order(60.0)
  public class F3Key extends AbstractKeyStroke {

    @Override
    protected String getConfiguredKeyStroke() {
      return "f3";
    }
  }

  @Order(70.0)
  public class F4Key extends AbstractKeyStroke {

    @Override
    protected String getConfiguredKeyStroke() {
      return "f4";
    }
  }

  @Order(80.0)
  public class F5Key extends AbstractKeyStroke {

    @Override
    protected String getConfiguredKeyStroke() {
      return "f5";
    }
  }

  @Order(90.0)
  public class F6Key extends AbstractKeyStroke {

    @Override
    protected String getConfiguredKeyStroke() {
      return "f6";
    }
  }

  @Order(100.0)
  public class F7Key extends AbstractKeyStroke {

    @Override
    protected String getConfiguredKeyStroke() {
      return "f7";
    }
  }

  @Order(110.0)
  public class F8Key extends AbstractKeyStroke {

    @Override
    protected String getConfiguredKeyStroke() {
      return "f8";
    }
  }

  @Order(120.0)
  public class F9Key extends AbstractKeyStroke {

    @Override
    protected String getConfiguredKeyStroke() {
      return "f9";
    }
  }

  @Order(130.0)
  public class F10Key extends AbstractKeyStroke {

    @Override
    protected String getConfiguredKeyStroke() {
      return "f10";
    }
  }

  @Order(140.0)
  public class F11Key extends AbstractKeyStroke {

    @Override
    protected String getConfiguredKeyStroke() {
      return "f11";
    }
  }

  @Order(150.0)
  public class F12Key extends AbstractKeyStroke {

    @Override
    protected String getConfiguredKeyStroke() {
      return "f12";
    }
  }

}
