/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.textfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.textfield.ITextFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * Convenience subclass for {@link AbstractStringField} due to popular usage of the word "textField" instead of
 * "stringField"<br>
 * do not add further code or methods to this class
 */
@ClassId("53abcbfa-f3d8-48f8-8467-ba61c10b2abb")
public abstract class AbstractTextField extends AbstractStringField implements ITextField {

  public AbstractTextField() {
    this(true);
  }

  public AbstractTextField(boolean callInitializer) {
    super(callInitializer);
  }

  protected static class LocalTextFieldExtension<OWNER extends AbstractTextField> extends LocalStringFieldExtension<OWNER> implements ITextFieldExtension<OWNER> {

    public LocalTextFieldExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected ITextFieldExtension<? extends AbstractTextField> createLocalExtension() {
    return new LocalTextFieldExtension<AbstractTextField>(this);
  }
}
