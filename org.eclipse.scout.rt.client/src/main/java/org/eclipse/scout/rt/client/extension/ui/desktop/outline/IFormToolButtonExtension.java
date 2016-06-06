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
package org.eclipse.scout.rt.client.extension.ui.desktop.outline;

import org.eclipse.scout.rt.client.extension.ui.desktop.outline.FormToolButtonChains.FormToolButtonInitFormChain;
import org.eclipse.scout.rt.client.extension.ui.form.IFormMenuExtension;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractFormToolButton;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * @deprecated use {@link IFormMenuExtension} instead, will be removed in Scout 6.1
 */
@SuppressWarnings("deprecation")
@Deprecated
public interface IFormToolButtonExtension<FORM extends IForm, OWNER extends AbstractFormToolButton<FORM>> extends IFormMenuExtension<FORM, OWNER> {

  void execInitForm(FormToolButtonInitFormChain<FORM> chain, FORM form);
}
