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
package org.eclipse.scout.rt.platform.eventlistprofiler;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is Thread safe
 */
public final class EventListenerProfiler {

  private static final Logger LOG = LoggerFactory.getLogger(EventListenerProfiler.class);
  private static final EventListenerProfiler INSTANCE = new EventListenerProfiler();

  public static EventListenerProfiler getInstance() {
    return INSTANCE;
  }

  private boolean m_enabled = false;
  private final Object m_sourcesLock;
  private final List<WeakReference<IEventListenerSource>> m_sources;

  private EventListenerProfiler() {
    m_sourcesLock = new Object();
    m_sources = new ArrayList<>();
  }

  public boolean isEnabled() {
    return m_enabled;
  }

  public void setEnabled(boolean enabled) {
    m_enabled = enabled;
  }

  /**
   * Add a weak reference to a source provider NOTE: the passed argument MUST be referenced by the source type,
   * otherwise it is garbage collected immediately after adding
   */
  public void registerSourceAsWeakReference(IEventListenerSource source) {
    if (!m_enabled) {
      return;
    }
    synchronized (m_sourcesLock) {
      m_sources.add(new WeakReference<>(source));
    }
  }

  public void dump() {
    dump(System.out);
  }

  public void dump(OutputStream o) {
    /**
     * this call to gc is intended
     */
    System.gc(); // NOSONAR
    if (!m_enabled) {
      return;
    }
    try (PrintWriter out = new PrintWriter(o, true)) {
      EventListenerSnapshot snapshot = new EventListenerSnapshot();
      synchronized (m_sourcesLock) {
        manageNoLock();
        NumberFormat fmt = NumberFormat.getIntegerInstance(NlsLocale.get());
        out.println("Max memory:   " + fmt.format(Runtime.getRuntime().maxMemory()));
        out.println("Total memory: " + fmt.format(Runtime.getRuntime().totalMemory()));
        out.println("Free memory:  " + fmt.format(Runtime.getRuntime().freeMemory()));
        out.println("(Used memory):" + fmt.format(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
        out.println();
        for (WeakReference<IEventListenerSource> ref : m_sources) {
          IEventListenerSource p = ref.get();
          if (p != null) {
            p.dumpListenerList(snapshot);
          }
        }
      }
      snapshot.dump(out);
    }
    catch (RuntimeException e) {
      LOG.warn("Could not dump event listener snapshot", e);
    }
  }

  private void manageNoLock() {
    m_sources.removeIf(ref -> ref.get() == null);
  }

}
