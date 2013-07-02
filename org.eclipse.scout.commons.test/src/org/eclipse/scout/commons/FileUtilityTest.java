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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.scout.commons.utility.TestUtility;
import org.junit.Test;

/**
 * JUnit tests for {@link FileUtility}
 * 
 * @since 3.9.0
 */
public class FileUtilityTest {

  @Test
  public void testIsZipFile() throws Exception {
    File zipFile = null;
    File noZipFile = null;
    try {
      zipFile = TestUtility.createTempFileFromFilename("/zip.zip", getClass());
      noZipFile = TestUtility.createTempFileFromFilename("/nozip.zip", getClass());
      assertTrue("zip.zip is not a zip file", FileUtility.isZipFile(zipFile));
      assertFalse("nozip.zip is a zip file", FileUtility.isZipFile(noZipFile));
    }
    finally {
      TestUtility.deleteTempFile(zipFile);
      TestUtility.deleteTempFile(noZipFile);
    }
  }
}
