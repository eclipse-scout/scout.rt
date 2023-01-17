/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.testing.client.form;

import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * Dynamic form to build an ad-hoc application for testing
 */
@ClassId("0b6b8066-b0e4-4d29-a809-c6fe615b9dc8")
public class DynamicForm extends AbstractForm {
  private final IGroupBox m_mainBox;

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
