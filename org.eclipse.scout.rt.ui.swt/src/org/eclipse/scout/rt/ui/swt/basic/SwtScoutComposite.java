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

import java.lang.ref.WeakReference;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.form.fields.ISwtScoutFormField;
import org.eclipse.scout.rt.ui.swt.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swt.keystroke.ISwtKeyStrokeFilter;
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
 * @author Andreas Hoegger
 */
public abstract class SwtScoutComposite<T extends IPropertyObserver> extends AbstractSwtScoutPropertyObserver<T> implements ISwtScoutComposite<T>, IInputVerifiable {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutComposite.class);

  protected static final String CLIENT_PROP_INITIAL_OPAQUE = "scoutInitialOpaque";
  protected static final String CLIENT_PROP_INITIAL_FONT = "scoutInitialFont";
  protected static final String CLIENT_PROP_INITIAL_BACKGROUND = "scoutInitialBackground";
  protected static final String CLIENT_PROP_INITIAL_FOREGROUND = "scoutInitialForeground";

  private Composite m_swtContainer;
  private Control m_swtField;
  private EventListenerList m_eventListeners = new EventListenerList();

  private boolean m_initialized;

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

  @Override
  public void createField(Composite parent, T scoutObject, ISwtEnvironment environment) {
    setScoutObjectAndSwtEnvironment(scoutObject, environment);
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

  @Override
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

  @Override
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
      getEnvironment().addKeyStrokeFilter(swtField, new ISwtKeyStrokeFilter() {//TODO sle this adds many many instances, in this case better make SwtScoutComposite implements ISwtKeyStrokeFilter
        @Override
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
  protected boolean isHandleScoutPropertyChangeSwtThread() {
    return !isDisposed();
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

  @Override
  protected void attachScout() {
    super.attachScout();
    if (getScoutObject() != null) {
      if (m_swtContainer != null) {
        m_swtContainer.setData(ISwtScoutFormField.CLIENT_PROPERTY_SCOUT_OBJECT, getScoutObject());
      }
    }
  }

  @Override
  public final void dispose() {
    if (getSwtField() != null) {
      getSwtField().dispose();
    }
    if (getSwtContainer() != null) {
      getSwtContainer().dispose();
    }
    disconnectFromScout();
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

  /**
   * Runs the SwtInputVerifier.
   *
   * @since 3.10.0-M5
   */
  @Override
  public boolean runSwtInputVerifier() {
    boolean verified = handleSwtInputVerifier();
    for (IInputVerifyListener l : m_eventListeners.getListeners(IInputVerifyListener.class)) {
      l.inputVerified();
    }
    return verified;

  }

  @Override
  public void addInputVerifyListener(IInputVerifyListener listener) {
    m_eventListeners.add(IInputVerifyListener.class, listener);
  }

  @Override
  public void removeInputVerifyListener(IInputVerifyListener listener) {
    m_eventListeners.remove(IInputVerifyListener.class, listener);
  }

  private class P_SwtFieldListener implements Listener {
    private long m_timestamp;

    @Override
    public void handleEvent(Event event) {
      if (!event.doit) {
        return;
      }
      switch (event.type) {
        case SWT.Verify:
          if ("\t".equals(event.text)) {
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

          m_timestamp = event.time;
          event.doit = handleSwtInputVerifier();
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
    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.Dispose:
          handleSwtRemoveNotify();
          break;
      }
    }
  }
}
