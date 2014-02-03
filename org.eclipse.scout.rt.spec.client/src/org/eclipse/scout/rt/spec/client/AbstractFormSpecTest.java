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
package org.eclipse.scout.rt.spec.client;

import java.io.File;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.spec.client.gen.FormSpecGenerator;
import org.eclipse.scout.rt.spec.client.out.IDocSection;
import org.eclipse.scout.rt.spec.client.screenshot.FormPrinter;
import org.eclipse.scout.rt.spec.client.screenshot.PrintFormListener;

/**
 *
 */
public abstract class AbstractFormSpecTest extends AbstractSpecGen {

  public void printForm() throws ProcessingException {
    IForm form = createAndStartForm();
    File screensDir = getFileConfig().getImageDir();
    FormPrinter h = new FormPrinter(screensDir);

    form.addFormListener(new PrintFormListener(h));
    form.waitFor();
  }

  protected abstract IForm createAndStartForm() throws ProcessingException;

  protected IDocSection generate(IForm form) {
    FormSpecGenerator g = new FormSpecGenerator(getConfiguration());
    return g.getDocSection(form);
  }

  protected String getId(IForm form) {
    return form.getClass().getName();
  }

  public void printAllFields() throws ProcessingException {
    IForm form = createAndStartForm();
    IDocSection doc = generate(form);
    write(doc, getId(form), getImagePaths(form), form.getClass().getSimpleName());
    form.doClose();
  }

  /**
   * @param form
   * @return
   * @throws ProcessingException
   */
  private String[] getImagePaths(IForm form) throws ProcessingException {
    FormPrinter printer = new FormPrinter(getFileConfig().getImageDir());
    File[] printFiles = printer.getPrintFiles(form);
    return SpecIOUtility.addPrefix(SpecIOUtility.getRelativePaths(printFiles, getFileConfig().getSpecDir()), "../");
  }

}
