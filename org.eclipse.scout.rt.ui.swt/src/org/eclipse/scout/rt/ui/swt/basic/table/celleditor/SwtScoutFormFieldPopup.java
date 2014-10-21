/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this tribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.basic.table.celleditor;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.ui.swt.basic.SwtScoutComposite;
import org.eclipse.scout.rt.ui.swt.form.fields.ISwtScoutFormField;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.scout.rt.ui.swt.window.popup.SwtScoutDropDownPopup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Wraps a {@link IFormField} to be displayed as popup cell editor.
 */
public class SwtScoutFormFieldPopup extends SwtScoutComposite<IFormField> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutFormFieldPopup.class);

  private SwtScoutDropDownPopup m_swtScoutPopup;

  private Composite m_owner;
  private int m_minWidth;
  private int m_prefWidth;
  private int m_minHeight;
  private int m_prefHeight;
  private int m_style;

  private Control m_swtFormField;

  private final Set<IFormFieldPopupListener> m_listeners = Collections.synchronizedSet(new HashSet<IFormFieldPopupListener>());

  public SwtScoutFormFieldPopup(Composite owner) {
    m_owner = owner;
    m_style = SWT.NO_TRIM; // no trim area on the popup Shell.
  }

  @Override
  protected void initializeSwt(Composite parent) {
    super.initializeSwt(parent);

    // Create the Popup-Shell but do not make it visible yet; This is done the time the owner component is painted.
    m_swtScoutPopup = new SwtScoutDropDownPopup(getEnvironment(), m_owner, true, m_style);
    m_swtScoutPopup.setPopupOnField(true);
    m_swtScoutPopup.setHeightHint(m_prefHeight);
    m_swtScoutPopup.setWidthHint(m_prefWidth);
    m_swtScoutPopup.getShell().setMinimumSize(m_minWidth, m_minHeight);

    // Listener to be notified about traversal events and keystrokes occurred on the popup.
    final TraverseListener traverseListener = new TraverseListener() {
      @Override
      public void keyTraversed(TraverseEvent e) {
        switch (e.detail) {
          case SWT.TRAVERSE_TAB_NEXT: {
            e.doit = false;
            notifyListeners(IFormFieldPopupListener.TYPE_OK | IFormFieldPopupListener.TYPE_FOCUS_NEXT);
            break;
          }
          case SWT.TRAVERSE_TAB_PREVIOUS: {
            e.doit = false;
            notifyListeners(IFormFieldPopupListener.TYPE_OK | IFormFieldPopupListener.TYPE_FOCUS_BACK);
            break;
          }
          case SWT.TRAVERSE_ESCAPE: {
            e.doit = false;
            notifyListeners(IFormFieldPopupListener.TYPE_CANCEL);
            break;
          }
          case SWT.TRAVERSE_RETURN: {
            e.doit = false;
            notifyListeners(IFormFieldPopupListener.TYPE_OK);
            break;
          }
        }
      }
    };

    // Listener to handle Shell deactivation events.
    m_swtScoutPopup.addShellListener(new ShellAdapter() {

      @Override
      public void shellDeactivated(ShellEvent e) {
        notifyListeners(IFormFieldPopupListener.TYPE_OK);
      }
    });

    // Defer opening the Shell to be positioned at the location of the owner component.
    m_owner.addPaintListener(new PaintListener() {

      @Override
      public void paintControl(PaintEvent e) {
        if (m_owner.isDisposed()) {
          return; // do not open the Shell if the owner is already disposed.
        }
        m_owner.removePaintListener(this);

        // Create the Form to contain the form-field.
        final IForm form = createForm();
        if (form != null) {
          m_swtScoutPopup.showForm(form);
          m_swtFormField = findSwtFormField(m_swtScoutPopup.getUiForm().getSwtField(), getScoutObject());
          if (m_swtFormField == null) {
            LOG.warn("UI-FormField could not be found in UI-Form");
          }

          // Install listener to be notified about traversal events.
          installTraverseListener(m_swtScoutPopup.getShell(), traverseListener);
        }
        else {
          LOG.error("Failed to create the Form for the form-field.");
        }
      }

    });
    setSwtContainer(m_owner);
  }

  public void setMinWidth(int minWidth) {
    m_minWidth = minWidth;
  }

  public void setPrefWidth(int prefWidth) {
    m_prefWidth = prefWidth;
  }

  public void setMinHeight(int minHeight) {
    m_minHeight = minHeight;
  }

  public void setPrefHeight(int prefHeight) {
    m_prefHeight = prefHeight;
  }

  /**
   * Closes the popup.
   */
  public void closePopup() {
    m_swtScoutPopup.closePart();
  }

  /**
   * Touches the field to write its UI value back to the model.
   */
  public void touch() {
    if (m_swtFormField != null && !m_swtFormField.isDisposed()) {
      SwtUtility.runSwtInputVerifier(m_swtFormField);
    }
  }

  public void addListener(IFormFieldPopupListener listener) {
    m_listeners.add(listener);
  }

  public void removeListener(IFormFieldPopupListener listener) {
    m_listeners.remove(listener);
  }

  private void notifyListeners(int eventType) {
    for (IFormFieldPopupListener listener : m_listeners) {
      listener.handleEvent(eventType);
    }
  }

  /**
   * Installs the given traverse listener on the given control and its child controls.
   */
  private void installTraverseListener(Control control, TraverseListener listener) {
    control.addTraverseListener(listener);
    if (control instanceof Composite) {
      Composite composite = (Composite) control;
      for (Control child : composite.getChildren()) {
        installTraverseListener(child, listener);
      }
    }
  }

  /**
   * @return creates the {@link IForm} that contains the {@link IFormField}.
   */
  private IForm createForm() {
    final Holder<IForm> result = new Holder<IForm>();
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          P_Form form = new P_Form(getScoutObject());
          form.setAutoAddRemoveOnDesktop(false);
          form.startForm();
          result.setValue(form);
        }
        catch (Exception e) {
          LOG.error("Failed to create and start popup form.", e);
        }
      }
    };

    try {
      getEnvironment().invokeScoutLater(runnable, 2345).join(2345);
    }
    catch (InterruptedException e) {
      LOG.warn("Interrupted while waiting for the popup form to be started.", e);
    }

    return result.getValue();
  }

  /**
   * Finds the <code>FormField</code> {@link Control} that represents the {@link IFormField} in the hierarchy of the
   * given {@link Control}.
   *
   * @param control
   *          container.
   * @param formField
   *          {@link IFormField}.
   * @return {@link Control} or <code>null</code> if not found.
   */
  private static Control findSwtFormField(Control control, IFormField formField) {
    Object o = control.getData(ISwtScoutFormField.CLIENT_PROPERTY_SCOUT_OBJECT);
    if (o == formField) {
      return control;
    }

    if (control instanceof Composite) {
      for (Control child : ((Composite) control).getChildren()) {
        Control candiate = findSwtFormField(child, formField);
        if (candiate != null) {
          return candiate;
        }
      }
    }
    return null;
  }

  /**
   * {@link IForm} to contain the {@link IFormField}.
   */
  private static class P_Form extends AbstractForm {

    private final IFormField m_formField;

    public P_Form(IFormField formField) throws ProcessingException {
      super(false);
      m_formField = formField;
      callInitializer();
    }

    @Override
    protected boolean getConfiguredModal() {
      return false;
    }

    @Override
    protected int getConfiguredDisplayHint() {
      return DISPLAY_HINT_VIEW;
    }

    @Override
    public String getDisplayViewId() {
      return IForm.VIEW_ID_CENTER;
    }

    @Override
    protected boolean getConfiguredAskIfNeedSave() {
      return false;
    }

    public void startForm() throws ProcessingException {
      startInternal(new FormHandler());
    }

    public MainBox getMainBox() {
      return (MainBox) getRootGroupBox();
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Override
      protected void injectFieldsInternal(List<IFormField> fieldList) {
        fieldList.add(m_formField);
      }

      @Override
      protected boolean getConfiguredBorderVisible() {
        return false;
      }

      @Override
      protected int getConfiguredGridColumnCount() {
        return 1;
      }

      @Override
      protected boolean getConfiguredGridUseUiWidth() {
        return true;
      }

      @Override
      protected boolean getConfiguredGridUseUiHeight() {
        return true;
      }
    }

    private class FormHandler extends AbstractFormHandler {
    }
  }
}
