/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.placeholder;

import org.eclipse.scout.rt.client.extension.ui.form.fields.placeholder.IPlaceholderFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("1c651d5d-f327-4c60-8bf4-4a4714bc22cc")
public abstract class AbstractPlaceholderField extends AbstractFormField implements IPlaceholderField {
  public AbstractPlaceholderField() {
    this(true);
  }

  public AbstractPlaceholderField(boolean callInitializer) {
    super(callInitializer);
  }

  protected static class LocalPlaceholderFieldExtension<OWNER extends AbstractPlaceholderField> extends LocalFormFieldExtension<OWNER> implements IPlaceholderFieldExtension<OWNER> {

    public LocalPlaceholderFieldExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IPlaceholderFieldExtension<? extends AbstractPlaceholderField> createLocalExtension() {
    return new LocalPlaceholderFieldExtension<>(this);
  }
}
