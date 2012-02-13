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
package org.eclipse.scout.rt.ui.rap.form.fields.filechooserfield;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.IFileChooserField;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.extension.IFormFieldFactory;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.swt.widgets.Composite;

/**
 * Distinguish between file upload and file download
 * <p>
 * File download automatically creates a temp file and sets it to the ui. The model code is responsible to detect that
 * web ui is in use and trigger the end-iser download of the temporary file.
 */
public class FileChooserFieldFactory implements IFormFieldFactory {

  @Override
  public IRwtScoutFormField<?> createUiFormField(Composite parent, IFormField model, IRwtEnvironment uiEnvironment) {
    IFileChooserField scoutField = (IFileChooserField) model;
    if (scoutField.isTypeLoad()) {
      RwtScoutFileUploadField ui = new RwtScoutFileUploadField();
      ui.createUiField(parent, scoutField, uiEnvironment);
      return ui;
    }
    else {
      RwtScoutFileDownloadField ui = new RwtScoutFileDownloadField();
      ui.createUiField(parent, scoutField, uiEnvironment);
      return ui;
    }
  }

}
