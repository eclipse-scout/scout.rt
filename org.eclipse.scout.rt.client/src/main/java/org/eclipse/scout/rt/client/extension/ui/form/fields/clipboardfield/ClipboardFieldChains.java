/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
