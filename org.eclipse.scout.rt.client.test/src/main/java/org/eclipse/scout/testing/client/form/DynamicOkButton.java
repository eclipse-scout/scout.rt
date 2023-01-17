/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.testing.client.form;

import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * Dynamic field to build an ad-hoc application for testing
 */
@ClassId("570af8aa-d7da-4165-b73b-a37e7d0873c2")
public class DynamicOkButton extends AbstractOkButton {

  public DynamicOkButton() {
    super();
    setProperty("id", "ok");
  }

  @Override
  public String getFieldId() {
    return (String) getProperty("id");
  }

}
