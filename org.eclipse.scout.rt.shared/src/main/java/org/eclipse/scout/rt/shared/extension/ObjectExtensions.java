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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;

public class ObjectExtensions<OWNER, EXTENSION extends IExtension<? extends OWNER>> implements IExtensibleObject, Serializable {
  private static final long serialVersionUID = 1L;

  private final OWNER m_owner;
  private List<EXTENSION> m_extensions;

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
    if (m_extensions != null) {
      throw new IllegalStateException("The model object is already initialized: " + m_owner + ".");
    }
    IInternalExtensionRegistry extensionRegistry = BEANS.get(IInternalExtensionRegistry.class);
    if (extensionRegistry == null) {
      return;
    }
    try {
      extensionRegistry.pushScope(m_owner.getClass());
      m_extensions = loadExtensions(localExtension);
      try {
        extensionRegistry.pushExtensions(m_extensions);
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

  private List<EXTENSION> loadExtensions(EXTENSION localExtension) {
    List<EXTENSION> extensions = BEANS.get(IInternalExtensionRegistry.class).createExtensionsFor(m_owner);
    extensions.add(localExtension);
    return Collections.unmodifiableList(extensions);
  }
}
