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
package org.eclipse.scout.rt.ui.swt.form;

import java.io.File;
import java.util.WeakHashMap;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.swt.DefaultValidateRoot;
import org.eclipse.scout.rt.ui.swt.IValidateRoot;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.basic.ISwtScoutComposite;
import org.eclipse.scout.rt.ui.swt.basic.SwtScoutComposite;
import org.eclipse.scout.rt.ui.swt.basic.WidgetPrinter;
import org.eclipse.scout.rt.ui.swt.form.fields.ISwtScoutFormField;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFormFieldGridData;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class SwtScoutForm extends SwtScoutComposite<IForm> implements ISwtScoutForm {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutForm.class);

  private FormListener m_scoutFormListener;
  private WeakHashMap<FormEvent, Object> m_consumedScoutFormEvents = new WeakHashMap<FormEvent, Object>();

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    ISwtScoutFormField swtGroup = getEnvironment().createFormField(container, getScoutObject().getRootGroupBox());
    Composite swtField = swtGroup.getSwtContainer();
    setSwtContainer(container);
    setSwtField(swtField);

    // use grid layout with decent min-width
    SwtScoutFormFieldGridData layoutData = new SwtScoutFormFieldGridData(getScoutObject().getRootGroupBox());
    getSwtField().setLayoutData(layoutData);
    container.setLayout(new LogicalGridLayout(0, 0));
    container.setData(IValidateRoot.VALIDATE_ROOT_DATA, new DefaultValidateRoot(parent));
  }

  @Override
  protected void attachScout() {
    super.attachScout();
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
  public Composite getSwtFormPane() {
    return (Composite) getSwtField();
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
    if (modelField != null) {
      handleRequestFocusFromScout(modelField, true);
    }
  }

  private Control findUiField(IFormField modelField) {
    if (modelField == null) {
      return null;
    }
    for (Control comp : SwtUtility.findChildComponents(getSwtContainer(), Control.class)) {
      ISwtScoutComposite<?> composite = SwtScoutFieldComposite.getCompositeOnWidget(comp);
      if (composite != null && composite.getScoutObject() == modelField) {
        return composite.getSwtField();
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
        Shell sh = getSwtFormPane().getShell();
        if (sh.isVisible()) {
          // TODO swt not supported in swt: sh.toFront()
        }
        break;
      }
      case FormEvent.TYPE_TO_BACK: {
        Shell sh = getSwtFormPane().getShell();
        if (sh.isVisible()) {
          // TODO swt not supported in swt: sh.toBack()
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
      if (getSwtFormPane() != null) {
        if (e.getFormField() != null) {
          for (Control c : SwtUtility.findChildComponents(getSwtContainer(), Control.class)) {
            IPropertyObserver scoutModel = (IPropertyObserver) c.getData(ISwtScoutFormField.CLIENT_PROPERTY_SCOUT_OBJECT);
            if (scoutModel == e.getFormField()) {
              wp = new WidgetPrinter(c);
              break;
            }
          }
        }
        if (wp == null) {
          wp = new WidgetPrinter(getSwtFormPane().getShell());
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
      getEnvironment().invokeScoutLater(r, 0);
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
              if (getSwtFormPane() != null && !getSwtFormPane().isDisposed()) {
                handleScoutFormEventInUi(e);
              }
            }
          };
          getEnvironment().invokeSwtLater(t);
          break;
        }
      }
    }
  }// end private class
}
