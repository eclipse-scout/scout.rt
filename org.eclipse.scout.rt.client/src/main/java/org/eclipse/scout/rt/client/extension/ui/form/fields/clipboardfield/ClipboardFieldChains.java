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
