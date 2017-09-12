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
package org.eclipse.scout.rt.shared.extension;

import java.util.Deque;
import java.util.LinkedList;

public class ScopeStack {

  private final Deque<ExtensionScope<ExtensionRegistryItem>> m_contributionScopes;
  private final Deque<ExtensionScope<ExtensionRegistryItem>> m_extensionScopes;

  public ScopeStack(ExtensionScope<ExtensionRegistryItem> globalContributionScope, ExtensionScope<ExtensionRegistryItem> globalExtensionScope) {
    m_contributionScopes = new LinkedList<>();
    m_extensionScopes = new LinkedList<>();
    m_contributionScopes.push(globalContributionScope);
    m_extensionScopes.push(globalExtensionScope);
  }

  public void pushScope(Class<?> owner) {
    pushScope(owner, m_contributionScopes);
    pushScope(owner, m_extensionScopes);
  }

  public void popScope() {
    m_contributionScopes.pop();
    m_extensionScopes.pop();
  }

  protected void pushScope(Class<?> owner, Deque<ExtensionScope<ExtensionRegistryItem>> scopes) {
    ExtensionScope<ExtensionRegistryItem> currentScope = scopes.peek();
    ExtensionScope<ExtensionRegistryItem> subScope = currentScope.createSubScope(owner);
    if (subScope != null) {
      scopes.push(subScope);
    }
    else {
      scopes.push(currentScope);
    }
  }

  public ExtensionScope<ExtensionRegistryItem> getContributionScope() {
    return m_contributionScopes.peek();
  }

  public ExtensionScope<ExtensionRegistryItem> getExtensionScope() {
    return m_extensionScopes.peek();
  }

  public boolean isEmpty() {
    return m_contributionScopes.size() == 1 && m_extensionScopes.size() == 1;
  }
}
