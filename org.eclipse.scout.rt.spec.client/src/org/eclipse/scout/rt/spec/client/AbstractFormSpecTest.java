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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.spec.client.gen.FormSpecGenerator;
import org.eclipse.scout.rt.spec.client.out.IDocSection;
import org.eclipse.scout.rt.spec.client.screenshot.FormScreenshotPrinter;
import org.eclipse.scout.rt.spec.client.screenshot.PrintScreenshotsFormListener;

public abstract class AbstractFormSpecTest extends AbstractSpecGenTest {

  /**
   * Generate form spec as mediawiki file
   * 
   * @throws ProcessingException
   */
  @Override
  public void generateSpec() throws ProcessingException {
    List<File> screenshots = printScreenshots();
    IForm form = createAndStartForm(Collections.<FormListener> emptyList());
    form.activate();
    IDocSection doc = generateDocSection(form);
    writeMediawikiFile(doc, SpecUtility.getSpecFileBaseName(form), getImagePaths(screenshots));
    form.doClose();
  }

  protected List<File> printScreenshots() throws ProcessingException {
    ArrayList<FormListener> formListeners = new ArrayList<FormListener>();
    PrintScreenshotsFormListener listener = new PrintScreenshotsFormListener(new FormScreenshotPrinter(SpecIOUtility.getSpecFileConfigInstance().getImageDir()));
    formListeners.add(listener);
    IForm form = createAndStartForm(formListeners);
    form.activate();
    form.waitFor();
    return listener.getPrintedFiles();
  }

  /**
   * create the form, append the listeners and then start the form
   * 
   * @param formListeners
   * @return
   * @throws ProcessingException
   */
  // TODO ASA refactor in scout 4.0: split create and provide a default impl. for appendListenersAndStart()
  protected abstract IForm createAndStartForm(List<FormListener> formListeners) throws ProcessingException;

  protected IDocSection generateDocSection(IForm form) {
    FormSpecGenerator g = new FormSpecGenerator(getConfiguration());
    return g.getDocSection(form);
  }

  /**
   * get file path for all printed screenshots
   * 
   * @param form
   * @return
   * @throws ProcessingException
   */
  protected String[] getImagePaths(List<File> screenshots) throws ProcessingException {
    File[] files = CollectionUtility.toArray(screenshots, File.class);
    return SpecIOUtility.addPrefix(SpecIOUtility.getRelativePaths(files, SpecIOUtility.getSpecFileConfigInstance().getSpecDir()), "../");
  }

  @Override
  protected int getTopHeadingLevel() {
    return getConfiguration().getFormConfig().getTopHeadingLevel();
  }

}
