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
