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
package org.eclipse.scout.rt.client.ui.form.fields.datefield;

import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * convenience subclass of {@link AbstractDateField} with hasDate=false and hasTime=true
 */
@ClassId("c9f9056a-02ab-4d8e-b80e-16fa81010617")
public abstract class AbstractTimeField extends AbstractDateField {
  public AbstractTimeField() {
    this(true);
  }

  public AbstractTimeField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected boolean getConfiguredHasTime() {
    return true;
  }

  @Override
  protected boolean getConfiguredHasDate() {
    return false;
  }
}
