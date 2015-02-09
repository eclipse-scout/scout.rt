package org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
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

    public void execFilterBrowseLookupResult(final ILookupCall<LOOKUP_KEY> call, final List<ILookupRow<LOOKUP_KEY>> result) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IContentAssistFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> next) throws ProcessingException {
          next.execFilterBrowseLookupResult(ContentAssistFieldFilterBrowseLookupResultChain.this, call, result);
        }
      };
      callChain(methodInvocation, call, result);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class ContentAssistFieldBrowseNewChain<VALUE, LOOKUP_KEY> extends AbstractContentAssistFieldChain<VALUE, LOOKUP_KEY> {

    public ContentAssistFieldBrowseNewChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public ILookupRow<LOOKUP_KEY> execBrowseNew(final String searchText) throws ProcessingException {
      MethodInvocation<ILookupRow<LOOKUP_KEY>> methodInvocation = new MethodInvocation<ILookupRow<LOOKUP_KEY>>() {
        @Override
        protected void callMethod(IContentAssistFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> next) throws ProcessingException {
          setReturnValue(next.execBrowseNew(ContentAssistFieldBrowseNewChain.this, searchText));
        }
      };
      callChain(methodInvocation, searchText);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
    }
  }

  public static class ContentAssistFieldFilterKeyLookupResultChain<VALUE, LOOKUP_KEY> extends AbstractContentAssistFieldChain<VALUE, LOOKUP_KEY> {

    public ContentAssistFieldFilterKeyLookupResultChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execFilterKeyLookupResult(final ILookupCall<LOOKUP_KEY> call, final List<ILookupRow<LOOKUP_KEY>> result) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IContentAssistFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> next) throws ProcessingException {
          next.execFilterKeyLookupResult(ContentAssistFieldFilterKeyLookupResultChain.this, call, result);
        }
      };
      callChain(methodInvocation, call, result);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class ContentAssistFieldPrepareLookupChain<VALUE, LOOKUP_KEY> extends AbstractContentAssistFieldChain<VALUE, LOOKUP_KEY> {

    public ContentAssistFieldPrepareLookupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPrepareLookup(final ILookupCall<LOOKUP_KEY> call) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IContentAssistFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> next) throws ProcessingException {
          next.execPrepareLookup(ContentAssistFieldPrepareLookupChain.this, call);
        }
      };
      callChain(methodInvocation, call);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class ContentAssistFieldPrepareTextLookupChain<VALUE, LOOKUP_KEY> extends AbstractContentAssistFieldChain<VALUE, LOOKUP_KEY> {

    public ContentAssistFieldPrepareTextLookupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPrepareTextLookup(final ILookupCall<LOOKUP_KEY> call, final String text) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IContentAssistFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> next) throws ProcessingException {
          next.execPrepareTextLookup(ContentAssistFieldPrepareTextLookupChain.this, call, text);
        }
      };
      callChain(methodInvocation, call, text);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class ContentAssistFieldPrepareBrowseLookupChain<VALUE, LOOKUP_KEY> extends AbstractContentAssistFieldChain<VALUE, LOOKUP_KEY> {

    public ContentAssistFieldPrepareBrowseLookupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPrepareBrowseLookup(final ILookupCall<LOOKUP_KEY> call, final String browseHint) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IContentAssistFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> next) throws ProcessingException {
          next.execPrepareBrowseLookup(ContentAssistFieldPrepareBrowseLookupChain.this, call, browseHint);
        }
      };
      callChain(methodInvocation, call, browseHint);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class ContentAssistFieldFilterTextLookupResultChain<VALUE, LOOKUP_KEY> extends AbstractContentAssistFieldChain<VALUE, LOOKUP_KEY> {

    public ContentAssistFieldFilterTextLookupResultChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execFilterTextLookupResult(final ILookupCall<LOOKUP_KEY> call, final List<ILookupRow<LOOKUP_KEY>> result) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IContentAssistFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> next) throws ProcessingException {
          next.execFilterTextLookupResult(ContentAssistFieldFilterTextLookupResultChain.this, call, result);
        }
      };
      callChain(methodInvocation, call, result);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class ContentAssistFieldPrepareRecLookupChain<VALUE, LOOKUP_KEY> extends AbstractContentAssistFieldChain<VALUE, LOOKUP_KEY> {

    public ContentAssistFieldPrepareRecLookupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPrepareRecLookup(final ILookupCall<LOOKUP_KEY> call, final LOOKUP_KEY parentKey) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IContentAssistFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> next) throws ProcessingException {
          next.execPrepareRecLookup(ContentAssistFieldPrepareRecLookupChain.this, call, parentKey);
        }
      };
      callChain(methodInvocation, call, parentKey);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class ContentAssistFieldFilterLookupResultChain<VALUE, LOOKUP_KEY> extends AbstractContentAssistFieldChain<VALUE, LOOKUP_KEY> {

    public ContentAssistFieldFilterLookupResultChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execFilterLookupResult(final ILookupCall<LOOKUP_KEY> call, final List<ILookupRow<LOOKUP_KEY>> result) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IContentAssistFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> next) throws ProcessingException {
          next.execFilterLookupResult(ContentAssistFieldFilterLookupResultChain.this, call, result);
        }
      };
      callChain(methodInvocation, call, result);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class ContentAssistFieldFilterRecLookupResultChain<VALUE, LOOKUP_KEY> extends AbstractContentAssistFieldChain<VALUE, LOOKUP_KEY> {

    public ContentAssistFieldFilterRecLookupResultChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execFilterRecLookupResult(final ILookupCall<LOOKUP_KEY> call, final List<ILookupRow<LOOKUP_KEY>> result) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IContentAssistFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> next) throws ProcessingException {
          next.execFilterRecLookupResult(ContentAssistFieldFilterRecLookupResultChain.this, call, result);
        }
      };
      callChain(methodInvocation, call, result);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class ContentAssistFieldPrepareKeyLookupChain<VALUE, LOOKUP_KEY> extends AbstractContentAssistFieldChain<VALUE, LOOKUP_KEY> {

    public ContentAssistFieldPrepareKeyLookupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPrepareKeyLookup(final ILookupCall<LOOKUP_KEY> call, final LOOKUP_KEY key) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IContentAssistFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> next) throws ProcessingException {
          next.execPrepareKeyLookup(ContentAssistFieldPrepareKeyLookupChain.this, call, key);
        }
      };
      callChain(methodInvocation, call, key);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
