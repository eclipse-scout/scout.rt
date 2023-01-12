/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form;

import org.eclipse.scout.rt.client.extension.ui.action.menu.AbstractMenuExtension;
import org.eclipse.scout.rt.client.extension.ui.form.FormMenuChains.FormMenuInitFormChain;
import org.eclipse.scout.rt.client.ui.form.AbstractFormMenu;
import org.eclipse.scout.rt.client.ui.form.IForm;

public abstract class AbstractFormMenuExtension<FORM extends IForm, OWNER extends AbstractFormMenu<FORM>> extends AbstractMenuExtension<OWNER> implements IFormMenuExtension<FORM, OWNER> {

  public AbstractFormMenuExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execInitForm(FormMenuInitFormChain<FORM> chain, FORM form) {
    chain.execInitForm(form);
  }
}
