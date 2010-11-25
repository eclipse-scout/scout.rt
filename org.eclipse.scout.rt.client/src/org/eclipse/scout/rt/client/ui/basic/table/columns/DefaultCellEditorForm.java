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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;

/**
 * form to build an ad-hoc cell editor field
 */
public class DefaultCellEditorForm extends AbstractForm {
  private final IGroupBox m_mainBox;

  public DefaultCellEditorForm(IFormField editorField) throws ProcessingException {
    super(false);
    m_mainBox = new DefaultCellEditorMainBox(editorField);
    callInitializer();
  }

  @Override
  protected void initConfig() throws ProcessingException {
    super.initConfig();
    setAutoAddRemoveOnDesktop(false);
  }

  @Override
  public IGroupBox getRootGroupBox() {
    return m_mainBox;
  }

  public void start() throws ProcessingException {
    startInternal(new AbstractFormHandler() {
    });
  }

}
