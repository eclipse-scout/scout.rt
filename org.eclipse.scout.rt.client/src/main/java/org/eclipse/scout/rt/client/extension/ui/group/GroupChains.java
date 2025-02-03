/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
