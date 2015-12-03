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
package org.eclipse.scout.rt.client.extension;

import java.util.List;

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

    public void execStoreSession() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IClientSessionExtension<? extends AbstractClientSession> next) {
          next.execStoreSession(ClientSessionStoreSessionChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class ClientSessionLoadSessionChain extends AbstractClientSessionChain {

    public ClientSessionLoadSessionChain(List<? extends IClientSessionExtension<? extends AbstractClientSession>> extensions) {
      super(extensions);
    }

    public void execLoadSession() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IClientSessionExtension<? extends AbstractClientSession> next) {
          next.execLoadSession(ClientSessionLoadSessionChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }
}
