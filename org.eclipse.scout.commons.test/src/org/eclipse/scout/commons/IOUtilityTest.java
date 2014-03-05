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

import static org.eclipse.scout.rt.testing.commons.ScoutAssert.assertListEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.utility.TestUtility;
import org.junit.Test;

/**
 * JUnit tests for {@link IOUtility}
 * 
 * @since 3.9.1
 */
public class IOUtilityTest {
  private static final String[] LINES = {"one", "two", "three", "lorem ipsum sit amet"};
  private static final byte[] CONTENT = new byte[]{1, 2, 3, 4};
  private static final String FILENAME = "myTempFile";
  private static final String EXTENSION = ".tmp";

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
    File tempFile = null;
    try {
      tempFile = IOUtility.createTempFile(FILENAME + EXTENSION, CONTENT);
      assertTrue(tempFile.getName().endsWith(FILENAME + EXTENSION));
      assertArrayEquals(CONTENT, readFile(tempFile));
    }
    finally {
      TestUtility.deleteTempFile(tempFile);
    }

    try {
      tempFile = IOUtility.createTempFile(FILENAME, EXTENSION, CONTENT);
      assertTrue(tempFile.getName().startsWith(FILENAME));
      assertTrue(tempFile.getName().endsWith(EXTENSION));
      assertArrayEquals(CONTENT, readFile(tempFile));
      tempFile.delete();
    }
    finally {
      TestUtility.deleteTempFile(tempFile);
    }
  }

  @Test
  public void testCreateNewTempFileNoContent() throws ProcessingException, Throwable {
    File tempFile = null;
    try {
      tempFile = IOUtility.createTempFile(FILENAME + EXTENSION, null);
      assertTrue(tempFile.getName().endsWith(FILENAME + EXTENSION));
      assertArrayEquals(new byte[]{}, readFile(tempFile));
    }
    finally {
      TestUtility.deleteTempFile(tempFile);
    }

    try {
      tempFile = IOUtility.createTempFile(FILENAME, EXTENSION, null);
      assertTrue(tempFile.getName().startsWith(FILENAME));
      assertTrue(tempFile.getName().endsWith(EXTENSION));
      assertArrayEquals(new byte[]{}, readFile(tempFile));
    }
    finally {
      TestUtility.deleteTempFile(tempFile);
    }
  }

  @Test
  public void testCreateNewTempFileEmptyFilename() throws ProcessingException, Throwable {
    File tempFile = null;
    try {
      tempFile = IOUtility.createTempFile("", CONTENT);
      assertTrue(tempFile.getName().endsWith(".tmp"));
      assertArrayEquals(CONTENT, readFile(tempFile));
    }
    finally {
      TestUtility.deleteTempFile(tempFile);
    }
  }

  @Test
  public void testCreateNewTempFileNullParameter() throws ProcessingException, Throwable {
    File tempFile = null;
    try {
      tempFile = IOUtility.createTempFile(null, CONTENT);
      assertTrue(tempFile.getName().endsWith(".tmp"));
      assertArrayEquals(CONTENT, readFile(tempFile));
    }
    finally {
      TestUtility.deleteTempFile(tempFile);
    }
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

  @Test
  public void testUrlEncode() throws Exception {
    assertEquals("www.google.com", IOUtility.urlEncode("www.google.com"));
    assertNull(IOUtility.urlEncode(null));
    assertEquals("", IOUtility.urlEncode(""));
    assertEquals("", IOUtility.urlEncode(" "));
    assertEquals("", IOUtility.urlEncode(" \n\t"));
    assertEquals("http%3A%2F%2Fwww.google.org", IOUtility.urlEncode("         http://www.google.org       "));
    assertEquals("a%20test%20%20with%20%20%20multiple%20%20%20%20spaces", IOUtility.urlEncode(" a test  with   multiple    spaces"));
    assertEquals("Expected UTF-8 charset", "%C3%B6%C3%A4%C3%BC%C3%A9%C3%A0%C3%A8", IOUtility.urlEncode("öäüéàè"));
  }

  @Test
  public void testUrlDecode() throws Exception {
    assertEquals("www.google.com", IOUtility.urlDecode("www.google.com"));
    assertNull(IOUtility.urlDecode(null));
    assertEquals("", IOUtility.urlDecode(""));
    assertEquals("", IOUtility.urlDecode(" "));
    assertEquals("", IOUtility.urlDecode(" \n\t"));
    assertEquals("http://www.google.org", IOUtility.urlDecode("         http%3A%2F%2Fwww.google.org       "));
    assertEquals("a test  with   multiple    spaces", IOUtility.urlDecode("a%20test%20%20with%20%20%20multiple%20%20%20%20spaces"));
    assertEquals("Expected UTF-8 charset", "öäüéàè", IOUtility.urlDecode("%C3%B6%C3%A4%C3%BC%C3%A9%C3%A0%C3%A8"));
  }

  @Test
  public void testReadLines() throws ProcessingException, FileNotFoundException {
    File tempFile = null;
    try {
      tempFile = createTextTempFile();

      List<String> readLines = IOUtility.readLines(tempFile);
      String[] readLinesArray = readLines.toArray(new String[readLines.size()]);
      assertArrayEquals("arrays with read lines not as expected", LINES, readLinesArray);
    }
    finally {
      TestUtility.deleteTempFile(tempFile);
    }
  }

  @Test
  public void testReadLinesUTF8() throws ProcessingException, FileNotFoundException {
    File tempFile = null;
    try {
      tempFile = TestUtility.createTempFileFromFilename("ioUtilityTestUtf8.txt", getClass());

      List<String> readLines = IOUtility.readLines(tempFile);
      String[] readLinesArray = readLines.toArray(new String[readLines.size()]);
      assertTrue(StringUtility.equalsIgnoreCase(readLinesArray[0], "TestTestöäü"));
    }
    finally {
      TestUtility.deleteTempFile(tempFile);
    }
  }

  private File createTextTempFile() throws ProcessingException, FileNotFoundException {
    File tempFile;
    tempFile = IOUtility.createTempFile(FILENAME, EXTENSION, null);
    PrintWriter printWriter = new PrintWriter(tempFile);
    for (String line : LINES) {
      printWriter.println(line);
    }
    printWriter.close();
    return tempFile;
  }

  @Test
  public void testReadLinesEmptyFile() throws ProcessingException, FileNotFoundException {
    File tempFile = null;
    try {
      tempFile = IOUtility.createTempFile(FILENAME, EXTENSION, null);
      List<String> readLines = IOUtility.readLines(tempFile);
      assertTrue("Expected an empty list when reading an empty file.", readLines.isEmpty());
    }
    finally {
      TestUtility.deleteTempFile(tempFile);
    }
  }

  @Test
  public void testReadLinesNonExistingFile() {
    boolean processingExceptionOccured = false;
    try {
      IOUtility.readLines(new File("doesNotExist"));
    }
    catch (ProcessingException e) {
      processingExceptionOccured = true;
    }
    assertTrue("Exptected a ProcessingException for non existing file.", processingExceptionOccured);
  }

  @Test
  public void testAppendFile() throws FileNotFoundException, ProcessingException {
    File tempFile = null;
    File tempFile2 = null;
    PrintWriter pw = null;
    try {
      tempFile = createTextTempFile();
      tempFile2 = createTextTempFile();

      pw = new PrintWriter(new FileOutputStream(tempFile, true));
      IOUtility.appendFile(pw, tempFile2);
      pw.close();

      List<String> readLines = IOUtility.readLines(tempFile);
      List<String> expectedLines = new ArrayList<String>();
      expectedLines.addAll(Arrays.asList(LINES));
      expectedLines.addAll(Arrays.asList(LINES));
      assertListEquals(expectedLines, readLines);
    }
    finally {
      TestUtility.deleteTempFile(tempFile);
      TestUtility.deleteTempFile(tempFile2);
    }
  }

  @Test
  public void testAppendEmptyFile() throws FileNotFoundException, ProcessingException {
    File tempFile = null;
    File tempFile2 = null;
    PrintWriter pw = null;
    try {
      tempFile = createTextTempFile();
      tempFile2 = IOUtility.createTempFile(FILENAME, EXTENSION, null);

      pw = new PrintWriter(new FileOutputStream(tempFile, true));
      IOUtility.appendFile(pw, tempFile2);
      pw.close();

      List<String> readLines = IOUtility.readLines(tempFile);
      List<String> expectedLines = new ArrayList<String>();
      expectedLines.addAll(Arrays.asList(LINES));
      assertListEquals(expectedLines, readLines);
    }
    finally {
      TestUtility.deleteTempFile(tempFile);
      TestUtility.deleteTempFile(tempFile2);
    }
  }

  @Test
  public void testAppendNonExistingFile() throws FileNotFoundException, ProcessingException {
    File tempFile = null;
    File tempFile2 = new File("doesNotExist");
    PrintWriter pw = null;
    try {
      tempFile = createTextTempFile();
      pw = new PrintWriter(new FileOutputStream(tempFile, true));
      boolean processingExceptionOccured = false;
      try {
        IOUtility.appendFile(pw, tempFile2);
      }
      catch (ProcessingException e) {
        processingExceptionOccured = true;
      }
      finally {
        pw.close();
      }
      assertTrue("Exptected a ProcessingException for non existing file.", processingExceptionOccured);

    }
    finally {
      TestUtility.deleteTempFile(tempFile);
    }
  }

  @Test
  public void testAppendSameFile() throws FileNotFoundException, ProcessingException {
    File tempFile = null;
    PrintWriter pw = null;
    try {
      tempFile = createTextTempFile();

      pw = new PrintWriter(new FileOutputStream(tempFile, true));
      IOUtility.appendFile(pw, tempFile);
      IOUtility.appendFile(pw, tempFile);
      pw.flush();
      List<String> expectedLines = new ArrayList<String>();
      expectedLines.addAll(Arrays.asList(LINES));
      expectedLines.addAll(Arrays.asList(LINES));
      expectedLines.addAll(Arrays.asList(LINES));
      List<String> readLines = IOUtility.readLines(tempFile);
      // expect 3x original content
      assertListEquals(expectedLines, readLines);

      IOUtility.appendFile(pw, tempFile);
      pw.close();

      expectedLines.addAll(Arrays.asList(LINES));
      expectedLines.addAll(Arrays.asList(LINES));
      expectedLines.addAll(Arrays.asList(LINES));
      readLines = IOUtility.readLines(tempFile);
      // expect 6x original content
      assertListEquals(expectedLines, readLines);
    }
    finally {
      if (pw != null) {
        pw.close();
      }
      TestUtility.deleteTempFile(tempFile);
    }
  }

}
