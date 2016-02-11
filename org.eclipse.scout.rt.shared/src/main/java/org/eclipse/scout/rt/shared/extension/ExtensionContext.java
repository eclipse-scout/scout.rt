package org.eclipse.scout.rt.shared.extension;

import java.util.List;

/**
 * Preserves the state of an extension stack and extension scopes.
 * 
 * @since 5.2.0
 */
public class ExtensionContext {

  private final ExtensionScope<ExtensionRegistryItem> m_contributionScope;
  private final ExtensionScope<ExtensionRegistryItem> m_extensionScope;
  private final List<List<? extends IExtension<?>>> m_extensionStack;

  public ExtensionContext(ScopeStack scopeStack, ExtensionStack extensionStack) {
    m_contributionScope = scopeStack.getContributionScope();
    m_extensionScope = scopeStack.getExtensionScope();
    m_extensionStack = extensionStack.snapshot();
  }

  public ScopeStack getScopeStack() {
    return new ScopeStack(m_contributionScope, m_extensionScope);
  }

  public ExtensionStack getExtensionStack() {
    return new ExtensionStack(m_extensionStack);
  }
}
