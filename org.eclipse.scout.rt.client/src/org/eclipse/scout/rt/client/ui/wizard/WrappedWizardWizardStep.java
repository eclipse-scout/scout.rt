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
package org.eclipse.scout.rt.client.ui.wizard;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

/**
 * Wizard step containing another wizard. This step invokes the parent's <code>doNextStep()</code> method when
 * terminated (i.e. either finished or
 * canceled).
 * vastly reduced amount of automation and allows for much more custom
 * flexibility in handling wizard processes.
 */
public class WrappedWizardWizardStep extends AbstractWizardStep {

  private final IWizard m_parentWizard;
  private final IWizard m_childWizard;

  public WrappedWizardWizardStep(IWizard parentWizard, IWizard childWizard) throws ProcessingException {
    super();
    this.m_parentWizard = parentWizard;
    this.m_childWizard = childWizard;
    setTitle(childWizard.getTitle());
  }

  @Override
  protected void execActivate(int stepKind) throws ProcessingException {
    m_childWizard.addWizardListener(new WizardListener() {
      public void wizardChanged(WizardEvent e) {
        switch (e.getType()) {
          case WizardEvent.TYPE_CLOSED: {
            try {
              m_parentWizard.doNextStep();
            }
            catch (ProcessingException t) {
              SERVICES.getService(IExceptionHandlerService.class).handleException(t);
            }
            break;
          }
        }
      }
    });
    m_childWizard.start();
  }

  public IWizard getParentWizard() {
    return m_parentWizard;
  }

  public IWizard getChildWizard() {
    return m_childWizard;
  }
}
