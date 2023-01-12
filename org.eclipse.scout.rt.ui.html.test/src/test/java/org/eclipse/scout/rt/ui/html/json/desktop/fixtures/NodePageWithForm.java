/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop.fixtures;

import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.ui.html.json.form.fixtures.FormWithOneField;

@ClassId("8ab0cd01-bb00-4a3d-8dc4-bd30856af3f5")
public class NodePageWithForm extends AbstractPageWithNodes {

  @Override
  protected String getConfiguredTitle() {
    return "Node";
  }

  @Override
  protected void execPageActivated() {
    if (getDetailForm() == null) {
      FormWithOneField form = new FormWithOneField();
      form.setAllEnabled(false);
      setDetailForm(form);
      form.start();
    }
  }

  @Override
  protected void execDisposePage() {
    if (getDetailForm() != null) {
      getDetailForm().doClose();
      setDetailForm(null);
    }
  }
}
