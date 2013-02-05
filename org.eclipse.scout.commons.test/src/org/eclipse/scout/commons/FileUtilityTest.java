/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Merkli - initial API and implementation
 *     Stephan Leicht Vogt - Correction for tycho surefire testing
 ******************************************************************************/
package org.eclipse.scout.commons;

import java.io.File;
import java.io.InputStream;

import org.eclipse.scout.commons.utility.TestUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit tests for {@link FileUtility}
 * 
 * @since 3.9.0
 */
public class FileUtilityTest extends Assert {

  @Test
  public void testIsZipFile() throws Exception {
    File zipFile = null;
    File noZipFile = null;
    try {
      zipFile = getFile("/zip.zip");
      noZipFile = getFile("/nozip.zip");
      Assert.assertTrue("zip.zip is not a zip file", FileUtility.isZipFile(zipFile));
      Assert.assertFalse("nozip.zip is a zip file", FileUtility.isZipFile(noZipFile));
    }
    finally {
      TestUtility.deleteTempFile(zipFile);
      TestUtility.deleteTempFile(noZipFile);
    }
  }

  private File getFile(String fileName) {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
    return TestUtility.createTempFileFromResource(inputStream);
  }
}
