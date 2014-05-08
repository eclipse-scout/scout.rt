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

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.spec.client.config.SpecFileConfig;
import org.eclipse.scout.rt.spec.client.utility.SpecIOUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * Tests for {@link SpecIOUtility}
 */
public class SpecIOUtilityTest {

  private static final String TEST_DIR = "resources" + File.separator + "spec" + File.separator + "mediawiki";
  private SpecFileConfig m_config;

  @Before
  public void setup() {
    m_config = new SpecFileConfig("org.eclipse.scout.rt.spec.client.test");
  }

  @After
  public void tearDown() throws ProcessingException {
    IOUtility.deleteDirectory(m_config.getSpecDir());
  }

  /**
   * Test for {@link SpecIOUtility#copyFile(org.osgi.framework.Bundle, String, java.io.File)}
   */
  @Test
  public void testCopyFileSource() throws ProcessingException, IOException {
    testCopyFile(m_config.getBundle(), TEST_DIR + File.separator + "LoremIpsum1.mediawiki", "Lorem ipsum", true);
  }

  /**
   * Test for {@link SpecIOUtility#copyFile(org.osgi.framework.Bundle, String, java.io.File)}
   */
  @Test
  public void testCopyFileSourcePathDoesNotExist() throws ProcessingException, IOException {
    testCopyFile(m_config.getBundle(), TEST_DIR + File.separator + "LoremIpsum1.mediawiki", "Lorem ipsum", false);
  }

  /**
   * Test for {@link SpecIOUtility#copyFile(org.osgi.framework.Bundle, String, java.io.File)}
   */
  @Test
  public void testCopyFileBinary() throws ProcessingException, IOException {
    Bundle bundle = Platform.getBundle("org.eclipse.core.runtime");
    String path = "org" + File.separator + "eclipse" + File.separator + "core" + File.separator + "runtime" + File.separator + "Platform.class";
    String expectedContentPart = "Platform";
    testCopyFile(bundle, path, expectedContentPart, true);
  }

  private void testCopyFile(Bundle bundle, String path, String expectedContentPart, boolean createDirs) throws ProcessingException, FileNotFoundException, IOException {
    File specDir = m_config.getSpecDir();
    if (createDirs) {
      specDir.mkdirs();
    }
    File destFile = new File(specDir, new File(path).getName());
    SpecIOUtility.copyFile(bundle, path, destFile);
    assertTrue("File was not copied as expected", destFile.exists());
    BufferedReader reader = null;
    boolean contentFound = false;
    String line;
    try {
      reader = new BufferedReader(new FileReader(destFile));
      do {
        line = reader.readLine();
        if (line.contains(expectedContentPart)) {
          contentFound = true;
          break;
        }
      }
      while (line != null);
    }
    finally {
      if (reader != null) {
        reader.close();
      }
    }
    assertTrue("Content of copied file not as expected.", contentFound);
  }

  /**
   * Test for {@link SpecIOUtility#listFiles(org.osgi.framework.Bundle, String, FilenameFilter)}
   */
  @Test
  public void testListFilesBinary() throws ProcessingException {
    List<String> fileList = SpecIOUtility.listFiles(Platform.getBundle("org.eclipse.core.runtime"), "org" + File.separator + "eclipse" + File.separator + "core" + File.separator + "runtime", new AnyFileFilter());
    assertTrue("Expected 'Platform.class' in fileList", fileList.contains("Platform.class"));
  }

  /**
   * Test for {@link SpecIOUtility#listFiles(org.osgi.framework.Bundle, String, FilenameFilter)}
   */
  @Test
  public void testListFilesSource() throws ProcessingException {
    List<String> fileList = SpecIOUtility.listFiles(m_config.getBundle(), TEST_DIR, new LoremIpsum12Filter());
    assertTrue("Expected 2 files in list. But fileList.size() returned [" + fileList.size() + "]", fileList.size() == 2);
    assertTrue("Expected 'LoremIpsum1.mediawiki' in fileList.", fileList.contains("LoremIpsum1.mediawiki"));
    assertTrue("Expected 'LoremIpsum2.mediawiki' in fileList.", fileList.contains("LoremIpsum2.mediawiki"));
  }

  private class AnyFileFilter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String name) {
      return true;
    }
  }

  private class LoremIpsum12Filter implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {
      return "LoremIpsum1.mediawiki".equals(name) || "LoremIpsum2.mediawiki".equals(name);
    }

  }

}
