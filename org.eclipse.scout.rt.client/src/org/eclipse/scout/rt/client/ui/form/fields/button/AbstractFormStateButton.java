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
package org.eclipse.scout.rt.client.ui.form.fields.button;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.shared.ScoutTexts;

public abstract class AbstractFormStateButton extends AbstractButton implements IButton {

  public AbstractFormStateButton() {
    this(true);
  }

  public AbstractFormStateButton(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  @Override
  protected String getConfiguredLabel() {
    return ScoutTexts.get("FormStateLoad");
  }

  @Override
  protected int getConfiguredHorizontalAlignment() {
    return -1;
  }

  @Override
  protected void execClickAction() throws ProcessingException {
    getForm().doImportXml();
  }

  @Order(10)
  public class SaveMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("FormStateStore");
    }

    @Override
    protected void execAction() throws ProcessingException {
      getForm().doExportXml(false);
    }
  }

  @Order(20)
  public class SaveAsMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("FormStateStoreAs");
    }

    @Override
    protected void execAction() throws ProcessingException {
      getForm().doExportXml(true);
    }
  }
}
