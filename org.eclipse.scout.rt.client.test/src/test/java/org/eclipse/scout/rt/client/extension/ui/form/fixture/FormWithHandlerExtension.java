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
package org.eclipse.scout.rt.client.extension.ui.form.fixture;

import org.eclipse.scout.rt.client.extension.ui.form.AbstractFormExtension;
import org.eclipse.scout.rt.client.extension.ui.form.AbstractFormHandlerExtension;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerLoadChain;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.FormWithHandler.ModifyHandler;

/**
 * @since 6.0
 */
public class FormWithHandlerExtension extends AbstractFormExtension<FormWithHandler> {

  public FormWithHandlerExtension(FormWithHandler ownerForm) {
    super(ownerForm);
  }

  public static class ModifyHandlerExtension extends AbstractFormHandlerExtension<FormWithHandler.ModifyHandler> {

    private boolean m_loaded;

    public ModifyHandlerExtension(ModifyHandler owner) {
      super(owner);
    }

    @Override
    public void execLoad(FormHandlerLoadChain chain) {
      m_loaded = true;
      chain.execLoad();
    }

    public boolean isLoaded() {
      return m_loaded;
    }
  }
}
