/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.basic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;

/**
 * Root class for rendering elements from the Scout model.
 * 
 * @since 3.10.0-M5
 */
public abstract class AbstractSwtScoutPropertyObserver<T extends IPropertyObserver> implements ISwtScoutPropertyObserver<T> {

  private final OptimisticLock m_updateSwtFromScoutLock;
  private final Set<String> m_ignoredScoutEvents;

  private ISwtEnvironment m_environment;
  private T m_scoutObject;

  private P_ScoutPropertyChangeListener m_scoutPropertyListener;
  private boolean m_connectedToScout;

  public AbstractSwtScoutPropertyObserver() {
    m_updateSwtFromScoutLock = new OptimisticLock();
    m_ignoredScoutEvents = new HashSet<String>();
  }

  protected void setScoutObjectAndSwtEnvironment(T scoutObject, ISwtEnvironment environment) {
    m_scoutObject = scoutObject;
    m_environment = environment;
  }

  /**
   * @return the lock used in the Swt thread when applying scout changes
   */
  public OptimisticLock getUpdateSwtFromScoutLock() {
    return m_updateSwtFromScoutLock;
  }

  /**
   * add an event description that, when scout sends it, is ignored
   */
  public void addIgnoredScoutEvent(Class eventType, String name) {
    m_ignoredScoutEvents.add(eventType.getSimpleName() + ":" + name);
  }

  /**
   * remove an event description so that when scout sends it, it is processed
   */
  public void removeIgnoredScoutEvent(Class eventType, String name) {
    m_ignoredScoutEvents.remove(eventType.getSimpleName() + ":" + name);
  }

  @Override
  public T getScoutObject() {
    return m_scoutObject;
  }

  @Override
  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }

  public boolean isConnectedToScout() {
    return m_connectedToScout;
  }

  protected final void connectToScout() {
    if (!m_connectedToScout) {
      try {
        getUpdateSwtFromScoutLock().acquire();
        //
        attachScout();
        applyScoutProperties();
        applyScoutState();
        m_connectedToScout = true;
      }
      finally {
        getUpdateSwtFromScoutLock().release();
      }
    }
  }

  protected final void disconnectFromScout() {
    if (m_connectedToScout) {
      try {
        getUpdateSwtFromScoutLock().acquire();
        //
        detachScout();
        m_connectedToScout = false;
      }
      finally {
        getUpdateSwtFromScoutLock().release();
      }
    }
  }

  /**
   * Attaches the {@link P_ScoutPropertyChangeListener} which calls {@link #handleScoutPropertyChange(String, Object)}.
   * <p>
   * Override this method to set scout model properties on ui components or to attach other model listeners. Always call
   * super.attachScout() at the very beginning to make sure the property change listener gets attached properly.
   */
  protected void attachScout() {
    if (m_scoutObject != null) {
      if (m_scoutPropertyListener == null) {
        m_scoutPropertyListener = new P_ScoutPropertyChangeListener();
        m_scoutObject.addPropertyChangeListener(m_scoutPropertyListener);
      }
    }
  }

  /**
   * Override this method to remove listeners from scout model.
   */
  protected void detachScout() {
    if (m_scoutObject != null) {
      if (m_scoutPropertyListener != null) {
        m_scoutObject.removePropertyChangeListener(m_scoutPropertyListener);
        m_scoutPropertyListener = null;
      }
    }
  }

  /**
   * pre-processor for scout properties (in Scout Thread) decision whether a
   * handleScoutPropertyChange is queued to the swt thread runs in scout thread
   */
  protected boolean isHandleScoutPropertyChange(String name, Object newValue) {
    return true;
  }

  /**
   * pre-processor for scout properties (in SWT Thread) decision whether a
   * handleScoutPropertyChange will be handled in the swt thread.
   */
  protected boolean isHandleScoutPropertyChangeSwtThread() {
    return true;
  }

  /**
   * handler for scout properties (in Swt Thread) Special: swap enabled/editable
   * on textfields because of gray background and copy/paste capability runs in
   * swt thread
   */
  protected void handleScoutPropertyChange(String name, Object newValue) {
  }

  /**
   * override this method to set scout properties on swt components
   */
  protected void applyScoutProperties() {
  }

  /**
   * override this method to set scout model state on swt components
   */
  protected void applyScoutState() {
  }

  /**
   * @return true if that scout event is ignored
   */
  public boolean isIgnoredScoutEvent(Class eventType, String name) {
    if (m_ignoredScoutEvents.isEmpty()) {
      return false;
    }
    boolean b = m_ignoredScoutEvents.contains(eventType.getSimpleName() + ":" + name);
    return b;
  }

  protected void debugHandlePropertyChanged(PropertyChangeEvent e) {

  }

  private class P_ScoutPropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(final PropertyChangeEvent e) {
      debugHandlePropertyChanged(e);
      if (isIgnoredScoutEvent(PropertyChangeEvent.class, e.getPropertyName())) {
        return;
      }
      if (isHandleScoutPropertyChange(e.getPropertyName(), e.getNewValue())) {
        Runnable t = new Runnable() {
          @Override
          public void run() {
            if (isHandleScoutPropertyChangeSwtThread()) {
              try {
                getUpdateSwtFromScoutLock().acquire();
                //
                handleScoutPropertyChange(e.getPropertyName(), e.getNewValue());
              }
              finally {
                getUpdateSwtFromScoutLock().release();
              }
            }
          }
        };
        m_environment.invokeSwtLater(t);
      }
    }
  }// end private class
}
