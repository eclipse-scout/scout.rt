/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.serialization.IObjectReplacer;
import org.osgi.framework.Bundle;

/**
 * Serialization specialization to be used in osgi environments with bundle class loading instead of flat class loading<br>
 * <p>
 * see also {@link BundleObjectOutputStream}
 */
public class BundleObjectInputStream extends ObjectInputStream {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BundleObjectInputStream.class);
  private final ClassLoader m_classLoader;
  private final IObjectReplacer m_objectReplacer;

  public BundleObjectInputStream(InputStream in, Bundle[] bundleList) throws IOException {
    this(in, new BundleListClassLoader(null, bundleList), null);
  }

  public BundleObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException {
    this(in, classLoader, null);
  }

  public BundleObjectInputStream(InputStream in, ClassLoader classLoader, IObjectReplacer objectReplacer) throws IOException {
    super(in);
    if (classLoader == null) {
      throw new IllegalArgumentException("classLoader must not be null");
    }
    m_classLoader = classLoader;
    m_objectReplacer = objectReplacer;
    enableResolveObject(true);
  }

  /**
   * explicitly made public to allow object replacers to load classes via the input stream
   */
  @Override
  public Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
    return m_classLoader.loadClass(desc.getName());
  }

  @Override
  protected Object resolveObject(Object obj) throws IOException {
    if (m_objectReplacer != null) {
      return m_objectReplacer.resolveObject(obj);
    }
    return super.resolveObject(obj);
  }
}
