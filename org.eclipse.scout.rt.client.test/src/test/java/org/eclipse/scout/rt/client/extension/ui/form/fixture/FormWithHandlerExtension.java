/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
