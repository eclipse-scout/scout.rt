/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.spec.client;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.shared.TEXTS;

/**
 * implementation of {@link AbstractTypeSpecTest} for form fields
 */
public class FieldTypesSpecTest extends AbstractTypeSpecTest {

  /**
   * ID used for linking and as text-key for title
   */
  public static final String ID = "org.eclipse.scout.rt.spec.fieldtypes";

  public FieldTypesSpecTest() {
    super(ID, TEXTS.get(ID), TEXTS.getWithFallback(ID + ".introduction", null), IFormField.class);
  }
}
