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

import org.eclipse.scout.commons.internal.Activator;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.osgi.BundleInspector;
import org.eclipse.scout.commons.osgi.BundleListClassLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Factory for creating {@link BundleObjectSerializer} instances. All of them are sharing the very same
 * {@link BundleListClassLoader}.
 * 
 * @since 3.8.2
 */
public class BundleObjectSerializerFactory implements IObjectSerializerFactory {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BundleObjectSerializerFactory.class);
  private static final String CONTEXT_FINDER_CLASS_NAME = "org.eclipse.core.runtime.internal.adaptor.ContextFinder";

  private BundleListClassLoader m_bundleBasedClassLoader;

  public BundleObjectSerializerFactory() {
    String[] bundleOrderPrefixes = SerializationUtility.getBundleOrderPrefixes();
    Bundle[] orderedBundleLists = BundleInspector.getOrderedBundleList(bundleOrderPrefixes);
    ClassLoader parent = getOsgiParentClassLoader();
    m_bundleBasedClassLoader = new BundleListClassLoader(parent, orderedBundleLists);
  }

  @Override
  public IObjectSerializer createObjectSerializer(IObjectReplacer objectReplacer) {
    return new BundleObjectSerializer(objectReplacer, m_bundleBasedClassLoader);
  }

  @Override
  public ClassLoader getClassLoader() {
    return m_bundleBasedClassLoader;
  }

  protected ClassLoader getOsgiParentClassLoader() {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    if (contextClassLoader != null && CONTEXT_FINDER_CLASS_NAME.equals(contextClassLoader.getClass().getName())) {
      return contextClassLoader.getParent();
    }

    // find OSGi parent classloader by service
    ClassLoader osgiContextClassLoader = null;
    try {
      BundleContext context = Activator.getDefault().getBundle().getBundleContext();
      for (ServiceReference ref : context.getServiceReferences(ClassLoader.class.getName(), null)) {
        if ("contextClassLoader".equals(ref.getProperty("equinox.classloader.type"))) {
          try {
            osgiContextClassLoader = (ClassLoader) context.getService(ref);
            break;
          }
          finally {
            context.ungetService(ref);
          }
        }
      }
    }
    catch (Throwable t) {
      LOG.error("cannot determine OSGi context class loader", t);
    }
    if (osgiContextClassLoader == null) {
      return null;
    }
    if (CONTEXT_FINDER_CLASS_NAME.equals(osgiContextClassLoader.getClass().getName())) {
      return osgiContextClassLoader.getParent();
    }
    return osgiContextClassLoader;
  }
}
