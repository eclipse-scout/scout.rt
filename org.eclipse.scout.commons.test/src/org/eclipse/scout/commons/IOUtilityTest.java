/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lucien Hansen - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.utility.TestUtility;
import org.junit.Test;

/**
 * JUnit tests for {@link IOUtility}
 * 
 * @since 3.9.1
 */
public class IOUtilityTest {

  @Test
  public void testGetContentInEncoding() throws ProcessingException {
    File utf8File = null;
    File ansiFile = null;
    try {
      utf8File = TestUtility.createTempFileFromFilename("ioUtilityTestUtf8.txt", getClass());
      ansiFile = TestUtility.createTempFileFromFilename("ioUtilityTestAnsi.txt", getClass());

      String testContent = IOUtility.getContentInEncoding(utf8File.getPath(), "UTF-8");
      assertTrue("content is correct", StringUtility.equalsIgnoreCase(testContent, "TestTestöäü"));

      testContent = IOUtility.getContentInEncoding(ansiFile.getPath(), "UTF-8");
      assertFalse("content is correct", StringUtility.equalsIgnoreCase(testContent, "TestTestöäü"));
    }
    finally {
      TestUtility.deleteTempFile(utf8File);
      TestUtility.deleteTempFile(ansiFile);
    }
  }
}
