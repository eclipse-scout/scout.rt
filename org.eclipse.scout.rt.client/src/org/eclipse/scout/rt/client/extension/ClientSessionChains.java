package org.eclipse.scout.rt.client.extension;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class ClientSessionChains {

  private ClientSessionChains() {
  }

  protected abstract static class AbstractClientSessionChain extends AbstractExtensionChain<IClientSessionExtension<? extends AbstractClientSession>> {

    public AbstractClientSessionChain(List<? extends IClientSessionExtension<? extends AbstractClientSession>> extensions) {
      super(extensions, IClientSessionExtension.class);
    }
  }

  public static class ClientSessionStoreSessionChain extends AbstractClientSessionChain {

    public ClientSessionStoreSessionChain(List<? extends IClientSessionExtension<? extends AbstractClientSession>> extensions) {
      super(extensions);
    }

    public void execStoreSession() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IClientSessionExtension<? extends AbstractClientSession> next) throws ProcessingException {
          next.execStoreSession(ClientSessionStoreSessionChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class ClientSessionLoadSessionChain extends AbstractClientSessionChain {

    public ClientSessionLoadSessionChain(List<? extends IClientSessionExtension<? extends AbstractClientSession>> extensions) {
      super(extensions);
    }

    public void execLoadSession() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IClientSessionExtension<? extends AbstractClientSession> next) throws ProcessingException {
          next.execLoadSession(ClientSessionLoadSessionChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
