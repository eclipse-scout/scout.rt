/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.checkbox;

import org.eclipse.scout.rt.client.extension.ui.form.fields.checkbox.ICheckBoxExtension;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.AbstractBooleanField;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * Convenience subclass for {@link AbstractBooleanField} due to popular usage of the word "checkBox" instead of
 * "booleanField"<br>
 * do not add further code or methods to this class
 */
@ClassId("cb8efb7d-752a-4e95-955e-b4cb7436e05a")
public abstract class AbstractCheckBox extends AbstractBooleanField implements ICheckBox {

  public AbstractCheckBox() {
    this(true);
  }

  public AbstractCheckBox(boolean callInitializer) {
    super(callInitializer);
  }

  protected static class LocalCheckBoxExtension<OWNER extends AbstractCheckBox> extends LocalBooleanFieldExtension<OWNER> implements ICheckBoxExtension<OWNER> {

    public LocalCheckBoxExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected ICheckBoxExtension<? extends AbstractCheckBox> createLocalExtension() {
    return new LocalCheckBoxExtension<AbstractCheckBox>(this);
  }
}
