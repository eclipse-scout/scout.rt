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
package org.eclipse.scout.service.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.service.CreateServiceImmediatelySchedulingRule;

public class ExtensionPointTracker {
  public interface Listener {
    /**
     * This method is synchronized and therefore thread safe
     */
    void added(IExtension extension);

    /**
     * This method is synchronized and therefore thread safe
     */
    void removed(IExtension extension);
  }

  private static final Listener NULL_LISTENER = new Listener() {
    @Override
    public void added(IExtension extension) {
    }

    @Override
    public void removed(IExtension extension) {
    }
  };

  private final IExtensionRegistry m_registry;
  private final String m_extensionPointId;
  private final String m_namespace;
  private final String m_simpleName;
  private final Set<IExtension> m_extensionCache = new HashSet<IExtension>();
  private final Listener m_listener;
  private final RegistryChangeListener m_registryChangeListener = new RegistryChangeListener();
  private final Object m_trackerLock = new Object();
  private boolean m_open = false;

  public ExtensionPointTracker(IExtensionRegistry registry, String extensionPointId, Listener listener) {
    m_registry = registry;
    m_extensionPointId = extensionPointId;
    m_listener = (listener != null) ? listener : NULL_LISTENER;
    if (extensionPointId == null || -1 == extensionPointId.indexOf('.')) throw new IllegalArgumentException("Unexpected Extension Point Identifier: " + extensionPointId); //$NON-NLS-1$
    int lastDotIndex = extensionPointId.lastIndexOf('.');
    m_namespace = extensionPointId.substring(0, lastDotIndex);
    m_simpleName = extensionPointId.substring(lastDotIndex + 1);
  }

  public final Object getTrackerLock() {
    return m_trackerLock;
  }

  public boolean isOpen() {
    return m_open;
  }

  public void open() {
    // unsafe check (performance)
    if (m_open) {
      return;
    }
    synchronized (m_trackerLock) {
      // safe check
      if (m_open) {
        return;
      }
      IExtension[] extensions = null;
      m_registry.addRegistryChangeListener(m_registryChangeListener, m_namespace);
      try {
        IExtensionPoint point = m_registry.getExtensionPoint(m_extensionPointId);
        if (point != null) {
          extensions = point.getExtensions();
          m_extensionCache.addAll(Arrays.asList(extensions));
        }
      }
      catch (InvalidRegistryObjectException e) {
        m_registry.removeRegistryChangeListener(m_registryChangeListener);
        throw e;
      }
      if (extensions != null) {
        for (int i = 0; i < extensions.length; ++i) {
          m_listener.added(extensions[i]);
        }
      }
      m_open = true;
    }
    //wait for all "create immediately" services
    Job job = new Job("Wait for all 'create immediately' services") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        return Status.OK_STATUS;
      }
    };
    job.setRule(new CreateServiceImmediatelySchedulingRule());
    job.schedule();
    try {
      job.join();
    }
    catch (InterruptedException e) {
      //nop
    }
  }

  public void close() {
    Set<IExtension> extensions = null;
    synchronized (m_trackerLock) {
      if (!m_open) {
        return;
      }
      m_open = false;
      m_registry.removeRegistryChangeListener(m_registryChangeListener);
      extensions = new HashSet<IExtension>(m_extensionCache);
      m_extensionCache.clear();
      for (IExtension x : extensions) {
        m_listener.removed(x);
      }
    }
  }

  private boolean removeExtensionNoLocking(IExtension extension) {
    if (!m_open) {
      return false;
    }
    return m_extensionCache.remove(extension);
  }

  private boolean addExtensionNoLocking(IExtension extension) {
    if (!m_open) {
      return false;
    }
    return m_extensionCache.add(extension);
  }

  public IExtension[] getExtensions() {
    synchronized (m_trackerLock) {
      return m_extensionCache.toArray(new IExtension[m_extensionCache.size()]);
    }
  }

  class RegistryChangeListener implements IRegistryChangeListener {
    @Override
    public void registryChanged(IRegistryChangeEvent event) {
      IExtensionDelta[] deltas = event.getExtensionDeltas(m_namespace, m_simpleName);
      for (int i = 0; i < deltas.length; ++i) {
        IExtensionDelta delta = deltas[i];
        IExtension extension = delta.getExtension();
        switch (delta.getKind()) {
          case IExtensionDelta.ADDED:
            synchronized (m_trackerLock) {
              if (addExtensionNoLocking(extension)) {
                m_listener.added(extension);
              }
            }
            break;
          case IExtensionDelta.REMOVED:
            synchronized (m_trackerLock) {
              if (removeExtensionNoLocking(extension)) {
                m_listener.removed(extension);
              }
            }
          default:
            break;
        }
      }
    }
  }
}
