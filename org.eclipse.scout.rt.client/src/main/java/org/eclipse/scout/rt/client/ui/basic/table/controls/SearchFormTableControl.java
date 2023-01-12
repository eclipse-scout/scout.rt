/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.controls;

import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.shared.AbstractIcons;

/**
 * @since 5.1.0
 */
@ClassId("a5ec6d5d-1d49-4a83-932b-51935769d0c4")
@Order(100)
public class SearchFormTableControl extends AbstractFormTableControl {

  @Override
  protected void initConfig() {
    super.initConfig();
    setIconId(AbstractIcons.Search);
    setTooltipText(TEXTS.get("Search"));
  }

  @Override
  public void setForm(IForm form) {
    setEnabled(form != null);
    super.setForm(form);
  }

  @Override
  protected boolean getConfiguredEnabled() {
    return false;
  }

  @Override
  protected String getConfiguredKeyStroke() {
    return IKeyStroke.F6;
  }
}
