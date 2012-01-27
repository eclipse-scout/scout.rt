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

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartFieldProposalForm;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.basic.WidgetPrinter;
import org.eclipse.scout.rt.ui.rap.core.DefaultValidateRoot;
import org.eclipse.scout.rt.ui.rap.core.IValidateRoot;
import org.eclipse.scout.rt.ui.rap.core.form.IRwtScoutForm;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFormFieldGridData;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class RwtScoutForm extends RwtScoutComposite<IForm> implements IRwtScoutForm {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutForm.class);

  private FormListener m_scoutFormListener;

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
    // void
  }

  private class P_ScoutFormListener implements FormListener {
    @Override
    public void formChanged(final FormEvent e) {
      Display display = getUiEnvironment().getDisplay();
      if (display == null) {
        return;
      }
      switch (e.getType()) {
        case FormEvent.TYPE_PRINT: {
          final Object lock = new Object();
          Runnable t = new Runnable() {
            @Override
            public void run() {
              try {
                WidgetPrinter wp = null;
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
                synchronized (lock) {
                  lock.notifyAll();
                }
              }
            }
          };
          synchronized (lock) {
            display.asyncExec(t);
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
              if (getUiFormPane() != null && !getUiFormPane().isDisposed()) {
                Shell sh = getUiFormPane().getShell();
                if (sh.isVisible()) {
                  // TODO not supported in swt: sh.toFront()
                }
              }
            }
          };
          getUiEnvironment().invokeUiLater(t);
          break;
        }
        case FormEvent.TYPE_TO_BACK: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              if (getUiFormPane() != null && !getUiFormPane().isDisposed()) {
                Shell sh = getUiFormPane().getShell();
                if (sh.isVisible()) {
                  // TODO not supported in swt: sh.toBack()
                }
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
