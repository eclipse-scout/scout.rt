/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop;

import java.util.Deque;
import java.util.LinkedList;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.Bean;

/**
 * This class tracks the activation of Forms.
 */
@Bean
public class FormActivationTracker implements FormListener, DesktopListener {

  protected final Deque<IForm> m_formActivationHistory = new LinkedList<>();

  /**
   * Starts tracking of activated forms.
   */
  public void start(final IDesktop desktop) {
    desktop.addDesktopListener(this);
  }

  /**
   * Stops tracking of activated forms.
   */
  public void stop(final IDesktop desktop) {
    desktop.removeDesktopListener(this);
  }

  /**
   * @return the currently active {@link IForm}, or <code>null</code> if not available.
   */
  @SuppressWarnings("unchecked")
  public <FORM extends IForm> FORM getActiveForm() {
    return (FORM) m_formActivationHistory.getFirst();
  }

  /**
   * @return the most recent activated {@link IForm} of the given type, or <code>null</code> if not available. However,
   *         that IForm must not be the currently active {@link IForm}.
   */
  @SuppressWarnings("unchecked")
  public <FORM extends IForm> FORM getActiveForm(final Class<FORM> formType) {
    if (formType == null) {
      return null;
    }

    for (final IForm candidate : m_formActivationHistory) {
      if (formType.isAssignableFrom(candidate.getClass())) {
        return (FORM) candidate;
      }
    }
    return null;
  }

  @Override
  public void desktopChanged(final DesktopEvent e) {
    switch (e.getType()) {
      case DesktopEvent.TYPE_FORM_SHOW: {
        e.getForm().addFormListener(this);
        break;
      }
      case DesktopEvent.TYPE_FORM_HIDE: {
        final IForm form = e.getForm();
        m_formActivationHistory.remove(form);
        form.removeFormListener(this);
        break;
      }
      default:
        break;
    }
  }

  @Override
  public void formChanged(final FormEvent e) throws ProcessingException {
    if (e.getType() == FormEvent.TYPE_ACTIVATED) {
      // Move the Form to the head of the queue.
      m_formActivationHistory.remove(e.getForm());
      m_formActivationHistory.addFirst(e.getForm());
    }
  }
}
