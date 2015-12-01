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

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.ScoutTexts;

@ClassId("60f8536e-c0db-44c5-88ae-83289620f790")
public abstract class AbstractFormStateButton extends AbstractButton {

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
  protected void execClickAction() {
    getForm().doImportXml();
  }

  @Order(10)
  public class SaveMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("FormStateStore");
    }

    @Override
    protected void execAction() {
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
    protected void execAction() {
      getForm().doExportXml(true);
    }
  }
}
