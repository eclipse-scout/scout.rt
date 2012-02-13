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
package org.eclipse.scout.rt.ui.rap.form.fields.filechooserfield;

import java.io.File;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.IFileChooserField;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutValueFieldComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class RwtScoutFileDownloadField extends RwtScoutValueFieldComposite<IFileChooserField> implements IRwtScoutFileDownloadField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutFileDownloadField.class);

  public RwtScoutFileDownloadField() {
  }

  @Override
  protected void initializeUi(Composite parent) {
    super.initializeUi(parent);
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());
    Label dummyText = getUiEnvironment().getFormToolkit().createLabel(container, "(" + TEXTS.get("Automatic") + ")", SWT.NONE);
    dummyText.setEnabled(false);

    setUiContainer(container);
    setUiLabel(label);
    setUiField(dummyText);

    // layout
    container.setLayout(new LogicalGridLayout(1, 0));
  }

  @Override
  public Label getUiField() {
    return (Label) super.getUiField();
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    try {
      String fileName = getScoutObject().getFileName();
      if (fileName == null) {
        String[] exts = getScoutObject().getFileExtensions();
        fileName = "download." + (exts != null && exts.length > 0 ? exts[0] : "tmp");
      }
      final File tempFile = new File(IOUtility.createTempDirectory("download"), fileName);
      tempFile.deleteOnExit();
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().setTextFromUI(tempFile.getAbsolutePath());
        }
      };
      getUiEnvironment().invokeScoutLater(t, 0);
      // end notify
    }
    catch (Exception e) {
      LOG.error("Failed creating temporary file for " + getScoutObject().getClass(), e);
    }
  }
}
