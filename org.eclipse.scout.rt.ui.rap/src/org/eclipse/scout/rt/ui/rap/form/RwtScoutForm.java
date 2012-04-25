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
package org.eclipse.scout.rt.ui.rap.form;

import java.io.File;
import java.util.WeakHashMap;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartFieldProposalForm;
import org.eclipse.scout.rt.ui.rap.DefaultValidateRoot;
import org.eclipse.scout.rt.ui.rap.IValidateRoot;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.basic.IRwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.basic.WidgetPrinter;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFormFieldGridData;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class RwtScoutForm extends RwtScoutComposite<IForm> implements IRwtScoutForm {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutForm.class);

  private FormListener m_scoutFormListener;
  private WeakHashMap<FormEvent, Object> m_consumedScoutFormEvents = new WeakHashMap<FormEvent, Object>();

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    IRwtScoutFormField group = getUiEnvironment().createFormField(container, getScoutObject().getRootGroupBox());
    Composite field = group.getUiContainer();
    setUiContainer(container);
    setUiField(field);

    // use grid layout with decent min-width
    RwtScoutFormFieldGridData layoutData = new RwtScoutFormFieldGridData(getScoutObject().getRootGroupBox());
    getUiField().setLayoutData(layoutData);
    container.setLayout(new LogicalGridLayout(0, 0));
    container.setData(IValidateRoot.VALIDATE_ROOT_DATA, new DefaultValidateRoot(parent));
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    if (getScoutObject() instanceof ISmartFieldProposalForm) {
      getUiContainer().setData(WidgetUtil.CUSTOM_VARIANT, RwtUtility.VARIANT_PROPOSAL_FORM);
    }
    if (m_scoutFormListener == null) {
      m_scoutFormListener = new P_ScoutFormListener();
      getScoutObject().addFormListener(m_scoutFormListener);
    }
    // process all pending events, except requestFocus
    IEventHistory<FormEvent> h = getScoutObject().getEventHistory();
    if (h != null) {
      for (FormEvent e : h.getRecentEvents()) {
        switch (e.getType()) {
          case FormEvent.TYPE_TO_BACK:
          case FormEvent.TYPE_TO_FRONT:
          case FormEvent.TYPE_PRINT: {
            handleScoutFormEventInUi(e);
            break;
          }
        }
      }
    }
    setInitialFocus();
  }

  @Override
  protected void detachScout() {
    super.detachScout();
    if (m_scoutFormListener != null) {
      getScoutObject().removeFormListener(m_scoutFormListener);
      m_scoutFormListener = null;
    }
  }

  @Override
  public Composite getUiFormPane() {
    return (Composite) getUiField();
  }

  @Override
  public IForm getScoutObject() {
    return super.getScoutObject();
  }

  @Override
  public void setInitialFocus() {
    IFormField modelField = null;
    //check for request focus events in history
    IEventHistory<FormEvent> h = getScoutObject().getEventHistory();
    if (h != null) {
      for (FormEvent e : h.getRecentEvents()) {
        if (e.getType() == FormEvent.TYPE_REQUEST_FOCUS) {
          modelField = e.getFormField();
          break;
        }
      }
    }
    if (modelField == null) {
      for (IFormField f : getScoutObject().getAllFields()) {
        if ((f instanceof IValueField || f instanceof IButton)
            && f.isFocusable()
            && f.isVisible()
            && f.isEnabled()) {
          modelField = f;
          break;
        }
      }
    }
    if (modelField != null) {
      handleRequestFocusFromScout(modelField, true);
    }
  }

  private Control findUiField(IFormField modelField) {
    if (modelField == null) {
      return null;
    }
    for (Control comp : RwtUtility.findChildComponents(getUiContainer(), Control.class)) {
      IRwtScoutComposite<?> composite = RwtScoutFieldComposite.getCompositeOnWidget(comp);
      if (composite != null && composite.getScoutObject() == modelField) {
        return composite.getUiField();
      }
    }
    return null;
  }

  protected void handleScoutFormEventInUi(final FormEvent e) {
    if (m_consumedScoutFormEvents.containsKey(e)) {
      return;
    }
    m_consumedScoutFormEvents.put(e, Boolean.TRUE);
    //
    switch (e.getType()) {
      case FormEvent.TYPE_PRINT: {
        handlePrintFromScout(e);
        break;
      }
      case FormEvent.TYPE_TO_FRONT: {
        Shell sh = getUiFormPane().getShell();
        if (sh.isVisible()) {
          // TODO rap not supported in swt: sh.toFront()
        }
        break;
      }
      case FormEvent.TYPE_TO_BACK: {
        Shell sh = getUiFormPane().getShell();
        if (sh.isVisible()) {
          // TODO rap not supported in swt: sh.toBack()
        }
        break;
      }
      case FormEvent.TYPE_REQUEST_FOCUS: {
        handleRequestFocusFromScout(e.getFormField(), false);
        break;
      }
    }
  }

  protected void handlePrintFromScout(final FormEvent e) {
    WidgetPrinter wp = null;
    try {
      if (getUiFormPane() != null) {
        if (e.getFormField() != null) {
          for (Control c : RwtUtility.findChildComponents(getUiContainer(), Control.class)) {
            IPropertyObserver scoutModel = (IPropertyObserver) c.getData(IRwtScoutFormField.CLIENT_PROPERTY_SCOUT_OBJECT);
            if (scoutModel == e.getFormField()) {
              wp = new WidgetPrinter(c);
              break;
            }
          }
        }
        if (wp == null) {
          wp = new WidgetPrinter(getUiFormPane().getShell());
        }
      }
      if (wp != null) {
        try {
          wp.print(e.getPrintDevice(), e.getPrintParameters());
        }
        catch (Throwable ex) {
          LOG.error(null, ex);
        }
      }
    }
    finally {
      File outputFile = null;
      if (wp != null) {
        outputFile = wp.getOutputFile();
      }
      final File outputFileFinal = outputFile;
      Runnable r = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().fireFormPrintedFromUI(outputFileFinal);
        }
      };
      getUiEnvironment().invokeScoutLater(r, 0);
    }
  }

  protected void handleRequestFocusFromScout(IFormField modelField, boolean force) {
    if (modelField == null) {
      return;
    }
    Control comp = findUiField(modelField);
    if (comp != null && comp.getVisible()) {
      Control[] tabList = (comp instanceof Composite ? ((Composite) comp).getTabList() : null);
      if (tabList != null && tabList.length > 0) {
        comp = tabList[0];
      }
      if (comp != null && comp.getVisible()) {
        if (force) {
          comp.forceFocus();
        }
        else {
          comp.setFocus();
        }
      }
    }
  }

  private class P_ScoutFormListener implements FormListener {
    @Override
    public void formChanged(final FormEvent e) {
      Display display = getUiEnvironment().getDisplay();
      if (display == null) {
        return;
      }
      switch (e.getType()) {
        case FormEvent.TYPE_STRUCTURE_CHANGED: {
          break;
        }
        case FormEvent.TYPE_PRINT:
        case FormEvent.TYPE_TO_FRONT:
        case FormEvent.TYPE_TO_BACK:
        case FormEvent.TYPE_REQUEST_FOCUS: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              if (getUiFormPane() != null && !getUiFormPane().isDisposed()) {
                handleScoutFormEventInUi(e);
              }
            }
          };
          getUiEnvironment().invokeUiLater(t);
          break;
        }
      }
    }
  }// end private class
}
