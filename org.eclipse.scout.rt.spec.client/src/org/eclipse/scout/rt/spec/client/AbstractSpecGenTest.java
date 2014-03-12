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
import java.io.Writer;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.spec.client.config.IDocConfig;
import org.eclipse.scout.rt.spec.client.out.IDocSection;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiWriter;
import org.eclipse.scout.testing.client.runner.ScoutClientGUITestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Base class for all spec test
 */
@RunWith(ScoutClientGUITestRunner.class)
public abstract class AbstractSpecGenTest {

  /**
   * Generate spec in one or more mediawiki files
   * 
   * @throws ProcessingException
   */
  @Test
  abstract public void generateSpec() throws ProcessingException;

  protected IDocConfig getConfiguration() {
    return SpecUtility.getDocConfigInstance();
  }

  /**
   * @param section
   * @param fileBaseName
   *          file name without extension
   * @param imagePaths
   * @throws ProcessingException
   */
  protected void writeMediawikiFile(IDocSection section, String fileBaseName, String[] imagePaths) throws ProcessingException {
    File wiki = SpecIOUtility.createNewFile(SpecIOUtility.getSpecFileConfigInstance().getMediawikiDir(), fileBaseName, ".mediawiki");
    Writer fileWriter = SpecIOUtility.createWriter(wiki);
    MediawikiWriter w = new MediawikiWriter(fileWriter, section, imagePaths);
    w.write(getTopHeadingLevel());
  }

  protected int getTopHeadingLevel() {
    return getConfiguration().getDefaultTopHeadingLevel();
  }

}
