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

import org.eclipse.scout.rt.client.ui.form.fields.labelfield.AbstractLabelField;
import org.eclipse.scout.rt.dataobject.DoEntity;

@HybridActionType(CreateDummyWidgetHybridAction.TYPE)
public class CreateDummyWidgetHybridAction extends AbstractHybridAction<DoEntity> {

  protected static final String TYPE = "createWidget:Dummy";

  @Override
  public void execute(DoEntity data) {
    hybridManager().addWidget("dummy-widget-1", new AbstractLabelField() {
    });
    hybridManager().addWidget("dummy-widget-2", new AbstractLabelField() {
    });
  }
}
