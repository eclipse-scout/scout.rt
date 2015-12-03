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
package org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.imagefield.AbstractImageField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class ImageFieldChains {

  private ImageFieldChains() {
  }

  protected abstract static class AbstractImageFieldChain extends AbstractExtensionChain<IImageFieldExtension<? extends AbstractImageField>> {

    public AbstractImageFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IImageFieldExtension.class);
    }
  }

  public static class ImageFieldDragRequestChain extends AbstractImageFieldChain {

    public ImageFieldDragRequestChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public TransferObject execDragRequest() {
      MethodInvocation<TransferObject> methodInvocation = new MethodInvocation<TransferObject>() {
        @Override
        protected void callMethod(IImageFieldExtension<? extends AbstractImageField> next) {
          setReturnValue(next.execDragRequest(ImageFieldDragRequestChain.this));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }

  public static class ImageFieldDropRequestChain extends AbstractImageFieldChain {

    public ImageFieldDropRequestChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execDropRequest(final TransferObject transferObject) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IImageFieldExtension<? extends AbstractImageField> next) {
          next.execDropRequest(ImageFieldDropRequestChain.this, transferObject);
        }
      };
      callChain(methodInvocation, transferObject);
    }
  }
}
