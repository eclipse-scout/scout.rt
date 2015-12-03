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
