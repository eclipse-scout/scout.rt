/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.testing.client.form;

import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;

/**
 * Dynamic form to build an ad-hoc application for testing
 */
public class DynamicForm extends AbstractForm {
  private IGroupBox m_mainBox;

  public DynamicForm(String title, IGroupBox mainBox) {
    super(false);
    m_mainBox = mainBox;
    callInitializer();
    setTitle(title);
  }

  @Override
  public IGroupBox getRootGroupBox() {
    return m_mainBox;
  }

  public IButton getButton(String id) {
    return (IButton) getFieldById(id);
  }

  public void start(IFormHandler handler) {
    startInternal(handler);
  }

}
