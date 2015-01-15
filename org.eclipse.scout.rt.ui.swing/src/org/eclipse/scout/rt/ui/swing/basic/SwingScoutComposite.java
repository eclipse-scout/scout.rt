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
package org.eclipse.scout.rt.ui.swing.basic;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import javax.swing.InputVerifier;
import javax.swing.JComponent;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.WeakEventListener;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.profiler.DesktopProfiler;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.action.SwingScoutAction;
import org.eclipse.scout.rt.ui.swing.form.fields.LogicalGridDataBuilder;

public abstract class SwingScoutComposite<T extends IPropertyObserver> implements ISwingScoutComposite<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutComposite.class);

  public static final String CLIENT_PROP_INITIAL_OPAQUE = "scoutInitialOpaque";
  public static final String CLIENT_PROP_INITIAL_FONT = "scoutInitialFont";
  public static final String CLIENT_PROP_INITIAL_BACKGROUND = "scoutInitialBackground";
  public static final String CLIENT_PROP_INITIAL_FOREGROUND = "scoutInitialForeground";

  private static final Object NULL_VALUE = new Object();
  private static final ThreadLocal<OptimisticLock> CONNECT_TO_SCOUT_THREAD_OPTIMISTIC_LOCK = new ThreadLocal<OptimisticLock>() {
    @Override
    protected OptimisticLock initialValue() {
      return new OptimisticLock();
    }
  };
  private static final ThreadLocal<OptimisticLock> DISCONNECT_FROM_SCOUT_THREAD_OPTIMISTIC_LOCK = new ThreadLocal<OptimisticLock>() {
    @Override
    protected OptimisticLock initialValue() {
      return new OptimisticLock();
    }
  };

  private ISwingEnvironment m_env;
  private JComponent m_swingField;
  private final OptimisticLock m_updateSwingFromScoutLock;
  private final Set<String> m_ignoredScoutEvents;
  private volatile EventListenerList m_inputVerifyListeners; // initialized lazily on first use.
  private boolean m_inputDirty;
  private T m_scoutObject;
  private P_ScoutPropertyChangeListener m_scoutPropertyListener;
  private P_SwingDefaultFocusListener m_swingDefaultFocusListener;
  private P_SwingInputVerifier m_swingInputVerifier;
  private P_SwingShowingListener m_swingShowingListener;
  private boolean m_initialized;
  private boolean m_connectedToScout;

  public SwingScoutComposite() {
    super();
    if (DesktopProfiler.getInstance().isEnabled()) {
      DesktopProfiler.getInstance().registerObject(this);
    }
    m_updateSwingFromScoutLock = new OptimisticLock();
    m_ignoredScoutEvents = new HashSet<String>();
  }

  /**
   * @return the lock used in the swing thread when applying scout changes
   */
  public OptimisticLock getUpdateSwingFromScoutLock() {
    return m_updateSwingFromScoutLock;
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
  public void addInputVerifyListener(ISwingInputVerifyListener listener) {
    if (m_inputVerifyListeners == null) {
      synchronized (this) {
        if (m_inputVerifyListeners == null) {
          m_inputVerifyListeners = new EventListenerList();
        }
      }
    }
    m_inputVerifyListeners.add(ISwingInputVerifyListener.class, listener);
  }

  @Override
  public void removeInputVerifyListener(ISwingInputVerifyListener listener) {
    if (m_inputVerifyListeners == null) {
      return;
    }
    m_inputVerifyListeners.remove(ISwingInputVerifyListener.class, listener);
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

  @Override
  public void createField(T scoutObject, ISwingEnvironment environment) {
    m_scoutObject = scoutObject;
    m_env = environment;
    callInitializers();
  }

  protected final void callInitializers() {
    if (m_initialized) {
      return;
    }
    else {
      m_initialized = true;
      //
      initializeSwing();
      if (getSwingContainer() != null) {
        if (getCompositeOnWidget(getSwingContainer()) == null) {
          registerCompositeOnWidget(getSwingContainer(), this);
        }
        P_SwingAddRemoveListener listener = new P_SwingAddRemoveListener();
        getSwingContainer().addHierarchyListener(listener);
      }
      if (getSwingField() != null) {
        if (getCompositeOnWidget(getSwingField()) == null) {
          registerCompositeOnWidget(getSwingField(), this);
        }
        P_SwingAddRemoveListener listener = new P_SwingAddRemoveListener();
        getSwingField().addHierarchyListener(listener);
      }
      cacheSwingClientProperties();
      connectToScout();
    }
  }

  @Override
  public ISwingEnvironment getSwingEnvironment() {
    return m_env;
  }

  /**
   * override this method to init the swing components and attach swing
   * listeners expects to invoke setSwingField()
   */
  protected void initializeSwing() {
  }

  protected void cacheSwingClientProperties() {
    JComponent fld = getSwingField();
    if (fld != null) {
      // opaque
      if (!existsClientProperty(fld, CLIENT_PROP_INITIAL_OPAQUE)) {
        putClientProperty(fld, CLIENT_PROP_INITIAL_OPAQUE, new Boolean(fld.isOpaque()));
      }
      // background
      if (!existsClientProperty(fld, CLIENT_PROP_INITIAL_BACKGROUND)) {
        putClientProperty(fld, CLIENT_PROP_INITIAL_BACKGROUND, fld.getBackground());
      }
      // foreground
      if (!existsClientProperty(fld, CLIENT_PROP_INITIAL_FOREGROUND)) {
        putClientProperty(fld, CLIENT_PROP_INITIAL_FOREGROUND, fld.getForeground());
      }
      // font
      if (!existsClientProperty(fld, CLIENT_PROP_INITIAL_FONT)) {
        putClientProperty(fld, CLIENT_PROP_INITIAL_FONT, fld.getFont());
      }
    }
  }

  @Override
  public JComponent getSwingField() {
    return m_swingField;
  }

  @Override
  public JComponent getSwingContainer() {
    return m_swingField;
  }

  protected void setSwingField(JComponent swingField) {
    if (DesktopProfiler.getInstance().isEnabled()) {
      DesktopProfiler.getInstance().registerObject(swingField);
    }
    if (m_swingField == swingField) {
      return;
    }
    //remove old
    if (m_swingField != null) {
      if (m_swingDefaultFocusListener != null) {
        m_swingField.removeFocusListener(m_swingDefaultFocusListener);
        m_swingDefaultFocusListener = null;
      }
      if (m_swingInputVerifier != null) {
        m_swingField.setInputVerifier(null);
        m_swingInputVerifier = null;
      }
      if (m_swingShowingListener != null) {
        m_swingField.removeHierarchyListener(m_swingShowingListener);
        m_swingShowingListener = null;
      }
    }
    //add new
    m_swingField = swingField;
    if (m_swingField != null) {
      if (getScoutObject() instanceof IFormField) {
        m_swingField.putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, LogicalGridDataBuilder.createField(getSwingEnvironment(), ((IFormField) getScoutObject()).getGridData()));
      }
      m_swingField.addFocusListener(m_swingDefaultFocusListener = new P_SwingDefaultFocusListener());
      m_swingField.setInputVerifier(m_swingInputVerifier = new P_SwingInputVerifier());
      m_swingField.addHierarchyListener(m_swingShowingListener = new P_SwingShowingListener());
    }
  }

  @Override
  public T getScoutObject() {
    return m_scoutObject;
  }

  @Override
  public final void connectToScout() {
    if (!m_connectedToScout) {
      //avoid reentrancy
      m_connectedToScout = true;
      //since swing is not reliable on addNotify/removeNotify, recursively delegate connect to all child components with a registered scout model
      OptimisticLock lock = CONNECT_TO_SCOUT_THREAD_OPTIMISTIC_LOCK.get();
      try {
        if (lock.acquire()) {
          Container container = getSwingContainer();
          if (container == null) {
            container = getSwingField();
          }
          if (container != null) {
            for (Component c : SwingUtility.findChildComponents(container, Component.class)) {
              ISwingScoutComposite ui = getCompositeOnWidget(c);
              if (ui != null) {
                try {
                  ui.connectToScout();
                }
                catch (Throwable t) {
                  LOG.error("connectToScout: model=" + m_scoutObject + ", widget=" + c, t);
                }
              }
            }
          }
        }
      }
      finally {
        lock.release();
      }
      //
      try {
        m_updateSwingFromScoutLock.acquire();
        //
        attachScout();
      }
      finally {
        m_updateSwingFromScoutLock.release();
      }
    }
  }

  @Override
  public final void disconnectFromScout() {
    if (m_connectedToScout) {
      //avoid reentrancy
      m_connectedToScout = false;
      //since swing is not reliable on addNotify/removeNotify, recursively delegate connect to all child components with a registered scout model
      OptimisticLock lock = DISCONNECT_FROM_SCOUT_THREAD_OPTIMISTIC_LOCK.get();
      try {
        if (lock.acquire()) {
          Container container = getSwingContainer();
          if (container == null) {
            container = getSwingField();
          }
          if (container != null) {
            for (Component c : SwingUtility.findChildComponents(container, Component.class)) {
              ISwingScoutComposite ui = getCompositeOnWidget(c);
              if (ui != null) {
                try {
                  ui.disconnectFromScout();
                }
                catch (Throwable t) {
                  LOG.error("disconnectFromScout: model=" + m_scoutObject + ", widget=" + c, t);
                }
              }
            }
          }
        }
      }
      finally {
        lock.release();
      }
      //
      try {
        m_updateSwingFromScoutLock.acquire();
        //
        detachScout();
      }
      finally {
        m_updateSwingFromScoutLock.release();
      }
    }
  }

  public static void registerCompositeOnWidget(JComponent comp, ISwingScoutComposite ui) {
    if (comp != null) {
      comp.putClientProperty(CLIENT_PROP_SWING_SCOUT_COMPOSITE, new WeakReference<ISwingScoutComposite>(ui));
    }
  }

  /**
   * @return the swing-scout composite used by this widget or null
   */
  @SuppressWarnings("unchecked")
  public static ISwingScoutComposite getCompositeOnWidget(Component comp) {
    if (comp instanceof JComponent) {
      WeakReference<ISwingScoutComposite> ref = (WeakReference<ISwingScoutComposite>) ((JComponent) comp).getClientProperty(CLIENT_PROP_SWING_SCOUT_COMPOSITE);
      return ref != null ? ref.get() : null;
    }
    else {
      return null;
    }
  }

  /**
   * @return the scout model used by this widget or null if this {@link JComponent} has not client property with a scout
   *         model
   *         reference.
   */
  public static IPropertyObserver getScoutModelOnWidget(Component comp) {
    ISwingScoutComposite ui = getCompositeOnWidget(comp);
    if (ui != null) {
      return ui.getScoutObject();
    }
    else {
      return null;
    }
  }

  /**
   * override this method to set scout properties on swing components
   */
  protected void attachScout() {
    if (m_scoutObject != null && m_scoutPropertyListener == null) {
      m_scoutPropertyListener = new P_ScoutPropertyChangeListener();
      m_scoutObject.addPropertyChangeListener(m_scoutPropertyListener);
    }
  }

  /**
   * override this method to remove listeners from scout model
   */
  protected void detachScout() {
    if (m_scoutObject != null && m_scoutPropertyListener != null) {
      m_scoutObject.removePropertyChangeListener(m_scoutPropertyListener);
      m_scoutPropertyListener = null;
    }
  }

  /**
   * WORKAROUND swing does not know a EXISTSClientProperty therefore we store a
   * NULL_OBJECT as a null value then a client property exists when its value is
   * non-null
   */
  protected boolean existsClientProperty(JComponent comp, String key) {
    Object value = comp.getClientProperty(key);
    return value != null;
  }

  protected Object getClientProperty(JComponent comp, String key) {
    Object value = comp.getClientProperty(key);
    if (value == NULL_VALUE) {
      value = null;
    }
    return value;
  }

  protected void putClientProperty(JComponent comp, String key, Object value) {
    if (value == null) {
      value = NULL_VALUE;
    }
    comp.putClientProperty(key, value);
  }

  /**
   * pre-processor for scout properties (in Scout Thread) decision whether a
   * handleScoutPropertyChange is queued to the swing thread runs in scout
   * thread
   */
  protected boolean isHandleScoutPropertyChange(String name, Object newValue) {
    return true;
  }

  /**
   * handler for scout properties (in Swing Thread) Special: swap
   * enabled/editable on textfields because of gray background and copy/paste
   * capability runs in swing thread
   * <p>
   * This handler runs inside {@link #getUpdateSwingFromScoutLock()}
   */
  protected void handleScoutPropertyChange(String name, Object newValue) {
  }

  protected void handleSwingFocusGained() {
  }

  protected void handleSwingFocusLost() {
  }

  protected boolean handleSwingInputVerifier() {
    return true;
  }

  protected void handleSwingAddNotify() {
    connectToScout();
  }

  protected void handleSwingRemoveNotify() {
    disconnectFromScout();
  }

  protected void handleSwingShowing() {
  }

  protected void setInputDirty(boolean inputDirty) {
    m_inputDirty = inputDirty;
  }

  protected boolean isInputDirty() {
    return m_inputDirty;
  }

  /*
   * Inner classes
   */
  private class P_SwingDefaultFocusListener extends FocusAdapter {
    @Override
    public void focusGained(FocusEvent f) {
      if (f.isTemporary()) {
        return;
      }
      setInputDirty(true);
      handleSwingFocusGained();
    }

    @Override
    public void focusLost(FocusEvent f) {
      handleSwingFocusLost();
    }

  }// end private class P_SwingDefaultFocusListener

  private final class P_SwingInputVerifier extends InputVerifier {
    private boolean m_lastResult = true;

    @Override
    public boolean verify(JComponent input) {
      if (isInputDirty()) {
        if (getUpdateSwingFromScoutLock().isReleased()) {
          m_lastResult = handleSwingInputVerifier();

          // notify follow-up listeners
          if (m_inputVerifyListeners != null) {
            for (ISwingInputVerifyListener listener : m_inputVerifyListeners.getListeners(ISwingInputVerifyListener.class)) {
              try {
                listener.verify(input);
              }
              catch (Exception t) {
                LOG.error("Error notifying verify listener '" + listener.getClass().getName() + "'.", t);
              }
            }
          }
        }
      }
      if (m_lastResult) {
        setInputDirty(false);
      }
      return m_lastResult;
    }
  }// end private class P_SwingInputVerifier

  private class P_SwingAddRemoveListener implements HierarchyListener {

    @Override
    public void hierarchyChanged(HierarchyEvent e) {
      if (e.getID() == HierarchyEvent.HIERARCHY_CHANGED) {
        if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
          if (e.getComponent().isDisplayable()) {
            handleSwingAddNotify();
          }
          else {
            handleSwingRemoveNotify();
          }
        }
      }
    }
  }// end private class P_SwingAddRemoveListener

  private class P_SwingShowingListener implements HierarchyListener {
    @Override
    public void hierarchyChanged(HierarchyEvent e) {
      if (e.getID() == HierarchyEvent.HIERARCHY_CHANGED) {
        if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
          Component c = (Component) e.getSource();
          boolean b = false;
          try {
            b = c.isShowing();
          }
          catch (Throwable t) {
            // nop
          }
          if (b) {
            handleSwingShowing();
          }
        }
      }
    }
  }// end private class P_SwingShowingListener

  /**
   * This listener must be a {@link WeakEventListener}. Even though all scout
   * composites unregister listeners on swing removeNotify, some composites like {@link SwingScoutAction} have no
   * atatched swing JComponent. These
   * composites depend on the use of this weak listener concept
   */
  private class P_ScoutPropertyChangeListener implements PropertyChangeListener, WeakEventListener {
    @Override
    public void propertyChange(final PropertyChangeEvent e) {
      if (isIgnoredScoutEvent(PropertyChangeEvent.class, e.getPropertyName())) {
        return;
      }
      //
      if (isHandleScoutPropertyChange(e.getPropertyName(), e.getNewValue())) {
        Runnable t = new Runnable() {
          @Override
          public void run() {
            try {
              m_updateSwingFromScoutLock.acquire();
              //
              handleScoutPropertyChange(e.getPropertyName(), e.getNewValue());
            }
            finally {
              m_updateSwingFromScoutLock.release();
            }
          }
        };
        getSwingEnvironment().invokeSwingLater(t);
      }
    }
  }// end private class P_ScoutPropertyChangeListener

}
