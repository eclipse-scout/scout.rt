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
import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.spec.client.config.SpecFileConfig;
import org.junit.Test;

/**
 * Tests for {@link SpecIOUtility}
 */
public class SpecIOUtilityTest {
  private static final String TEST_DIR = "resources/spec/mediawiki";
  private static final String TEST_FILE_PATH = TEST_DIR + "/CC.mediawiki";

  /**
   * Test for {@link SpecIOUtility#copyFile(org.osgi.framework.Bundle, String, java.io.File)}
   * 
   * @throws ProcessingException
   */
  @Test
  public void testCopyFile() throws ProcessingException {
    SpecFileConfig config = new SpecFileConfig("org.eclipse.scout.rt.spec.client.test");
    File specDir = config.getSpecDir();
    specDir.mkdirs();
    SpecIOUtility.copyFile(config.getBundle(), TEST_FILE_PATH, new File(specDir, "out.mediawiki"));
  }

  /**
   * Copy all files
   */
  @Test
  public void testCopyAll2() throws ProcessingException, URISyntaxException, IOException {
    SpecFileConfig config = new SpecFileConfig("org.eclipse.scout.rt.spec.client.test");
    File f = new File(FileLocator.toFileURL(config.getBundle().getEntry("/resources/spec/mediawiki")).toURI());
    SpecIOUtility.copyAll(f, config.getSpecDir().getParentFile(), null);
  }

}
