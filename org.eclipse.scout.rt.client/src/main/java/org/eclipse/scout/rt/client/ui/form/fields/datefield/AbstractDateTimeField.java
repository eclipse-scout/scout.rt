/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.datefield;

import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * convenience subclass of {@link AbstractDateField} with hasDate=true and hasTime=true
 */
@ClassId("7475d45c-396f-44c5-bb72-4610d980d3ac")
public abstract class AbstractDateTimeField extends AbstractDateField {

  public AbstractDateTimeField() {
    this(true);
  }

  public AbstractDateTimeField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected boolean getConfiguredHasTime() {
    return true;
  }
}
