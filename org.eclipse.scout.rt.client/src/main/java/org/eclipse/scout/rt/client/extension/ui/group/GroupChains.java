/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.group;

import java.util.List;

import org.eclipse.scout.rt.client.ui.group.AbstractGroup;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class GroupChains {

  private GroupChains() {
  }

  public abstract static class AbstractGroupChain extends AbstractExtensionChain<IGroupExtension<? extends AbstractGroup>> {

    public AbstractGroupChain(List<? extends IGroupExtension<? extends AbstractGroup>> extensions) {
      super(extensions, IGroupExtension.class);
    }

  }

  public static class GroupDisposeGroupChain extends AbstractGroupChain {

    public GroupDisposeGroupChain(List<? extends IGroupExtension<? extends AbstractGroup>> extensions) {
      super(extensions);
    }

    public void execDisposeGroup() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IGroupExtension<? extends AbstractGroup> next) {
          next.execDisposeGroup(GroupDisposeGroupChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class GroupInitGroupChain extends AbstractGroupChain {

    public GroupInitGroupChain(List<? extends IGroupExtension<? extends AbstractGroup>> extensions) {
      super(extensions);
    }

    public void execInitGroup() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IGroupExtension<? extends AbstractGroup> next) {
          next.execInitGroup(GroupInitGroupChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

}
