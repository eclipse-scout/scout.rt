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
package org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield.BrowserFieldChains.BrowserFieldLocationChangedChain;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.AbstractBrowserField;

public abstract class AbstractBrowserFieldExtension<OWNER extends AbstractBrowserField> extends AbstractFormFieldExtension<OWNER> implements IBrowserFieldExtension<OWNER> {

  public AbstractBrowserFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execPostMessage(BrowserFieldLocationChangedChain chain, String data, String origin) {
    chain.execPostMessage(data, origin);
  }

}
