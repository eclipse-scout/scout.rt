/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.runtime.ExtensionPointTracker;
import org.eclipse.scout.commons.runtime.ExtensionPointTracker.Listener;
import org.osgi.framework.Bundle;

/**
 * Extension manager base implementation that parses Scout extensions.
 * 
 * @since 3.9.0
 */
public abstract class AbstractExtensionManager implements Listener {

  protected final IScoutLogger LOG = ScoutLogManager.getLogger(getClass());

  private final String m_extensionPointId;
  private final ExtensionPointTracker m_tracker;
  private final Map<IExtension, Set<Object>> m_extensionContributionsMap;
  private final Map<String, IExtensionProcessor<?>> m_extensionProcessors;

  public AbstractExtensionManager(IExtensionRegistry registry, String extensionPointId) {
    if (registry == null) {
      throw new IllegalArgumentException("registry must not be null");
    }
    if (!StringUtility.hasText(extensionPointId) || registry.getExtensionPoint(extensionPointId) == null) {
      throw new IllegalArgumentException("extension point does not exist [" + extensionPointId + "]");
    }
    m_extensionPointId = extensionPointId;
    m_tracker = new ExtensionPointTracker(registry, extensionPointId, this);
    m_extensionContributionsMap = new HashMap<IExtension, Set<Object>>();
    m_extensionProcessors = new HashMap<String, IExtensionProcessor<?>>();
  }

  public void start() {
    m_tracker.open();
  }

  public void stop() {
    m_tracker.close();
  }

  protected Object getLock() {
    return m_tracker.getTrackerLock();
  }

  /**
   * Adds an extension processor for the given configuration element name. Only one processor can be registered for a
   * particular element. The last processor is used if this method is called several times with the same element name.
   * 
   * @param elementName
   * @param processor
   */
  public void addExtensionProcessor(String elementName, IExtensionProcessor<?> processor) {
    m_extensionProcessors.put(elementName, processor);
  }

  /**
   * Removes the extension processor for the given element.
   * 
   * @param elementName
   */
  public void removeExtensionProcessor(String elementName) {
    m_extensionProcessors.remove(elementName);
  }

  @Override
  public void added(IExtension extension) {
    // resolve contribution bundle
    Bundle contributor = Platform.getBundle(extension.getContributor().getName());
    if (contributor == null) {
      LOG.info("conributor bundle not found for id '" + extension.getContributor().getName() + "'");
      return;
    }
    // process contributions
    Set<Object> contributions = new HashSet<Object>();
    for (IConfigurationElement element : extension.getConfigurationElements()) {
      try {
        // check whether contribution is active
        String active = element.getAttribute("active");
        if (active != null && !TypeCastUtility.castValue(active, boolean.class)) {
          if (LOG.isInfoEnabled()) {
            LOG.info("ignoring inactive extension " + element.getName());
          }
          continue;
        }

        IExtensionProcessor<?> processor = m_extensionProcessors.get(element.getName());
        if (processor == null) {
          // unknown type
          LOG.warn("unsupported element [" + element.getName() + "]");
          continue;
        }

        Object contribution = processor.processConfigurationElement(contributor, element);
        if (contribution != null) {
          contributions.add(contribution);
        }
      }
      catch (Exception e) {
        LOG.error("could not load extension [" + m_extensionPointId + " / " + element.getName() + "] for " + element + " in " + extension.getContributor(), e);
      }
    }
  }

  @Override
  public void removed(IExtension extension) {
    synchronized (getLock()) {
      Set<Object> contributions = m_extensionContributionsMap.remove(extension);
      if (contributions != null && !contributions.isEmpty()) {
        removeContributions(contributions);
      }
    }
  }

  /**
   * Adds the given contributions for the given extensions.
   * 
   * @param extension
   * @param contributions
   */
  protected void addContributions(IExtension extension, Set<Object> contributions) {
    if (contributions.isEmpty()) {
      return;
    }
    m_extensionContributionsMap.put(extension, contributions);
  }

  /**
   * Callback method subclasses are required to remove any cached data for the given contributions.
   * 
   * @param contributions
   */
  protected abstract void removeContributions(Set<Object> contributions);

  /**
   * Loads a class form the given bundle and optionally checks, if it is instance of expected type.
   * 
   * @param bundle
   *          the bundle that loads the class.
   * @param type
   *          the expected type or <code>null</code>, if no instance of check should be performed.
   * @param classname
   *          the fully qualified name of the class to load.
   * @return Returns <code>null</code> if the given bundle or class name is <code>null</code> or the class object of the
   *         requested class.
   * @throws ClassNotFoundException
   *           if the class cannot be loaded by the given bundle
   * @throws ProcessingException
   *           if the loaded class is not an instance of the given expected type.
   */
  public static <T> Class<? extends T> loadClass(Bundle bundle, Class<T> type, String classname) throws ProcessingException, ClassNotFoundException {
    if (bundle == null || !StringUtility.hasText(classname)) {
      return null;
    }
    @SuppressWarnings("unchecked")
    Class<T> cl = (Class<T>) bundle.loadClass(classname);
    if (type != null && !type.isAssignableFrom(cl)) {
      throw new ProcessingException("class [" + classname + "] is not instance of [" + type.getName() + "]");
    }
    return cl;
  }
}
