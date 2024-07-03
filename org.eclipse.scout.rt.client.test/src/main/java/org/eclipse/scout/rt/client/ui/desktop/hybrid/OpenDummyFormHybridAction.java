/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid;

@HybridActionType(OpenDummyFormHybridAction.TYPE)
public class OpenDummyFormHybridAction extends AbstractFormHybridAction<DummyForm, DummyDo> {

  protected static final String TYPE = OPEN_FORM_PREFIX + "Dummy";

  @Override
  protected DummyForm createForm(DummyDo data) {
    DummyForm form = new DummyForm();
    if (data != null) {
      form.importData(data);
    }
    return form;
  }

  @Override
  protected void exportResult(DummyForm form, DummyDo result) {
    form.exportData(result);
  }
}
