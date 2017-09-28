/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.wizard;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformError;

/**
 * Wizard step containing another wizard. This step invokes the parent's <code>doNextStep()</code> method when
 * terminated (i.e. either finished or canceled). vastly reduced amount of automation and allows for much more custom
 * flexibility in handling wizard processes.
 */
@ClassId("f66e442b-7056-4fbd-bd72-f19a0b63c1c3")
public class WrappedWizardWizardStep extends AbstractWizardStep<IForm> {

  private final IWizard m_parentWizard;
  private final IWizard m_childWizard;

  public WrappedWizardWizardStep(IWizard parentWizard, IWizard childWizard) {
    super();
    this.m_parentWizard = parentWizard;
    this.m_childWizard = childWizard;
    setTitle(childWizard.getTitle());
  }

  @Override
  protected void execActivate(int stepKind) {
    m_childWizard.addWizardListener(e -> {
      switch (e.getType()) {
        case WizardEvent.TYPE_CLOSED: {
          try {
            m_parentWizard.doNextStep();
          }
          catch (RuntimeException | PlatformError t) {
            BEANS.get(ExceptionHandler.class).handle(t);
          }
          break;
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
