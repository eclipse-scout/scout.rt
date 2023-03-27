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
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions;

public class ObjectExtensions<OWNER, EXTENSION extends IExtension<? extends OWNER>> implements IExtensibleObject, Serializable {
  private static final long serialVersionUID = 1L;

  private final OWNER m_owner;
  private final boolean m_requiresNewScope;
  private List<EXTENSION> m_extensions;
  private ExtensionContext m_extensionContext;

  public ObjectExtensions(OWNER owner, boolean requiresNewScope) {
    m_owner = owner;
    m_requiresNewScope = requiresNewScope;
  }

  @Override
  public List<EXTENSION> getAllExtensions() {
    if (m_extensions == null) {
      throw new IllegalStateException("The model object is not initialized: " + m_owner + ".");
    }
    return m_extensions;
  }

  @Override
  public <T extends IExtension<?>> T getExtension(Class<T> c) {
    if (c == null) {
      return null;
    }
    if (m_extensions == null) {
      throw new IllegalStateException("The model object is not initialized: " + m_owner + ".");
    }
    for (EXTENSION extension : m_extensions) {
      if (c == extension.getClass()) {
        return c.cast(extension);
      }
    }
    return null;
  }

  public void initConfig(EXTENSION localExtension, Runnable modelObjectInitializer) {
    initConfig(localExtension, false, modelObjectInitializer);
  }

  public void initConfigAndBackupExtensionContext(EXTENSION localExtension, Runnable modelObjectInitializer) {
    initConfig(localExtension, true, modelObjectInitializer);
  }

  protected void initConfig(final EXTENSION localExtension, final boolean backupExtensionContext, final Runnable modelObjectInitializer) {
    if (m_extensions != null) {
      throw new IllegalStateException("The model object is already initialized: " + m_owner + ".");
    }
    final IInternalExtensionRegistry extensionRegistry = BEANS.get(IInternalExtensionRegistry.class);
    if (extensionRegistry == null) {
      return;
    }

    Runnable initConfigRunnable = () -> {
      try {
        extensionRegistry.pushScope(m_owner.getClass());
        m_extensions = loadExtensions(localExtension);
        try {
          extensionRegistry.pushExtensions(m_extensions);
          if (backupExtensionContext) {
            m_extensionContext = extensionRegistry.backupExtensionContext();
          }
          if (modelObjectInitializer != null) {
            modelObjectInitializer.run();
          }
        }
        finally {
          extensionRegistry.popExtensions(m_extensions);
        }
      }
      finally {
        extensionRegistry.popScope();
      }
    };

    if (m_requiresNewScope) {
      extensionRegistry.runInContext(null, initConfigRunnable);
    }
    else {
      initConfigRunnable.run();
    }
  }

  /**
   * Executes the given runnable in the extension context, in which the referencing model object was created.
   * {@link #initConfigAndBackupExtensionContext(IExtension, Runnable)} must have been invoked before.
   */
  public void runInExtensionContext(Runnable runnable) {
    IInternalExtensionRegistry extensionRegistry = BEANS.get(IInternalExtensionRegistry.class);
    if (extensionRegistry == null) {
      return;
    }
    Assertions.assertNotNull(m_extensionContext, "The extension context has not been backed-up for {}. "
        + "Use initConfigAndBackupExtensionContext for initializing this instance.", m_owner);
    extensionRegistry.runInContext(m_extensionContext, runnable);
  }

  private List<EXTENSION> loadExtensions(EXTENSION localExtension) {
    List<EXTENSION> extensions = BEANS.get(IInternalExtensionRegistry.class).createExtensionsFor(m_owner);
    if (extensions.isEmpty()) {
      return Collections.singletonList(localExtension);
    }
    extensions.add(localExtension);
    return Collections.unmodifiableList(extensions);
  }
}
