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
package org.eclipse.scout.rt.client.extension.ui.form.fields.beanfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.beanfield.BeanFieldChains.BeanFieldAppLinkActionChain;
import org.eclipse.scout.rt.client.ui.form.fields.beanfield.AbstractBeanField;

public abstract class AbstractBeanFieldExtension<VALUE, OWNER extends AbstractBeanField<VALUE>> extends AbstractValueFieldExtension<VALUE, OWNER> implements IBeanFieldExtension<VALUE, OWNER> {

  public AbstractBeanFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execAppLinkAction(BeanFieldAppLinkActionChain chain, String ref) {
    chain.execAppLinkAction(ref);
  }
}
