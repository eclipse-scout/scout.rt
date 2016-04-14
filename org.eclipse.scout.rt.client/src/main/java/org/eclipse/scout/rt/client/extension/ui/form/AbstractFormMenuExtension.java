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
