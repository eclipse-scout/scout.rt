package org.eclipse.scout.rt.server.extension;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.AbstractServerSession;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class ServerSessionChains {

  private ServerSessionChains() {
  }

  protected abstract static class AbstractServerSessionChain extends AbstractExtensionChain<IServerSessionExtension<? extends AbstractServerSession>> {

    public AbstractServerSessionChain(List<? extends IServerSessionExtension<? extends AbstractServerSession>> extensions) {
      super(extensions, IServerSessionExtension.class);
    }
  }

  public static class ServerSessionLoadSessionChain extends AbstractServerSessionChain {

    public ServerSessionLoadSessionChain(List<? extends IServerSessionExtension<? extends AbstractServerSession>> extensions) {
      super(extensions);
    }

    public void execLoadSession() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IServerSessionExtension<? extends AbstractServerSession> next) throws ProcessingException {
          next.execLoadSession(ServerSessionLoadSessionChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
