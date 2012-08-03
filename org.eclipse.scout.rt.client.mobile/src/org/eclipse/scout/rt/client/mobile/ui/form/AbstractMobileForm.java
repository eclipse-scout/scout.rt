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
package org.eclipse.scout.rt.client.mobile.ui.form;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.IMobileForm;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;

/**
 * @since 3.9.0
 */
public abstract class AbstractMobileForm extends AbstractForm implements IMobileForm {

  public AbstractMobileForm() throws ProcessingException {
    this(true);
  }

  public AbstractMobileForm(boolean callInitializer) throws ProcessingException {
    super(callInitializer);
  }

  @Override
  protected void initConfig() throws ProcessingException {
    super.initConfig();

    setFooterVisible(getConfiguredFooterVisible());
  }

  protected boolean getConfiguredFooterVisible() {
    return false;
  }

  @Override
  public boolean isFooterVisible() {
    return propertySupport.getPropertyBool(PROP_FOOTER_VISIBLE);
  }

  @Override
  public void setFooterVisible(boolean visible) {
    propertySupport.setPropertyBool(PROP_FOOTER_VISIBLE, visible);
  }

}
