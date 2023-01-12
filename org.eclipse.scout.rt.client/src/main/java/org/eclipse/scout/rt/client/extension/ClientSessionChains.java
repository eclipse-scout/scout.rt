/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
