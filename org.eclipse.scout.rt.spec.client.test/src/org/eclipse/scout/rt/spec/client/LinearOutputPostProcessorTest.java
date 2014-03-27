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

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.spec.client.config.SpecFileConfig;
import org.eclipse.scout.rt.testing.commons.ScoutAssert;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link LinearOutputPostProcessor}
 */
public class LinearOutputPostProcessorTest {

  private static final String TEST_DIR = "resources" + File.separator + "spec" + File.separator + "mediawiki";
  private SpecFileConfig m_originalConfig;

  @Before
  public void setup() {
    m_originalConfig = SpecIOUtility.getSpecFileConfigInstance();
    SpecIOUtility.setSpecFileConfig(new SpecFileConfig("org.eclipse.scout.rt.spec.client.test"));
  }

  @After
  public void tearDown() throws ProcessingException {
    IOUtility.deleteDirectory(SpecIOUtility.getSpecFileConfigInstance().getSpecDir());
    SpecIOUtility.setSpecFileConfig(m_originalConfig);
  }

  @Test
  public void testPrefixAnchorsAndLinks() throws ProcessingException {
    File destFile = copyFile(TEST_DIR + File.separator + "LoremIpsum1.mediawiki");
    File expectedFile = copyFile(TEST_DIR + File.separator + "expectedAfterLinearOutputPostProcessorLoremIpsum1.mediawiki");

    new LinearOutputPostProcessor(null).prefixAnchorsAndLinks(destFile);
    ScoutAssert.assertTextFileEquals(expectedFile, destFile, "UTF-8");
  }

  @Test
  public void testFindFileForConfigEntry() throws ProcessingException {
    copyFile(TEST_DIR + File.separator + "LoppTestForm_1c1ce9e7-7216-4f01-9881-4786fa87e06c.mediawiki");
    copyFile(TEST_DIR + File.separator + "LoppTestFormExtended_71f9de60-604d-440e-8b91-2ad6a8c6f12d.mediawiki");
    copyFile(TEST_DIR + File.separator + "LoremIpsum1.mediawiki");
    LinearOutputPostProcessor lopp = new LinearOutputPostProcessor(null);

    Assert.assertNull("Expected null for non existing file.", lopp.findFileForConfigEntry("DoesNotExist"));

    testFindFileForConfigEntry(lopp, "LoremIpsum1", "LoremIpsum1.mediawiki");
    testFindFileForConfigEntry(lopp, "LoppTestForm", "LoppTestForm_1c1ce9e7-7216-4f01-9881-4786fa87e06c.mediawiki");
    testFindFileForConfigEntry(lopp, "org.eclipse.scout.rt.spec.client.LinearOutputPostProcessorTest$LoppTestForm", "LoppTestForm_1c1ce9e7-7216-4f01-9881-4786fa87e06c.mediawiki");
    testFindFileForConfigEntry(lopp, "LoppTestFormToExtend", "LoppTestFormExtended_71f9de60-604d-440e-8b91-2ad6a8c6f12d.mediawiki");
    testFindFileForConfigEntry(lopp, "org.eclipse.scout.rt.spec.client.LinearOutputPostProcessorTest$LoppTestFormToExtend", "LoppTestFormExtended_71f9de60-604d-440e-8b91-2ad6a8c6f12d.mediawiki");

  }

  private void testFindFileForConfigEntry(LinearOutputPostProcessor lopp, String configEntry, String expectedFileName) throws ProcessingException {
    File file = lopp.findFileForConfigEntry(configEntry);
    Assert.assertEquals("config entry was not resolved as expected", expectedFileName, file.getName());
    Assert.assertTrue("resolved does not exist", file.exists());
  }

  private File copyFile(String relativeSourcePath) throws ProcessingException {
    File expectedFile = new File(SpecIOUtility.getSpecFileConfigInstance().getMediawikiDir(), new File(relativeSourcePath).getName());
    SpecIOUtility.copyFile(SpecIOUtility.getSpecFileConfigInstance().getBundle(), relativeSourcePath, expectedFile);
    return expectedFile;
  }

  @ClassId("1c1ce9e7-7216-4f01-9881-4786fa87e06c")
  class LoppTestForm extends AbstractForm {
    public LoppTestForm() throws ProcessingException {
      super();
    }
  }

  @ClassId("a24a5c92-c10a-4a26-a3ce-45257f9c295f")
  class LoppTestFormToExtend extends AbstractForm {
    public LoppTestFormToExtend() throws ProcessingException {
      super();
    }
  }

  @ClassId("71f9de60-604d-440e-8b91-2ad6a8c6f12d")
  class LoppTestFormExtended extends LoppTestFormToExtend {
    public LoppTestFormExtended() throws ProcessingException {
      super();
    }
  }

}
