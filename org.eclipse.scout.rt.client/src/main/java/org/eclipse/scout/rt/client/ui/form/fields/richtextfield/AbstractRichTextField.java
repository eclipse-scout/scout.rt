/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.richtextfield;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractBasicField;
import org.eclipse.scout.rt.client.ui.form.fields.IBasicFieldUIFacade;
import org.eclipse.scout.rt.platform.BEANS;

public abstract class AbstractRichTextField extends AbstractBasicField<String>implements IRichTextField {
  private IBasicFieldUIFacade m_uiFacade;

  public AbstractRichTextField() {
    this(true);
  }

  protected AbstractRichTextField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
  }

  @Override
  public IBasicFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }
}
