/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid;

@HybridActionType(OpenDummyFormHybridAction.TYPE)
public class OpenDummyFormHybridAction extends AbstractFormHybridAction<DummyForm, DummyDo> {
  protected final static String TYPE = OPEN_FORM_PREFIX + "Dummy";

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
    super.exportResult(form, result);
    form.exportData(result);
  }
}
