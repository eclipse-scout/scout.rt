/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.extension;

import java.util.List;

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

    public void execLoadSession() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IServerSessionExtension<? extends AbstractServerSession> next) {
          next.execLoadSession(ServerSessionLoadSessionChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }
}
