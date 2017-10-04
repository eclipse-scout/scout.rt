/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util;

import static org.eclipse.scout.rt.testing.platform.util.ScoutAssert.assertListEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
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
  private static final String PLATFORM_PATH = "org/eclipse/scout/rt/platform/";

  @Test
  public void testGetContentInEncoding() {
    File utf8File = null;
    File ansiFile = null;
    try {
      utf8File = createTempFile("ioUtilityTestUtf8.txt");
      ansiFile = createTempFile("ioUtilityTestAnsi.txt");

      String testContent = IOUtility.getContentInEncoding(utf8File.getPath(), StandardCharsets.UTF_8.name());
      assertEquals(testContent, "TestTestöäü");

      testContent = IOUtility.getContentInEncoding(ansiFile.getPath(), StandardCharsets.UTF_8.name());
      assertFalse("content is correct", StringUtility.equalsIgnoreCase(testContent, "TestTestöäü"));
    }
    finally {
      IOUtility.deleteFile(utf8File);
      IOUtility.deleteFile(ansiFile);
    }
  }

  @SuppressWarnings("resource")
  private File createTempFile(String name) {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PLATFORM_PATH + name);
    return IOUtility.createTempFile(inputStream, "temp", "zip");
  }

  private byte[] readFile(File file) throws Throwable {
    RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
    byte[] fileContent = new byte[(int) randomAccessFile.length()];
    randomAccessFile.read(fileContent);
    randomAccessFile.close();
    return fileContent;
  }

  @Test
  public void testCreateNewTempFile() throws Throwable {
    File tempFile = null;
    try {
      tempFile = IOUtility.createTempFile(FILENAME + EXTENSION, CONTENT);
      assertTrue(tempFile.getName().endsWith(FILENAME + EXTENSION));
      assertArrayEquals(CONTENT, readFile(tempFile));
    }
    finally {
      IOUtility.deleteFile(tempFile);
    }

    try {
      tempFile = IOUtility.createTempFile(FILENAME, EXTENSION, CONTENT);
      assertTrue(tempFile.getName().startsWith(FILENAME));
      assertTrue(tempFile.getName().endsWith(EXTENSION));
      assertArrayEquals(CONTENT, readFile(tempFile));
      tempFile.delete();
    }
    finally {
      IOUtility.deleteFile(tempFile);
    }
  }

  @Test
  public void testCreateNewTempFileNoContent() throws Throwable {
    File tempFile = null;
    try {
      tempFile = IOUtility.createTempFile(FILENAME + EXTENSION, null);
      assertTrue(tempFile.getName().endsWith(FILENAME + EXTENSION));
      assertArrayEquals(new byte[]{}, readFile(tempFile));
    }
    finally {
      IOUtility.deleteFile(tempFile);
    }

    try {
      tempFile = IOUtility.createTempFile(FILENAME, EXTENSION, null);
      assertTrue(tempFile.getName().startsWith(FILENAME));
      assertTrue(tempFile.getName().endsWith(EXTENSION));
      assertArrayEquals(new byte[]{}, readFile(tempFile));
    }
    finally {
      IOUtility.deleteFile(tempFile);
    }
  }

  @Test
  public void testCreateNewTempFileEmptyFilename() throws Throwable {
    File tempFile = null;
    try {
      tempFile = IOUtility.createTempFile("", CONTENT);
      assertTrue(tempFile.getName().endsWith(".tmp"));
      assertArrayEquals(CONTENT, readFile(tempFile));
    }
    finally {
      IOUtility.deleteFile(tempFile);
    }
  }

  @Test
  public void testCreateNewTempFileNullParameter() throws Throwable {
    File tempFile = null;
    try {
      tempFile = IOUtility.createTempFile(null, CONTENT);
      assertTrue(tempFile.getName().endsWith(".tmp"));
      assertArrayEquals(CONTENT, readFile(tempFile));
    }
    finally {
      IOUtility.deleteFile(tempFile);
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
    try {
      File tempDirWithSubs = new File(tempDir, "sub" + File.separator + "sub" + File.separator + "sub");
      tempFile.delete();
      if (!tempDirWithSubs.exists()) {
        tempDirWithSubs.mkdirs();
      }
      tempFile = new File(tempDirWithSubs.getParent(), "tempFile.tmp");
      tempFile.createNewFile();
      assertTrue("Temp dir was not successfully created.", tempDir.exists());
      assertTrue("Temp file was not successfully created.", tempFile.exists());
    }
    finally {
      boolean deleted = IOUtility.deleteDirectory(tempDir);
      assertTrue(deleted);
      assertFalse("Temp dir was not deleted.", tempDir.exists());
      assertFalse("Temp file was not deleted.", tempFile.exists());
    }
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
  public void testReadLines() throws FileNotFoundException {
    File tempFile = null;
    try {
      tempFile = createTextTempFile();

      List<String> readLines = IOUtility.readLines(tempFile, StandardCharsets.UTF_8.name());
      String[] readLinesArray = readLines.toArray(new String[readLines.size()]);
      assertArrayEquals("arrays with read lines not as expected", LINES, readLinesArray);
    }
    finally {
      IOUtility.deleteFile(tempFile);
    }
  }

  @Test
  public void testReadLinesUTF8() throws IOException {
    File tempFile = null;
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("org/eclipse/scout/rt/platform/ioUtilityTestUtf8.txt")) {
      tempFile = IOUtility.createTempFile(inputStream, "temp", "zip");

      List<String> readLines = IOUtility.readLines(tempFile, StandardCharsets.UTF_8.name());
      String[] readLinesArray = readLines.toArray(new String[readLines.size()]);
      assertTrue(StringUtility.equalsIgnoreCase(readLinesArray[0], "TestTestöäü"));
    }
    finally {
      IOUtility.deleteFile(tempFile);
    }
  }

  private File createTextTempFile() throws FileNotFoundException {
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
  public void testReadLinesEmptyFile() throws FileNotFoundException {
    File tempFile = null;
    try {
      tempFile = IOUtility.createTempFile(FILENAME, EXTENSION, null);
      List<String> readLines = IOUtility.readLines(tempFile, StandardCharsets.UTF_8.name());
      assertTrue("Expected an empty list when reading an empty file.", readLines.isEmpty());
    }
    finally {
      IOUtility.deleteFile(tempFile);
    }
  }

  @Test
  public void testReadLinesNonExistingFile() {
    try {
      IOUtility.readLines(new File("doesNotExist"), StandardCharsets.UTF_8.name());
      fail("Exptected a ProcessingException for non existing file.");
    }
    catch (ProcessingException expected) {
    }
  }

  @Test
  public void testAppendFile() throws FileNotFoundException {
    File tempFile = null;
    File tempFile2 = null;
    PrintWriter pw = null;
    try {
      tempFile = createTextTempFile();
      tempFile2 = createTextTempFile();

      pw = new PrintWriter(new FileOutputStream(tempFile, true));
      IOUtility.appendFile(pw, tempFile2);
      pw.close();

      List<String> readLines = IOUtility.readLines(tempFile, StandardCharsets.UTF_8.name());
      List<String> expectedLines = new ArrayList<String>();
      expectedLines.addAll(Arrays.asList(LINES));
      expectedLines.addAll(Arrays.asList(LINES));
      assertListEquals(expectedLines, readLines);
    }
    finally {
      IOUtility.deleteFile(tempFile);
      IOUtility.deleteFile(tempFile2);
    }
  }

  @Test
  public void testAppendEmptyFile() throws FileNotFoundException {
    File tempFile = null;
    File tempFile2 = null;
    PrintWriter pw = null;
    try {
      tempFile = createTextTempFile();
      tempFile2 = IOUtility.createTempFile(FILENAME, EXTENSION, null);

      pw = new PrintWriter(new FileOutputStream(tempFile, true));
      IOUtility.appendFile(pw, tempFile2);
      pw.close();

      List<String> readLines = IOUtility.readLines(tempFile, StandardCharsets.UTF_8.name());
      List<String> expectedLines = new ArrayList<String>();
      expectedLines.addAll(Arrays.asList(LINES));
      assertListEquals(expectedLines, readLines);
    }
    finally {
      IOUtility.deleteFile(tempFile);
      IOUtility.deleteFile(tempFile2);
    }
  }

  @Test
  public void testAppendNonExistingFile() throws FileNotFoundException {
    File tempFile = null;
    File tempFile2 = new File("doesNotExist");
    PrintWriter pw = null;
    try {
      tempFile = createTextTempFile();
      pw = new PrintWriter(new FileOutputStream(tempFile, true));
      try {
        IOUtility.appendFile(pw, tempFile2);
        fail("Exptected a ProcessingException for non existing file.");
      }
      catch (ProcessingException expected) {
      }
      finally {
        pw.close();
      }
    }
    finally {
      IOUtility.deleteFile(tempFile);
    }
  }

  @Test
  public void testAppendSameFile() throws FileNotFoundException {
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
      List<String> readLines = IOUtility.readLines(tempFile, StandardCharsets.UTF_8.name());
      // expect 3x original content
      assertListEquals(expectedLines, readLines);

      IOUtility.appendFile(pw, tempFile);
      pw.close();

      expectedLines.addAll(Arrays.asList(LINES));
      expectedLines.addAll(Arrays.asList(LINES));
      expectedLines.addAll(Arrays.asList(LINES));
      readLines = IOUtility.readLines(tempFile, StandardCharsets.UTF_8.name());
      // expect 6x original content
      assertListEquals(expectedLines, readLines);
    }
    finally {
      if (pw != null) {
        pw.close();
      }
      IOUtility.deleteFile(tempFile);
    }
  }

  @Test
  public void testRemoveByteOrderMark() throws Exception {
    final byte[] UTF8_BOM = new byte[]{(byte) 0xef, (byte) 0xbb, (byte) 0xbf};
    final byte[] UTF16BE_BOM = new byte[]{(byte) 0xfe, (byte) 0xff};
    final byte[] UTF16LE_BOM = new byte[]{(byte) 0xff, (byte) 0xfe};
    final byte[] UTF32BE_BOM = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xfe, (byte) 0xff};
    final byte[] UTF32LE_BOM = new byte[]{(byte) 0xff, (byte) 0xfe, (byte) 0x00, (byte) 0x00};

    List<byte[]> bomsToTest = new LinkedList<byte[]>();
    bomsToTest.add(UTF8_BOM);
    bomsToTest.add(UTF16BE_BOM);
    bomsToTest.add(UTF16LE_BOM);
    bomsToTest.add(UTF32BE_BOM);
    bomsToTest.add(UTF32LE_BOM);

    final byte[] lorem = "lorem".getBytes();

    String filename = "temp.txt";
    File file;

    for (byte[] bom : bomsToTest) {
      file = IOUtility.createTempFile(filename, mergeArrays(bom, lorem));
      assertArrayEquals(lorem, IOUtility.removeByteOrderMark(IOUtility.getContent(file.getPath())));
      file.delete();
    }

    assertNull(IOUtility.removeByteOrderMark(null));
    assertArrayEquals(new byte[]{(byte) 0xef, (byte) 0xbb}, IOUtility.removeByteOrderMark(new byte[]{(byte) 0xef, (byte) 0xbb}));

  }

  private byte[] mergeArrays(byte[] a, byte[] b) {
    byte[] combined = new byte[a.length + b.length];

    System.arraycopy(a, 0, combined, 0, a.length);
    System.arraycopy(b, 0, combined, a.length, b.length);
    return combined;
  }

  @Test
  public void testReadBytes() {
    byte[] expected = new byte[]{0, 1, 2, 3};
    byte[] actual = IOUtility.readBytes(newInputStream(expected));
    assertArrayEquals(expected, actual);
  }

  @Test
  public void testReadBytesLengthToSmall() {
    byte[] data = new byte[]{0, 1, 2, 3};
    byte[] expected = Arrays.copyOfRange(data, 0, 3);
    byte[] actual = IOUtility.readBytes(newInputStream(data), 3);
    assertArrayEquals(expected, actual);
  }

  @Test
  public void testReadBytesLengthToBig() {
    byte[] data = new byte[]{0, 1, 2, 3};
    byte[] actual = IOUtility.readBytes(newInputStream(data), 10);
    assertArrayEquals(data, actual);
  }

  @Test
  public void testReadStringFromStream1() throws Exception {
    String expected = "0123äöü";
    String actual = IOUtility.readString(newInputStream(expected.getBytes("UTF-8")), "UTF-8");
    assertEquals(expected, actual);
  }

  @Test
  public void testReadStringFromStream2() throws Exception {
    String data = "0123äöü";
    String expected = data.substring(0, 6);
    String actual = IOUtility.readString(newInputStream(data.getBytes("UTF-8")), "UTF-8", 6);
    assertEquals(expected, actual);
  }

  @Test
  public void testReadString() {
    String expected = "0123";
    String actual = IOUtility.readString(newReader(expected));
    assertEquals(expected, actual);
  }

  @Test
  public void testReadStringLenthToSmall() {
    String data = "0123";
    String expected = data.substring(0, 3);
    String actual = IOUtility.readString(newReader(data), 3);
    assertEquals(expected, actual);
  }

  @Test
  public void testReadStringLenthToBig() {
    String data = "0123";
    String actual = IOUtility.readString(newReader(data), 10);
    assertEquals(data, actual);
  }

  private InputStream newInputStream(byte[] bytes) {
    return new ByteArrayInputStream(bytes);
  }

  private Reader newReader(String s) {
    return new StringReader(s);
  }

  @Test
  public void testWriteBytes() {
    byte[] expected = new byte[]{0, 1, 2, 3};
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOUtility.writeBytes(out, expected);
    byte[] actual = out.toByteArray();
    assertArrayEquals(expected, actual);
  }

  @Test
  public void testWriteStringToStream() throws Exception {
    String expected = "0123äöü";
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOUtility.writeString(out, "UTF-8", expected);
    String actual = new String(out.toByteArray(), "UTF-8");
    assertEquals(expected, actual);
  }

  @Test
  public void testWriteString() {
    String expected = "0123";
    StringWriter out = new StringWriter();
    IOUtility.writeString(out, expected);
    String actual = out.toString();
    assertEquals(expected, actual);
  }

  @Test
  public void testResourceTryNull() throws Exception {
    try (InputStream res = (System.currentTimeMillis() == 0 ? new ByteArrayInputStream(new byte[15]) : null)) {
      if (res != null) {
        System.out.println("null resource: " + res);
      }
    }
  }

  @Test
  public void testResourceTryError1() throws Exception {
    try (InputStream res = new FileInputStream("foo/bar/test")) {
      if (res != null) {
        System.out.println("null resource: " + res);
      }
    }
    catch (Exception ex) {
      assertEquals(ex.getClass(), java.io.FileNotFoundException.class);
      return;
    }
    fail("should not go here");
  }

  @Test
  public void testResourceTryError2() throws Exception {
    try (InputStream res = null) {
      @SuppressWarnings({"resource", "unused"})
      InputStream tmp = new FileInputStream("foo/bar/test");
    }
    catch (Exception ex) {
      assertEquals(ex.getClass(), java.io.FileNotFoundException.class);
      return;
    }
    fail("should not go here");
  }
}
