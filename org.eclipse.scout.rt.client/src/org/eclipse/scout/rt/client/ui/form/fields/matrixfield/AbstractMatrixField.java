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
package org.eclipse.scout.rt.client.ui.form.fields.matrixfield;

import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;

/**
 * @deprecated use {@link org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField} instead
 */
@SuppressWarnings("deprecation")
@Deprecated
public abstract class AbstractMatrixField extends AbstractFormField implements IMatrixField {

  public AbstractMatrixField() {
    this(true);
  }

  public AbstractMatrixField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
  }

}
