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

import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * Dynamic field to build an ad-hoc application for testing
 */
@ClassId("73d968ca-d4f8-4012-a6b4-80aa2232e1b4")
public class DynamicStringField extends AbstractStringField {

  public DynamicStringField(String id, String label) {
    super();
    setProperty("id", id);
    setLabel(label);
  }

  @Override
  public String getFieldId() {
    return (String) getProperty("id");
  }

}
