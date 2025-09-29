/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import static org.eclipse.scout.rt.platform.util.IOUtility.urlTextToUrl;
import static org.eclipse.scout.rt.testing.platform.util.ScoutAssert.assertListEquals;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
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
      assertEquals("TestTestöäü", testContent);

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
      //noinspection ResultOfMethodCallIgnored
      tempFile.delete();
    }
    finally {
      IOUtility.deleteFile(tempFile);
    }
  }

  @Test
  public void testCreateNewTempFileInSpecificFolder() {
    File tempFolder = null;
    try {
      tempFolder = IOUtility.createTempDirectory("subDirectoryForTest");
      File file1 = IOUtility.createTempFile("fileInTopLevel", ".txt", null);
      File file2 = IOUtility.createTempFile("fileInSubFolder", ".txt", tempFolder, null);
      assertEquals(file2.getParentFile(), tempFolder);
      assertEquals(file2.getParentFile().getParentFile(), file1.getParentFile());
    }
    finally {
      IOUtility.deleteDirectory(tempFolder);
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
  @SuppressWarnings("ResultOfMethodCallIgnored")
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
  public void testUrlEncode() {
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
  public void testUrlDecode() {
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
  public void testUnzip() throws IOException {
    try (InputStream in = getClass().getClassLoader().getResourceAsStream(PLATFORM_PATH + "zip.zip")) {
      byte[] zipSlip = IOUtility.readBytes(in);
      Collection<BinaryResource> content = IOUtility.unzip(zipSlip, null);
      assertEquals(1, content.size());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnzipSlip() throws IOException {
    try (InputStream in = getClass().getClassLoader().getResourceAsStream(PLATFORM_PATH + FileUtilityTest.getZipSlipSampleFileName())) {
      byte[] zipSlip = IOUtility.readBytes(in);
      IOUtility.unzip(zipSlip, null);
    }
  }

  @Test
  public void testReadLines() throws FileNotFoundException {
    File tempFile = null;
    try {
      tempFile = createTextTempFile();

      List<String> readLines = IOUtility.readLines(tempFile, StandardCharsets.UTF_8.name());
      String[] readLinesArray = readLines.toArray(new String[0]);
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
      String[] readLinesArray = readLines.toArray(new String[0]);
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
  public void testReadLinesEmptyFile() {
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
  public void testReadBinaryResource() {
    URL url;
    BinaryResource br;

    // null case
    br = IOUtility.readBinaryResource(null);
    assertNull(br);

    // Standard case with automatic file name
    url = getClass().getClassLoader().getResource(PLATFORM_PATH + "ioUtilityTestUtf8.txt");
    br = IOUtility.readBinaryResource(url);
    assertNotNull(br);
    assertTrue(br.getContentLength() > 0);
    assertEquals("ioUtilityTestUtf8.txt", br.getFilename());

    // Standard case with custom file name
    br = IOUtility.readBinaryResource(url, "foo/bar.ext");
    assertNotNull(br);
    assertTrue(br.getContentLength() > 0);
    assertEquals("foo/bar.ext", br.getFilename());

    // Invalid URL
    url = UriUtility.toUrl("http://does.not.exist");
    try {
      br = IOUtility.readBinaryResource(url);
      fail("Missing expected exception");
    }
    catch (ProcessingException e) {
      // expected
    }

    // Directory
    url = getClass().getClassLoader().getResource(PLATFORM_PATH);
    br = IOUtility.readBinaryResource(url);
    assertNotNull(br);
    assertTrue(br.getContentLength() > 0);
    assertEquals("platform", br.getFilename());

    // URL to non-existing file (url will be null, so this is essentially the "null case" from above)
    url = getClass().getClassLoader().getResource(PLATFORM_PATH + "thisFileDoesNotExist.txt");
    br = IOUtility.readBinaryResource(url);
    assertNull(br);
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
      List<String> expectedLines = new ArrayList<>();
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
      List<String> expectedLines = Arrays.asList(LINES);
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
      List<String> expectedLines = new ArrayList<>();
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
  public void testRemoveByteOrderMark() {
    final byte[] UTF8_BOM = new byte[]{(byte) 0xef, (byte) 0xbb, (byte) 0xbf};
    final byte[] UTF16BE_BOM = new byte[]{(byte) 0xfe, (byte) 0xff};
    final byte[] UTF16LE_BOM = new byte[]{(byte) 0xff, (byte) 0xfe};
    final byte[] UTF32BE_BOM = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xfe, (byte) 0xff};
    final byte[] UTF32LE_BOM = new byte[]{(byte) 0xff, (byte) 0xfe, (byte) 0x00, (byte) 0x00};

    List<byte[]> bomsToTest = new LinkedList<>();
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
      //noinspection ResultOfMethodCallIgnored
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
  public void testReadStringFromStream1() {
    String expected = "0123äöü";
    String actual = IOUtility.readString(newInputStream(expected.getBytes(StandardCharsets.UTF_8)), "UTF-8");
    assertEquals(expected, actual);
  }

  @Test
  public void testReadStringFromStream2() {
    String data = "0123äöü";
    String expected = data.substring(0, 6);
    String actual = IOUtility.readString(newInputStream(data.getBytes(StandardCharsets.UTF_8)), "UTF-8", 6);
    assertEquals(expected, actual);
  }

  @Test
  public void testReadString() {
    String expected = "0123";
    String actual = IOUtility.readString(newReader(expected));
    assertEquals(expected, actual);
  }

  @Test
  public void testReadStringLengthToSmall() {
    String data = "0123";
    String expected = data.substring(0, 3);
    String actual = IOUtility.readString(newReader(data), 3);
    assertEquals(expected, actual);
  }

  @Test
  public void testReadStringSmallReader() {
    String data = "0123";
    String expected = data;
    String actual = IOUtility.readString(newReader(data), 10);
    assertEquals(expected, actual);
  }

  @Test
  public void testReadStringEmptyString() {
    String data = "";
    String expected = "";
    String actual = IOUtility.readString(newReader(data), 10);
    assertEquals(expected, actual);
  }

  @Test
  public void testReadStringZeroLength() {
    String data = "0123";
    String expected = "";
    String actual = IOUtility.readString(newReader(data), 0);
    assertEquals(expected, actual);
  }

  @Test
  public void testReadStringSameLength() {
    String data = "0123";
    String expected = data;
    String actual = IOUtility.readString(newReader(data), 4);
    assertEquals(expected, actual);
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
  public void testWriteStringToStream() {
    String expected = "0123äöü";
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOUtility.writeString(out, "UTF-8", expected);
    String actual = out.toString(StandardCharsets.UTF_8);
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
  @SuppressWarnings("HttpUrlsUsage")
  public void testUrlTextToUrl() throws URISyntaxException, MalformedURLException {
    assertNull(urlTextToUrl(null));
    assertNull(urlTextToUrl(""));
    assertNull(urlTextToUrl("|"));
    assertEquals(new URI("https://testurl").toURL(), urlTextToUrl("testurl"));
    assertEquals(new URI("https://eclipsescout.github.io/").toURL(), urlTextToUrl("https://eclipsescout.github.io/"));
    assertEquals(new URI("http://eclipsescout.github.io/").toURL(), urlTextToUrl("http://eclipsescout.github.io/"));
    assertEquals(new URI("mailto:test@scout.github.io").toURL(), urlTextToUrl("test@scout.github.io"));
  }
}
