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
package org.eclipse.scout.rt.client.extension.ui.form.fields.clipboardfield;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.clipboardfield.AbstractClipboardField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class ClipboardFieldChains {

  private ClipboardFieldChains() {
  }

  protected abstract static class AbstractClipboardFieldChain extends AbstractExtensionChain<IClipboardFieldExtension<? extends AbstractClipboardField>> {

    public AbstractClipboardFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IClipboardFieldExtension.class);
    }
  }
}
