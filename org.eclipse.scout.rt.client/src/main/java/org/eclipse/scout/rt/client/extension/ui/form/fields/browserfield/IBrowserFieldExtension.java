/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield.BrowserFieldChains.BrowserFieldExternalWindowStateChangedChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield.BrowserFieldChains.BrowserFieldPostMessageChain;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.AbstractBrowserField;

public interface IBrowserFieldExtension<OWNER extends AbstractBrowserField> extends IFormFieldExtension<OWNER> {

  void execPostMessage(BrowserFieldPostMessageChain chain, Object data, String origin);

  void execExternalWindowStateChanged(BrowserFieldExternalWindowStateChangedChain chain, boolean state);
}
