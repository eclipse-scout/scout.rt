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
package org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractContentAssistField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public final class ContentAssistFieldChains {

  private ContentAssistFieldChains() {
  }

  protected abstract static class AbstractContentAssistFieldChain<VALUE, LOOKUP_KEY> extends AbstractExtensionChain<IContentAssistFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractContentAssistField<VALUE, LOOKUP_KEY>>> {

    public AbstractContentAssistFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IContentAssistFieldExtension.class);
    }
  }

  public static class ContentAssistFieldFilterBrowseLookupResultChain<VALUE, LOOKUP_KEY> extends AbstractContentAssistFieldChain<VALUE, LOOKUP_KEY> {

    public ContentAssistFieldFilterBrowseLookupResultChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execFilterBrowseLookupResult(final ILookupCall<LOOKUP_KEY> call, final List<ILookupRow<LOOKUP_KEY>> result) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IContentAssistFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> next) {
          next.execFilterBrowseLookupResult(ContentAssistFieldFilterBrowseLookupResultChain.this, call, result);
        }
      };
      callChain(methodInvocation, call, result);
    }
  }

  public static class ContentAssistFieldBrowseNewChain<VALUE, LOOKUP_KEY> extends AbstractContentAssistFieldChain<VALUE, LOOKUP_KEY> {

    public ContentAssistFieldBrowseNewChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public ILookupRow<LOOKUP_KEY> execBrowseNew(final String searchText) {
      MethodInvocation<ILookupRow<LOOKUP_KEY>> methodInvocation = new MethodInvocation<ILookupRow<LOOKUP_KEY>>() {
        @Override
        protected void callMethod(IContentAssistFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> next) {
          setReturnValue(next.execBrowseNew(ContentAssistFieldBrowseNewChain.this, searchText));
        }
      };
      callChain(methodInvocation, searchText);
      return methodInvocation.getReturnValue();
    }
  }

  public static class ContentAssistFieldFilterKeyLookupResultChain<VALUE, LOOKUP_KEY> extends AbstractContentAssistFieldChain<VALUE, LOOKUP_KEY> {

    public ContentAssistFieldFilterKeyLookupResultChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execFilterKeyLookupResult(final ILookupCall<LOOKUP_KEY> call, final List<ILookupRow<LOOKUP_KEY>> result) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IContentAssistFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> next) {
          next.execFilterKeyLookupResult(ContentAssistFieldFilterKeyLookupResultChain.this, call, result);
        }
      };
      callChain(methodInvocation, call, result);
    }
  }

  public static class ContentAssistFieldPrepareLookupChain<VALUE, LOOKUP_KEY> extends AbstractContentAssistFieldChain<VALUE, LOOKUP_KEY> {

    public ContentAssistFieldPrepareLookupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPrepareLookup(final ILookupCall<LOOKUP_KEY> call) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IContentAssistFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> next) {
          next.execPrepareLookup(ContentAssistFieldPrepareLookupChain.this, call);
        }
      };
      callChain(methodInvocation, call);
    }
  }

  public static class ContentAssistFieldPrepareTextLookupChain<VALUE, LOOKUP_KEY> extends AbstractContentAssistFieldChain<VALUE, LOOKUP_KEY> {

    public ContentAssistFieldPrepareTextLookupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPrepareTextLookup(final ILookupCall<LOOKUP_KEY> call, final String text) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IContentAssistFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> next) {
          next.execPrepareTextLookup(ContentAssistFieldPrepareTextLookupChain.this, call, text);
        }
      };
      callChain(methodInvocation, call, text);
    }
  }

  public static class ContentAssistFieldPrepareBrowseLookupChain<VALUE, LOOKUP_KEY> extends AbstractContentAssistFieldChain<VALUE, LOOKUP_KEY> {

    public ContentAssistFieldPrepareBrowseLookupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPrepareBrowseLookup(final ILookupCall<LOOKUP_KEY> call, final String browseHint) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IContentAssistFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> next) {
          next.execPrepareBrowseLookup(ContentAssistFieldPrepareBrowseLookupChain.this, call, browseHint);
        }
      };
      callChain(methodInvocation, call, browseHint);
    }
  }

  public static class ContentAssistFieldFilterTextLookupResultChain<VALUE, LOOKUP_KEY> extends AbstractContentAssistFieldChain<VALUE, LOOKUP_KEY> {

    public ContentAssistFieldFilterTextLookupResultChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execFilterTextLookupResult(final ILookupCall<LOOKUP_KEY> call, final List<ILookupRow<LOOKUP_KEY>> result) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IContentAssistFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> next) {
          next.execFilterTextLookupResult(ContentAssistFieldFilterTextLookupResultChain.this, call, result);
        }
      };
      callChain(methodInvocation, call, result);
    }
  }

  public static class ContentAssistFieldPrepareRecLookupChain<VALUE, LOOKUP_KEY> extends AbstractContentAssistFieldChain<VALUE, LOOKUP_KEY> {

    public ContentAssistFieldPrepareRecLookupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPrepareRecLookup(final ILookupCall<LOOKUP_KEY> call, final LOOKUP_KEY parentKey) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IContentAssistFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> next) {
          next.execPrepareRecLookup(ContentAssistFieldPrepareRecLookupChain.this, call, parentKey);
        }
      };
      callChain(methodInvocation, call, parentKey);
    }
  }

  public static class ContentAssistFieldFilterLookupResultChain<VALUE, LOOKUP_KEY> extends AbstractContentAssistFieldChain<VALUE, LOOKUP_KEY> {

    public ContentAssistFieldFilterLookupResultChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execFilterLookupResult(final ILookupCall<LOOKUP_KEY> call, final List<ILookupRow<LOOKUP_KEY>> result) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IContentAssistFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> next) {
          next.execFilterLookupResult(ContentAssistFieldFilterLookupResultChain.this, call, result);
        }
      };
      callChain(methodInvocation, call, result);
    }
  }

  public static class ContentAssistFieldFilterRecLookupResultChain<VALUE, LOOKUP_KEY> extends AbstractContentAssistFieldChain<VALUE, LOOKUP_KEY> {

    public ContentAssistFieldFilterRecLookupResultChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execFilterRecLookupResult(final ILookupCall<LOOKUP_KEY> call, final List<ILookupRow<LOOKUP_KEY>> result) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IContentAssistFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> next) {
          next.execFilterRecLookupResult(ContentAssistFieldFilterRecLookupResultChain.this, call, result);
        }
      };
      callChain(methodInvocation, call, result);
    }
  }

  public static class ContentAssistFieldPrepareKeyLookupChain<VALUE, LOOKUP_KEY> extends AbstractContentAssistFieldChain<VALUE, LOOKUP_KEY> {

    public ContentAssistFieldPrepareKeyLookupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPrepareKeyLookup(final ILookupCall<LOOKUP_KEY> call, final LOOKUP_KEY key) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IContentAssistFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> next) {
          next.execPrepareKeyLookup(ContentAssistFieldPrepareKeyLookupChain.this, call, key);
        }
      };
      callChain(methodInvocation, call, key);
    }
  }
}
