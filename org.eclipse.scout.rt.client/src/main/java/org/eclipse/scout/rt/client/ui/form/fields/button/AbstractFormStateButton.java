/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.button;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.text.TEXTS;

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
    return TEXTS.get("FormStateLoad");
  }

  @Override
  protected void execClickAction() {
    getForm().doImportXml();
  }

  @Order(10)
  @ClassId("db8cdb66-c87a-40c3-be7c-02433e079f99")
  public class SaveMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("FormStateStore");
    }

    @Override
    protected void execAction() {
      getForm().doExportXml(false);
    }
  }

  @Order(20)
  @ClassId("84786b4f-d807-4b9b-9b63-c917b4de3133")
  public class SaveAsMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("FormStateStoreAs");
    }

    @Override
    protected void execAction() {
      getForm().doExportXml(true);
    }
  }
}
