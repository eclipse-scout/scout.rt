/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.form;

import org.eclipse.scout.rt.client.extension.ui.action.menu.IMenuExtension;
import org.eclipse.scout.rt.client.extension.ui.form.FormMenuChains.FormMenuInitFormChain;
import org.eclipse.scout.rt.client.ui.form.AbstractFormMenu;
import org.eclipse.scout.rt.client.ui.form.IForm;

public interface IFormMenuExtension<FORM extends IForm, OWNER extends AbstractFormMenu<FORM>> extends IMenuExtension<OWNER> {

  void execInitForm(FormMenuInitFormChain<FORM> chain, FORM form);
}
