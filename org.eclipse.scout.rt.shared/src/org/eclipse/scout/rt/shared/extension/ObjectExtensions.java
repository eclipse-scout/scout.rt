/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.extension;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.service.SERVICES;

public class ObjectExtensions<OWNER, EXTENSION extends IExtension<? extends OWNER>> implements IExtensibleObject, Serializable {
  private static final long serialVersionUID = 1L;

  private final OWNER m_owner;
  private List<EXTENSION> m_extensions;
  private ExtensionContext m_extensionContext;

  public ObjectExtensions(OWNER owner) {
    m_owner = owner;
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

  protected void initConfig(EXTENSION localExtension, boolean backupExtensionContext, Runnable modelObjectInitializer) {
    if (m_extensions != null) {
      throw new IllegalStateException("The model object is already initialized: " + m_owner + ".");
    }
    IInternalExtensionRegistry extensionRegistry = SERVICES.getService(IInternalExtensionRegistry.class);
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
  }

  /**
   * Executes the given runnable in the extension context, in which the referencing model object was created.
   * {@link #initConfigAndBackupExtensionContext(IExtension, Runnable)} must have been invoked before.
   */
  public void runInExtensionContext(Runnable runnable) {
    IInternalExtensionRegistry extensionRegistry = SERVICES.getService(IInternalExtensionRegistry.class);
    if (extensionRegistry == null) {
      return;
    }
    if (m_extensionContext == null) {
      throw new IllegalArgumentException("The extension context has not been backed-up for " + m_owner + ". Use initConfigAndBackupExtensionContext for initializing this instance.");
    }
    extensionRegistry.runInContext(m_extensionContext, runnable);
  }

  private List<EXTENSION> loadExtensions(EXTENSION localExtension) {
    List<EXTENSION> extensions = SERVICES.getService(IInternalExtensionRegistry.class).createExtensionsFor(m_owner);
    extensions.add(localExtension);
    return Collections.unmodifiableList(extensions);
  }
}
