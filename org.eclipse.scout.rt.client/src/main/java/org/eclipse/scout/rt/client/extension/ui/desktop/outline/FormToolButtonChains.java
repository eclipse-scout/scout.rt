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

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.action.IActionExtension;
import org.eclipse.scout.rt.client.extension.ui.form.FormMenuChains;
import org.eclipse.scout.rt.client.extension.ui.form.FormMenuChains.AbstractFormMenuChain;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * @deprecated use {@link FormMenuChains} instead
 */
@Deprecated
public final class FormToolButtonChains {

  private FormToolButtonChains() {
  }

  protected abstract static class AbstractFormToolButtonChain<FORM extends IForm> extends AbstractFormMenuChain<FORM> {
    public AbstractFormToolButtonChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions);
    }
  }

  public static class FormToolButtonInitFormChain<FORM extends IForm> extends AbstractFormToolButtonChain<FORM> {

    public FormToolButtonInitFormChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions);
    }
  }
}
