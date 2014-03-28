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
package org.eclipse.scout.rt.ui.rap.window;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.tool.IToolButton;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.action.RwtScoutToolbarAction;
import org.eclipse.ui.forms.widgets.Form;

/**
 * Abstract rwt scout part composite for a form.
 * <p>
 * This base implementation handles form attachment and listens for form removal on the desktop
 */
public abstract class AbstractRwtScoutPart implements IRwtScoutPart {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractRwtScoutPart.class);

  private IForm m_scoutForm;
  private IRwtEnvironment m_uiEnvironment;
  private boolean m_opened;
  private PropertyChangeListener m_formPropertyListener;
  private DesktopListener m_desktopListener;

  public AbstractRwtScoutPart() {
  }

  @Override
  public void setBusy(boolean b) {
    //nop
  }

  protected void createPart(IForm scoutForm, IRwtEnvironment uiEnvironment) {
    if (m_scoutForm != null) {
      throw new IllegalArgumentException("The form dialog is already open. The form '" + scoutForm.getTitle() + " (" + scoutForm.getClass().getName() + ")' can not be opened!");
    }
    m_scoutForm = scoutForm;
    m_uiEnvironment = uiEnvironment;
    m_formPropertyListener = new P_ScoutPropertyChangeListener();
    m_desktopListener = new P_ScoutDesktopListener();
    IDesktop desktop = getUiEnvironment().getScoutDesktop();
    if (desktop != null) {
      desktop.addDesktopListener(m_desktopListener);
    }
  }

  @Override
  public final void showPart() {
    if (m_opened) {
      return;
    }
    m_opened = true;
    showPartImpl();
    //double-check if the model is still showing
//    if (!m_scoutForm.isFormOpen()) {FIXME
//      closePart();
//      return;
//    }
  }

  protected abstract void showPartImpl();

  @Override
  public final void closePart() {
    if (!m_opened) {
      return;
    }
    m_opened = false;
    IDesktop desktop = getUiEnvironment().getScoutDesktop();
    if (desktop != null) {
      desktop.removeDesktopListener(m_desktopListener);
    }
    closePartImpl();
  }

  protected abstract void closePartImpl();

  @Override
  public IForm getScoutObject() {
    return m_scoutForm;
  }

  protected IRwtEnvironment getUiEnvironment() {
    return m_uiEnvironment;
  }

  protected void attachScout() {
    updateToolbarActionsFromScout();
    IForm form = getScoutObject();
    // listeners
    form.addPropertyChangeListener(m_formPropertyListener);
    //init properties
    setTitleFromScout();
    setImageFromScout();
    setMaximizeEnabledFromScout();
    setMaximizedFromScout();
    setMinimizeEnabledFromScout();
    setMinimizedFromScout();
    boolean closable = false;
    for (IFormField f : form.getAllFields()) {
      if (f.isEnabled() && f.isVisible() && f instanceof IButton) {
        switch (((IButton) f).getSystemType()) {
          case IButton.SYSTEM_TYPE_CLOSE:
          case IButton.SYSTEM_TYPE_CANCEL: {
            closable = true;
            break;
          }
        }
      }
      if (closable) {
        break;
      }
    }
    setCloseEnabledFromScout(closable);
  }

  /**
  *
  */
  protected void updateToolbarActionsFromScout() {
    Form uiForm = getUiForm();
    if (uiForm == null) {
      return;
    }
    List<IToolButton> toolbuttons = ActionUtility.visibleNormalizedActions(getScoutObject().getToolButtons());
    if (!toolbuttons.isEmpty()) {
      IToolBarManager toolBarManager = uiForm.getToolBarManager();
      for (IToolButton b : toolbuttons) {
        toolBarManager.add(new RwtScoutToolbarAction(b, toolBarManager, getUiEnvironment()));
      }
      toolBarManager.update(true);
    }

  }

  protected void detachScout() {
    // listeners
    getScoutObject().removePropertyChangeListener(m_formPropertyListener);
  }

  protected void setTitleFromScout() {
  }

  protected void setImageFromScout() {
  }

  protected void setMaximizeEnabledFromScout() {
  }

  protected void setMaximizedFromScout() {
  }

  protected void setMinimizeEnabledFromScout() {
  }

  protected void setMinimizedFromScout() {
  }

  protected void setCloseEnabledFromScout(boolean defaultValue) {
  }

  protected void ensureFormVisible() {
  }

  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (name.equals(IForm.PROP_TITLE)) {
      setTitleFromScout();
    }
    else if (name.equals(IForm.PROP_ICON_ID)) {
      setImageFromScout();
    }
    else if (name.equals(IForm.PROP_MINIMIZE_ENABLED)) {
      setMinimizeEnabledFromScout();
    }
    else if (name.equals(IForm.PROP_MAXIMIZE_ENABLED)) {
      setMaximizeEnabledFromScout();
    }
    else if (name.equals(IForm.PROP_MINIMIZED)) {
      setMinimizedFromScout();
    }
    else if (name.equals(IForm.PROP_MAXIMIZED)) {
      setMaximizedFromScout();
    }
  }

  private void handleDesktopChangedUiThread(DesktopEvent e) {
    switch (e.getType()) {
      case DesktopEvent.TYPE_FORM_REMOVED: {
        //auto-detach listener
        IDesktop desktop = e.getDesktop();
        desktop.removeDesktopListener(m_desktopListener);
        try {
          closePart();
        }
        catch (Throwable ex) {
          LOG.warn("could not close part.", ex);
        }
        break;
      }
      case DesktopEvent.TYPE_FORM_ENSURE_VISIBLE: {
        ensureFormVisible();
        break;
      }
    }
  }

  private class P_ScoutPropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(final PropertyChangeEvent e) {
      if (getUiEnvironment().getDisplay() != null && !getUiEnvironment().getDisplay().isDisposed()) {
        Runnable t = new Runnable() {
          @Override
          public void run() {
            handleScoutPropertyChange(e.getPropertyName(), e.getNewValue());
          }
        };
        getUiEnvironment().invokeUiLater(t);
      }
    }
  }// end private class

  private class P_ScoutDesktopListener implements DesktopListener {
    @Override
    public void desktopChanged(final DesktopEvent e) {
      if (getUiEnvironment().getDisplay() != null && !getUiEnvironment().getDisplay().isDisposed()) {
        if (e.getForm() == getScoutObject()) {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              handleDesktopChangedUiThread(e);
            }
          };
          getUiEnvironment().invokeUiLater(t);
        }
      }
    }
  }

}
