/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.extension;

import java.io.Serializable;
import java.util.List;

/**
 * Preserves the state of an extension stack and extension scopes.
 *
 * @since 5.2.0
 */
public class ExtensionContext implements Serializable {

  private static final long serialVersionUID = 1L;

  private final ExtensionScope<ExtensionRegistryItem> m_contributionScope;
  private final ExtensionScope<ExtensionRegistryItem> m_extensionScope;
  private final List<List<? extends IExtension<?>>> m_extensionStack;

  public ExtensionContext(ScopeStack scopeStack, ExtensionStack extensionStack) {
    m_contributionScope = scopeStack.getContributionScope();
    m_extensionScope = scopeStack.getExtensionScope();
    m_extensionStack = extensionStack.snapshot();
  }

  public ScopeStack getScopeStack() {
    return new BackupScopeStack(m_contributionScope, m_extensionScope);
  }

  public ExtensionStack getExtensionStack() {
    return new ExtensionStack(m_extensionStack);
  }

  public static class BackupScopeStack extends ScopeStack {

    public BackupScopeStack(ExtensionScope<ExtensionRegistryItem> globalContributionScope, ExtensionScope<ExtensionRegistryItem> globalExtensionScope) {
      super(globalContributionScope, globalExtensionScope);
    }

    @Override
    public void popScope() {
      if (super.isEmpty()) {
        // Always keep the initial item on the backup stack.
        // The thread local is cleared in ExtensionRegistry#runInContext
        throw new IllegalStateException("popScope from empty backup scope stack");
      }
      super.popScope();
    }

    @Override
    public boolean isEmpty() {
      // Backup scope stack is never empty. The initial stack element always remains.
      return false;
    }
  }
}
