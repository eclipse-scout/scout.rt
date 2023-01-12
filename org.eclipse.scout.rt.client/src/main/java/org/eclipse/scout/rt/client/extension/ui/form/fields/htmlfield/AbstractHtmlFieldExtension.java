/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.htmlfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.htmlfield.HtmlFieldChains.HtmlFieldAppLinkActionChain;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;

public abstract class AbstractHtmlFieldExtension<OWNER extends AbstractHtmlField> extends AbstractValueFieldExtension<String, OWNER> implements IHtmlFieldExtension<OWNER> {

  public AbstractHtmlFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execAppLinkAction(HtmlFieldAppLinkActionChain chain, String ref) {
    chain.execAppLinkAction(ref);
  }
}
