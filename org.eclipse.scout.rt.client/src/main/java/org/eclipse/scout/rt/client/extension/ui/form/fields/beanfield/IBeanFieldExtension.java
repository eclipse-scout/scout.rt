/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.beanfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.beanfield.BeanFieldChains.BeanFieldAppLinkActionChain;
import org.eclipse.scout.rt.client.ui.form.fields.beanfield.AbstractBeanField;

public interface IBeanFieldExtension<VALUE, OWNER extends AbstractBeanField<VALUE>> extends IValueFieldExtension<VALUE, OWNER> {

  void execAppLinkAction(BeanFieldAppLinkActionChain<VALUE> chain, String ref);
}
