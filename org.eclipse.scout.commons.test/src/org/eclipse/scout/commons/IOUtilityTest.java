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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.utility.TestUtility;
import org.junit.Test;

/**
 * JUnit tests for {@link IOUtility}
 * 
 * @since 3.9.1
 */
public class IOUtilityTest {
  private static final byte[] content = new byte[]{1, 2, 3, 4};
  private static String filename = "myTempFile";
  private static String extension = ".tmp";

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

  private byte[] readFile(File file) throws Throwable {
    RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
    byte[] fileContent = new byte[(int) randomAccessFile.length()];
    randomAccessFile.read(fileContent);
    randomAccessFile.close();
    return fileContent;
  }

  @Test
  public void testCreateNewTempFile() throws ProcessingException, Throwable {
    File tempFile = IOUtility.createTempFile(filename + extension, content);
    assertTrue(tempFile.getName().endsWith(filename + extension));
    assertArrayEquals(content, readFile(tempFile));
    tempFile.delete();

    tempFile = IOUtility.createTempFile(filename, extension, content);
    assertTrue(tempFile.getName().startsWith(filename));
    assertTrue(tempFile.getName().endsWith(extension));
    assertArrayEquals(content, readFile(tempFile));
    tempFile.delete();
  }

  @Test
  public void testCreateNewTempFileNoContent() throws ProcessingException, Throwable {
    File tempFile = IOUtility.createTempFile(filename + extension, null);
    assertTrue(tempFile.getName().endsWith(filename + extension));
    assertArrayEquals(new byte[]{}, readFile(tempFile));
    tempFile.delete();

    tempFile = IOUtility.createTempFile(filename, extension, null);
    assertTrue(tempFile.getName().startsWith(filename));
    assertTrue(tempFile.getName().endsWith(extension));
    assertArrayEquals(new byte[]{}, readFile(tempFile));
    tempFile.delete();
  }

  @Test
  public void testCreateNewTempFileEmptyFilename() throws ProcessingException, Throwable {
    File tempFile = IOUtility.createTempFile("", content);
    assertTrue(tempFile.getName().endsWith(".tmp"));
    assertArrayEquals(content, readFile(tempFile));
    tempFile.delete();
  }

  @Test
  public void testCreateNewTempFileNullParameter() throws ProcessingException, Throwable {
    File tempFile = IOUtility.createTempFile(null, content);
    assertTrue(tempFile.getName().endsWith(".tmp"));
    assertArrayEquals(content, readFile(tempFile));
    tempFile.delete();
  }

  @Test
  public void testFileExtension() {
    assertEquals("temp", IOUtility.getFileExtension("Test.temp"));
    assertEquals("temp", IOUtility.getFileExtension("Test.xy.temp"));
    assertEquals("temp", IOUtility.getFileExtension(".temp"));
    assertEquals("", IOUtility.getFileExtension("Test."));
    assertEquals("", IOUtility.getFileExtension("."));
    assertNull(IOUtility.getFileExtension(""));
    assertNull(IOUtility.getFileExtension(null));
  }

  @Test
  public void testDeleteDirectory() throws IOException {
    File tempFile = File.createTempFile("tempFile", "tmp");
    File tempDir = new File(tempFile.getParent(), "FileUtilityTestTempDir");
    File tempDirWithSubs = new File(tempDir, "sub" + File.separator + "sub" + File.separator + "sub");
    tempFile.delete();
    if (!tempDir.exists()) {
      tempDirWithSubs.mkdirs();
    }
    tempFile = new File(tempDirWithSubs.getParent(), "tempFile.tmp");
    tempFile.createNewFile();
    assertTrue("Temp dir was not successfully created.", tempDir.exists());
    assertTrue("Temp file was not successfully created.", tempFile.exists());
    IOUtility.deleteDirectory(tempDir);
    assertFalse("Temp dir was not deleted.", tempDir.exists());
    assertFalse("Temp file was not deleted.", tempFile.exists());
  }
}
