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
import java.util.Set;

import org.eclipse.rap.rwt.internal.lifecycle.UITestUtil;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.LogicalGridData;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.keystroke.IRwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.keystroke.RwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.testing.CustomWidgetIdGenerator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * @since 3.8.0
 */
@SuppressWarnings("restriction")
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
  public final void createUiField(Composite parent, T scoutObject, IRwtEnvironment uiEnvironment) {
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
      if (getScoutObject() == null) {
        LOG.error("Could not initialize rwt scout composite " + this.getClass().getName() + " because scout object is null.");
      }
      else {
        LOG.error("Could not initialize component '" + getScoutObject().getClass().getName() + "' to '" + this.getClass().getName() + "'.", e);
      }
    }
    finally {
      m_created = true;
    }
  }

  private void setCustomWidgetIds(Widget widget) {
    if (!UITestUtil.isEnabled()) {
      return;
    }

    CustomWidgetIdGenerator.getInstance().setCustomWidgetIds(widget, getScoutObject(), getClass().getName());
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
      P_RwtContainerDisposeListener listener = new P_RwtContainerDisposeListener();
      m_uiContainer.addListener(SWT.Dispose, listener);

      setCustomWidgetIds(m_uiContainer);
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
      if (isAutoSetLayoutData() && getScoutObject() instanceof IFormField && m_uiField.getLayoutData() == null) {
        m_uiField.setLayoutData(LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData()));
      }

      // on CR validate input first
      for (IRwtKeyStroke stroke : getUiKeyStrokes()) {
        getUiEnvironment().addKeyStroke(uiField, stroke, true);
      }
      P_RwtFieldDisposeListener listener = new P_RwtFieldDisposeListener();
      m_uiField.addListener(SWT.Dispose, listener);

      setCustomWidgetIds(m_uiField);
    }
  }

  /**
   * Attaches a focus listener on the given field in order to call {@link #handleUiFocusGained()} respectively
   * {@link #handleUiFocusLost()}. Also calls {@link #handleUiInputVerifier(boolean)} on focus out if callInputVerifier
   * is set to true.
   */
  protected void attachFocusListener(Control field, boolean callInputVerifier) {
    P_RwtFieldFocusListener listener = new P_RwtFieldFocusListener(callInputVerifier);
    field.addListener(SWT.FocusIn, listener);
    field.addListener(SWT.FocusOut, listener);
  }

  /**
   * If true is returned, it will automatically set the {@link LogicalGridData} on the {@link #getUiField()} if no
   * layout data is set and the scout object is a {@link IFormField}.
   * <p>
   * Default is true.
   *
   * @return
   */
  protected boolean isAutoSetLayoutData() {
    return true;
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
    return new IRwtKeyStroke[]{new RwtKeyStroke(SWT.CR) {
      @Override
      public void handleUiAction(Event e) {
        handleUiInputVerifier(e.doit);
      }
    }};
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
   * Attaches the {@link P_ScoutPropertyChangeListener} which calls {@link #handleScoutPropertyChange(String, Object)}.
   * <p>
   * Override this method to set scout model properties on ui components or to attach other model listeners. Always call
   * super.attachScout() at the very beginning to make sure the property change listener gets attached properly.
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

  /**
   * @see #attachFocusListener(Control, boolean)
   */
  protected void handleUiFocusGained() {
  }

  /**
   * @see #attachFocusListener(Control, boolean)
   */
  protected void handleUiFocusLost() {
  }

  /**
   * @see #attachFocusListener(Control, boolean)
   */
  protected void handleUiInputVerifier(boolean doit) {
    //do nothing
  }

  /**
   * Forces UI Input to be verified.
   */
  public void runUiInputVerifier() {
    handleUiInputVerifier(true);
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

  private class P_RwtFieldFocusListener implements Listener {
    private static final long serialVersionUID = 1L;
    private boolean m_callInputVerifier;

    public P_RwtFieldFocusListener(boolean callInputVerifier) {
      m_callInputVerifier = callInputVerifier;
    }

    @Override
    public void handleEvent(Event event) {
      if (!event.doit) {
        return;
      }
      switch (event.type) {
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
            if (m_callInputVerifier) {
              handleUiInputVerifier(true);
            }
            handleUiFocusLost();
          }
          break;
        default:
          break;
      }
    }
  }

  private class P_RwtFieldDisposeListener implements Listener {
    private static final long serialVersionUID = 1L;

    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.Dispose:
          if (m_uiContainer == null) {
            // only activated when there is no container
            handleUiDispose();
          }
          break;
      }
    }
  }

  private class P_RwtContainerDisposeListener implements Listener {
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
