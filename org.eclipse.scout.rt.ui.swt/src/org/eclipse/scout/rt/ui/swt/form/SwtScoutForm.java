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

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.swt.DefaultValidateRoot;
import org.eclipse.scout.rt.ui.swt.IValidateRoot;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.basic.SwtScoutComposite;
import org.eclipse.scout.rt.ui.swt.basic.WidgetPrinter;
import org.eclipse.scout.rt.ui.swt.form.fields.ISwtScoutFormField;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFormFieldGridData;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class SwtScoutForm extends SwtScoutComposite<IForm> implements ISwtScoutForm {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutForm.class);

  private FormListener m_scoutFormListener;

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
  }

  @Override
  protected void detachScout() {
    super.detachScout();
    if (m_scoutFormListener != null) {
      getScoutObject().removeFormListener(m_scoutFormListener);
      m_scoutFormListener = null;
    }
  }

  public Composite getSwtFormPane() {
    return (Composite) getSwtField();
  }

  @Override
  public IForm getScoutObject() {
    return super.getScoutObject();
  }

  public void setInitialFocus() {
    // void
  }

  private class P_ScoutFormListener implements FormListener {
    public void formChanged(final FormEvent e) {
      switch (e.getType()) {
        case FormEvent.TYPE_PRINT: {
          final Object lock = new Object();
          Runnable t = new Runnable() {
            @Override
            public void run() {
              try {
                WidgetPrinter wp = null;
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
                synchronized (lock) {
                  lock.notifyAll();
                }
              }
            }
          };
          synchronized (lock) {
            getEnvironment().getDisplay().asyncExec(t);
            try {
              lock.wait(30000L);
            }
            catch (InterruptedException ie) {
            }
          }
          break;
        }
        case FormEvent.TYPE_STRUCTURE_CHANGED: {
          // XXX from imo: check if necessary in swt and implement analogous to swing implementation
          break;
        }
        case FormEvent.TYPE_TO_FRONT: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              if (getSwtFormPane() != null) {
                Shell sh = getSwtFormPane().getShell();
                if (sh.isVisible()) {
                  // TODO not supported in swt: sh.toFront()
                }
              }
            }
          };
          getEnvironment().invokeSwtLater(t);
          break;
        }
        case FormEvent.TYPE_TO_BACK: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              if (getSwtFormPane() != null) {
                Shell sh = getSwtFormPane().getShell();
                if (sh.isVisible()) {
                  // TODO not supported in swt: sh.toBack()
                }
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
