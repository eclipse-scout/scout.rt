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
package org.eclipse.scout.rt.client.ui.form.fields.checkbox;

import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.AbstractBooleanField;

/**
 * Convenience subclass for {@link AbstractBooleanField} due to popular usage of
 * the word "checkBox" instead of "booleanField"<br>
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
}
