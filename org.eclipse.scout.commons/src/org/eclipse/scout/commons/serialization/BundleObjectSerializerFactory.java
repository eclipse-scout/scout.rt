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
package org.eclipse.scout.commons.serialization;

import org.eclipse.scout.commons.osgi.BundleInspector;
import org.eclipse.scout.commons.osgi.BundleListClassLoader;
import org.osgi.framework.Bundle;

/**
 * Factory for creating {@link BundleObjectSerializer} instances. All of them are sharing the very same
 * {@link BundleListClassLoader}.
 * 
 * @since 3.8.2
 */
public class BundleObjectSerializerFactory implements IObjectSerializerFactory {

  private BundleListClassLoader m_bundleBasedClassLoader;

  public BundleObjectSerializerFactory() {
    String[] bundleOrderPrefixes = SerializationUtility.getBundleOrderPrefixes();
    Bundle[] orderedBundleLists = BundleInspector.getOrderedBundleList(bundleOrderPrefixes);
    m_bundleBasedClassLoader = new BundleListClassLoader(orderedBundleLists);
  }

  @Override
  public IObjectSerializer createObjectSerializer(IObjectReplacer objectReplacer) {
    return new BundleObjectSerializer(objectReplacer, m_bundleBasedClassLoader);
  }

  @Override
  public ClassLoader getClassLoader() {
    return m_bundleBasedClassLoader;
  }
}
