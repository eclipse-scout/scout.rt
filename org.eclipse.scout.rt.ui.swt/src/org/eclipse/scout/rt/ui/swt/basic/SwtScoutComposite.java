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
package org.eclipse.scout.rt.ui.swt.basic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.form.fields.ISwtScoutFormField;
import org.eclipse.scout.rt.ui.swt.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swt.keystroke.ISwtKeyStrokeFilter;
import org.eclipse.scout.rt.ui.swt.util.AbstractShellPackHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * <h3>SwtScoutComposite</h3> ...
 * 
 * @since 1.0.0 28.03.2008
 */
public abstract class SwtScoutComposite<T extends IPropertyObserver> implements ISwtScoutComposite<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutComposite.class);

  protected static final String CLIENT_PROP_INITIAL_OPAQUE = "scoutInitialOpaque";
  protected static final String CLIENT_PROP_INITIAL_FONT = "scoutInitialFont";
  protected static final String CLIENT_PROP_INITIAL_BACKGROUND = "scoutInitialBackground";
  protected static final String CLIENT_PROP_INITIAL_FOREGROUND = "scoutInitialForeground";

  private Composite m_swtContainer;
  private Control m_swtField;
  private final OptimisticLock m_updateSwtFromScoutLock;
  private final Set<String> m_ignoredScoutEvents;
  private T m_scoutObject;
  private P_ScoutPropertyChangeListener m_scoutPropertyListener;
  private boolean m_connectedToScout;

  private ISwtEnvironment m_environment;
  private boolean m_initialized;

  public SwtScoutComposite() {
    m_updateSwtFromScoutLock = new OptimisticLock();
    m_ignoredScoutEvents = new HashSet<String>();
  }

  public static void registerCompositeOnWidget(Widget comp, ISwtScoutComposite ui) {
    if (comp != null) {
      comp.setData(PROP_SWT_SCOUT_COMPOSITE, new WeakReference<ISwtScoutComposite>(ui));
    }
  }

  /**
   * @return the swt-scout composite used by this widget or null
   */
  @SuppressWarnings("unchecked")
  public static ISwtScoutComposite getCompositeOnWidget(Widget comp) {
    if (comp instanceof Widget) {
      WeakReference<ISwtScoutComposite> ref = (WeakReference<ISwtScoutComposite>) comp.getData(PROP_SWT_SCOUT_COMPOSITE);
      return ref != null ? ref.get() : null;
    }
    else {
      return null;
    }
  }

  /**
   * @return the scout model used by this widget or null if this {@link Widget} has not client property with a scout
   *         model
   *         reference.
   */
  public static IPropertyObserver getScoutModelOnWidget(Widget comp) {
    ISwtScoutComposite ui = getCompositeOnWidget(comp);
    if (ui != null) {
      return ui.getScoutObject();
    }
    else {
      return null;
    }
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

  public void createField(Composite parent, T scoutObject, ISwtEnvironment environment) {
    m_scoutObject = scoutObject;
    m_environment = environment;
    callInitializers(parent);
  }

  protected final void callInitializers(Composite parent) {
    if (m_initialized) {
      return;
    }
    else {
      m_initialized = true;
      //
      try {
        initializeSwt(parent);
        if (getSwtContainer() != null) {
          if (getCompositeOnWidget(getSwtContainer()) == null) {
            registerCompositeOnWidget(getSwtContainer(), this);
          }
        }
        if (getSwtField() != null) {
          if (getCompositeOnWidget(getSwtField()) == null) {
            registerCompositeOnWidget(getSwtField(), this);
          }
        }
        connectToScout();
      }
      catch (Exception e) {
        m_initialized = false;
        LOG.error("could not initialize component '" + getScoutObject().getClass().getName() + "' to '" + this.getClass().getName() + "'.", e);
      }
    }
  }

  public boolean isInitialized() {
    return m_initialized;
  }

  public Composite getSwtContainer() {
    return m_swtContainer;
  }

  protected void setSwtContainer(Composite swtContainer) {
    m_swtContainer = swtContainer;
    if (m_swtContainer != null) {
      P_SwtContainerListener listener = new P_SwtContainerListener();
      m_swtContainer.addListener(SWT.Dispose, listener);
    }
  }

  /**
   * override this method to init the swt components and attach swt listeners
   * expects to invoke {@link #setSwtField(Control)} and/or {@link #setSwtContainer(Composite)}
   */
  protected void initializeSwt(Composite parent) {
  }

  public Control getSwtField() {
    return m_swtField;
  }

  protected void setSwtField(Control swtField) {
    m_swtField = swtField;
    if (m_swtField != null) {
      // the layout data check is used to pack a tree within a composite.
      if (getScoutObject() instanceof IFormField && m_swtField.getLayoutData() == null) {
        m_swtField.setLayoutData(LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData()));
      }

      // on CR validate input first
      getEnvironment().addKeyStrokeFilter(swtField, new ISwtKeyStrokeFilter() {
        public boolean accept(Event e, ISwtEnvironment environment) {
          if (getEnvironment() != null && getEnvironment().equals(environment)) {
            return filterKeyEvent(e);
          }
          return false;
        }
      });
      P_SwtFieldListener listener = new P_SwtFieldListener();
      m_swtField.addListener(SWT.FocusIn, listener);
      m_swtField.addListener(SWT.FocusOut, listener);
      m_swtField.addListener(SWT.Traverse, listener);
      m_swtField.addListener(SWT.Verify, listener);
      m_swtField.addListener(SWT.Dispose, listener);
    }
  }

  @Override
  public boolean isDisposed() {
    return getSwtContainer() == null || getSwtContainer().isDisposed();
  }

  protected boolean filterKeyEvent(Event e) {
    if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
      return handleSwtInputVerifier();
    }
    return true;
  }

  public T getScoutObject() {
    return m_scoutObject;
  }

  protected void setLayoutDirty() {
    AbstractShellPackHandler handler = getShellPackHandlerReq(getSwtContainer());
    if (handler != null) {
      handler.enqueueSizeCheck();
    }
  }

  private AbstractShellPackHandler getShellPackHandlerReq(Control c) {
    if (c == null) {
      return null;
    }
    AbstractShellPackHandler h = (AbstractShellPackHandler) c.getData(PROP_SHELL_PACK_HANDLER);
    if (h != null) {
      return h;
    }
    else {
      return getShellPackHandlerReq(c.getParent());
    }
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

  public final void dispose() {
    if (getSwtField() != null) {
      getSwtField().dispose();
    }
    if (getSwtContainer() != null) {
      getSwtContainer().dispose();
    }
    disconnectFromScout();
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
   * override this method to attach listeners to scout model and initialize
   * obsever state
   */
  protected void attachScout() {
    if (m_scoutObject != null) {
      if (m_swtContainer != null) {
        m_swtContainer.setData(ISwtScoutFormField.CLIENT_PROPERTY_SCOUT_OBJECT, m_scoutObject);
      }
      if (m_scoutPropertyListener == null) {
        m_scoutPropertyListener = new P_ScoutPropertyChangeListener();
        m_scoutObject.addPropertyChangeListener(m_scoutPropertyListener);
      }
    }
  }

  /**
   * override this method to remove listeners from scout model
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
   * handler for scout properties (in Swt Thread) Special: swap enabled/editable
   * on textfields because of gray background and copy/paste capability runs in
   * swt thread
   */
  protected void handleScoutPropertyChange(String name, Object newValue) {
  }

  protected void handleSwtFocusGained() {
  }

  protected void handleSwtFocusLost() {
  }

  protected boolean handleSwtInputVerifier() {
    return true;
  }

  protected void handleSwtAddNotify() {
    connectToScout();
  }

  protected void handleSwtRemoveNotify() {
    disconnectFromScout();
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }

  private class P_SwtFieldListener implements Listener {
    private long m_timestamp;

    public void handleEvent(Event event) {
      if (!event.doit) {
        return;
      }
      switch (event.type) {
        case SWT.Verify:
          if (event.text.equals("\t")) {
            event.doit = false;
          }
          break;
        case SWT.Traverse:
          switch (event.keyCode) {
            case SWT.ARROW_DOWN:
            case SWT.ARROW_UP:
            case SWT.ARROW_LEFT:
            case SWT.ARROW_RIGHT:
            case SWT.HOME:
            case SWT.END:
            case SWT.PAGE_DOWN:
            case SWT.PAGE_UP:
            case SWT.ESC:
            case SWT.CR:
              return;
          }
          //XXX needed?
          /*
          if (getSwtInputVerifierLock().isReleased()) {
            m_timestamp = event.time;
            event.doit = handleSwtInputVerifier();
          }
          */
          break;
        case SWT.FocusIn:
          handleSwtFocusGained();
          break;
        case SWT.FocusOut:
          // filter all temporary focus events
          if (getSwtField() != null && getSwtField().getDisplay().getActiveShell() != null
              && getSwtField().getShell() != getSwtField().getDisplay().getActiveShell()) {
            return;
          }
          else {
            if (m_timestamp != event.time) {
              handleSwtInputVerifier();
            }
            handleSwtFocusLost();
          }
          break;
        case SWT.Dispose:
          if (m_swtContainer == null) {
            // only activated when there is no container
            handleSwtRemoveNotify();
          }
          break;
        default:
          break;
      }
    }
  }

  private class P_SwtContainerListener implements Listener {
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.Dispose:
          handleSwtRemoveNotify();
          break;
      }
    }
  }

  protected void debugHandlePropertyChanged(PropertyChangeEvent e) {

  }

  private class P_ScoutPropertyChangeListener implements PropertyChangeListener {
    public void propertyChange(final PropertyChangeEvent e) {
      debugHandlePropertyChanged(e);
      if (isIgnoredScoutEvent(PropertyChangeEvent.class, e.getPropertyName())) {
        return;
      }
      if (isHandleScoutPropertyChange(e.getPropertyName(), e.getNewValue())) {
        Runnable t = new Runnable() {
          @Override
          public void run() {
            if (!isDisposed()) {
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
