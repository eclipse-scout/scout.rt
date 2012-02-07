/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.basic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.core.IRwtCoreEnvironment;
import org.eclipse.scout.rt.ui.rap.core.basic.IRwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.keystroke.IRwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.keystroke.RwtKeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * <h3>RwtScoutComposite</h3> ...
 * 
 * @since 3.7.0 June 2011
 */
public abstract class RwtScoutComposite<T extends IPropertyObserver> implements IRwtScoutComposite<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutComposite.class);

  protected static final String CLIENT_PROP_INITIAL_OPAQUE = "scoutInitialOpaque";
  protected static final String CLIENT_PROP_INITIAL_FONT = "scoutInitialFont";
  protected static final String CLIENT_PROP_INITIAL_BACKGROUND = "scoutInitialBackground";
  protected static final String CLIENT_PROP_INITIAL_FOREGROUND = "scoutInitialForeground";

  private Composite m_uiContainer;
  private Control m_uiField;
  private final OptimisticLock m_updateUiFromScoutLock;
  private final Set<String> m_ignoredScoutEvents;
  private T m_scoutObject;
  private P_ScoutPropertyChangeListener m_scoutPropertyListener;

  private IRwtEnvironment m_uiEnvironment;
  private boolean m_created;

  public RwtScoutComposite() {
    m_updateUiFromScoutLock = new OptimisticLock();
    m_ignoredScoutEvents = new HashSet<String>();
  }

  public static void registerCompositeOnWidget(Widget comp, IRwtScoutComposite ui) {
    if (comp != null) {
      comp.setData(PROP_RWT_SCOUT_COMPOSITE, new WeakReference<IRwtScoutComposite>(ui));
    }
  }

  /**
   * @return the rwt-scout composite used by this widget or null
   */
  @SuppressWarnings("unchecked")
  public static IRwtScoutComposite getCompositeOnWidget(Widget comp) {
    if (comp instanceof Widget) {
      WeakReference<IRwtScoutComposite> ref = (WeakReference<IRwtScoutComposite>) comp.getData(PROP_RWT_SCOUT_COMPOSITE);
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
    IRwtScoutComposite ui = getCompositeOnWidget(comp);
    if (ui != null) {
      return ui.getScoutObject();
    }
    else {
      return null;
    }
  }

  /**
   * @return the lock used in the Rwt thread when applying scout changes
   */
  public OptimisticLock getUpdateUiFromScoutLock() {
    return m_updateUiFromScoutLock;
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

  @Override
  public final void createUiField(Composite parent, T scoutObject, IRwtCoreEnvironment uiEnvironment) {
    if (m_created) {
      return;
    }
    m_scoutObject = scoutObject;
    m_uiEnvironment = (IRwtEnvironment) uiEnvironment;
    //
    try {
      initializeUi(parent);
      if (getUiContainer() != null) {
        if (getCompositeOnWidget(getUiContainer()) == null) {
          registerCompositeOnWidget(getUiContainer(), this);
        }
      }
      if (getUiField() != null) {
        if (getCompositeOnWidget(getUiField()) == null) {
          registerCompositeOnWidget(getUiField(), this);
        }
      }
      try {
        getUpdateUiFromScoutLock().acquire();
        //
        attachScout();
      }
      finally {
        getUpdateUiFromScoutLock().release();
      }
    }
    catch (Exception e) {
      LOG.error("could not initialize component '" + getScoutObject().getClass().getName() + "' to '" + this.getClass().getName() + "'.", e);
    }
    finally {
      m_created = true;
    }
  }

  @Override
  public boolean isCreated() {
    return m_created;
  }

  @Override
  public Composite getUiContainer() {
    return m_uiContainer;
  }

  protected void setUiContainer(Composite uiContainer) {
    m_uiContainer = uiContainer;
    if (m_uiContainer != null) {
      P_RwtContainerListener listener = new P_RwtContainerListener();
      m_uiContainer.addListener(SWT.Dispose, listener);
    }
  }

  /**
   * override this method to init the rwt components and attach rwt listeners
   * expects to invoke {@link #setUiField(Control)} and/or {@link #setUiContainer(Composite)}
   * <p>
   * TODO imo refactor to createFieldImpl
   */
  protected void initializeUi(Composite parent) {
  }

  @Override
  public Control getUiField() {
    return m_uiField;
  }

  protected void setUiField(Control uiField) {
    m_uiField = uiField;
    if (m_uiField != null) {
      // the layout data check is used to pack a tree within a composite.
      if (getScoutObject() instanceof IFormField && m_uiField.getLayoutData() == null) {
        m_uiField.setLayoutData(LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData()));
      }

      // on CR validate input first
      for (IRwtKeyStroke stroke : getUiKeyStrokes()) {
        getUiEnvironment().addKeyStroke(uiField, stroke, true);
      }
      P_RwtFieldListener listener = new P_RwtFieldListener();
      m_uiField.addListener(SWT.FocusIn, listener);
      m_uiField.addListener(SWT.FocusOut, listener);
      m_uiField.addListener(SWT.Traverse, listener);
      m_uiField.addListener(SWT.Verify, listener);
      m_uiField.addListener(SWT.Dispose, listener);
    }
  }

  @Override
  public boolean isUiDisposed() {
    if (getUiContainer() == null) {
      // try field
      return getUiField() == null || getUiField().isDisposed();
    }
    else {
      return getUiContainer().isDisposed();
    }
  }

  protected IRwtKeyStroke[] getUiKeyStrokes() {
    List<IRwtKeyStroke> strokes = null;

    strokes = CollectionUtility.appendList(strokes, new RwtKeyStroke(SWT.CR) {
      @Override
      public void handleUiAction(Event e) {
        handleUiInputVerifier(e.doit);
      }
    });

    return CollectionUtility.toArray(strokes, IRwtKeyStroke.class);
  }

  @Override
  public T getScoutObject() {
    return m_scoutObject;
  }

  @Override
  public final void dispose() {
    if (!m_created) {
      return;
    }
    if (getUiField() != null && !getUiField().isDisposed()) {
      getUiField().dispose();
    }
    if (getUiContainer() != null && !getUiContainer().isDisposed()) {
      getUiContainer().dispose();
    }
    disposeImpl();
  }

  /**
   * override this method to do additional operations when the composite is being disposed
   */
  protected void disposeImpl() {
  }

  /**
   * override this method to attach listeners to scout model and initialize
   * obsever state
   * <p>
   */
  protected void attachScout() {
    if (m_scoutObject != null) {
      if (m_uiContainer != null) {
        m_uiContainer.setData(IRwtScoutFormField.CLIENT_PROPERTY_SCOUT_OBJECT, m_scoutObject);
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
   * handleScoutPropertyChange is queued to the rwt thread runs in scout thread
   */
  protected boolean isHandleScoutPropertyChange(String name, Object newValue) {
    return true;
  }

  /**
   * handler for scout properties (in Rwt Thread) Special: swap enabled/editable
   * on textfields because of gray background and copy/paste capability runs in
   * rwt thread
   */
  protected void handleScoutPropertyChange(String name, Object newValue) {
  }

  protected void handleUiFocusGained() {
  }

  protected void handleUiFocusLost() {
  }

  protected void handleUiInputVerifier(boolean doit) {
    //do nothing
  }

  protected void handleUiDispose() {
    if (!m_created) {
      return;
    }
    try {
      getUpdateUiFromScoutLock().acquire();
      // remove possibly registered key strokes
      getUiEnvironment().removeKeyStrokes(getUiField());
      getUiEnvironment().removeKeyStrokes(getUiContainer());
      //
      detachScout();
    }
    finally {
      m_created = false;
      getUpdateUiFromScoutLock().release();
    }
  }

  @Override
  public IRwtEnvironment getUiEnvironment() {
    return m_uiEnvironment;
  }

  private class P_RwtFieldListener implements Listener {
    private static final long serialVersionUID = 1L;
    private long m_timestamp;

    @Override
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
          // guarantee the value be written back to the model
          m_timestamp = event.time;
          handleUiInputVerifier(event.doit);
          break;
        case SWT.FocusIn:
          handleUiFocusGained();
          break;
        case SWT.FocusOut:
          // filter all temporary focus events
          if (getUiField() != null && getUiField().getDisplay().getActiveShell() != null
              && getUiField().getShell() != getUiField().getDisplay().getActiveShell()) {
            return;
          }
          else {
            handleUiInputVerifier(true);
            handleUiFocusLost();
          }
          break;
        case SWT.Dispose:
          if (m_uiContainer == null) {
            // only activated when there is no container
            handleUiDispose();
          }
          break;
        default:
          break;
      }
    }
  }

  private class P_RwtContainerListener implements Listener {
    private static final long serialVersionUID = 1L;

    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.Dispose:
          handleUiDispose();
          break;
      }
    }
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
      if (isHandleScoutPropertyChange(e.getPropertyName(), e.getNewValue())
          && getUiEnvironment().getDisplay() != null && !getUiEnvironment().getDisplay().isDisposed()) {
        Runnable t = new Runnable() {
          @Override
          public void run() {
            if (!isUiDisposed()) {
              try {
                getUpdateUiFromScoutLock().acquire();
                //
                handleScoutPropertyChange(e.getPropertyName(), e.getNewValue());
              }
              finally {
                getUpdateUiFromScoutLock().release();
              }
            }
          }
        };
        getUiEnvironment().invokeUiLater(t);
      }
    }
  }// end private class
}
