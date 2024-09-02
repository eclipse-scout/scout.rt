/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit tests for {@link FileUtility}
 *
 * @since 3.9.0
 */
public class FileUtilityTest {

  private static final String TEXT_EXT = "txt";
  private static final String FILE_NAME = "fooBar.txt";
  private static final String FILE_NAME_MULTIPLE_DOTS = "foo.bar.txt";
  private static final String FILE_NAME_NO_EXT = "fooBar";
  private static final String PLATFORM_PATH = "org/eclipse/scout/rt/platform/";

  @Test
  public void testIsZipFile() throws Exception {
    File zipFile = null;
    File noZipFile = null;
    try {
      zipFile = createTempFile("zip.zip");
      noZipFile = createTempFile("nozip.zip");
      assertTrue("zip.zip is not a zip file", FileUtility.isZipFile(zipFile));
      assertFalse("nozip.zip is a zip file", FileUtility.isZipFile(noZipFile));
    }
    finally {
      IOUtility.deleteFile(zipFile);
      IOUtility.deleteFile(noZipFile);
    }
  }

  @Test
  public void testExtractArchive() throws IOException {
    extractZipToDir("zip.zip");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExtractArchiveZipSlip() throws IOException {
    String zipSlip;
    if ('/' == File.separatorChar) {
      zipSlip = "zip-slip.zip";
    }
    else {
      zipSlip = "zip-slip-win.zip";
    }
    extractZipToDir(zipSlip);
  }

  private void extractZipToDir(String zipName) throws IOException {
    Path target = Files.createTempDirectory("scoutExtractArchiveTest");
    File zipFile = createTempFile(zipName);
    try {
      FileUtility.extractArchive(zipFile, target.toFile());
      assertEquals(1, Files.list(target).count());
    }
    finally {
      IOUtility.deleteFile(zipFile);
      IOUtility.deleteDirectory(target.toFile());
    }
  }

  private File createTempFile(String name) throws IOException {
    try (InputStream in = getClass().getClassLoader().getResourceAsStream(PLATFORM_PATH + name)) {
      return IOUtility.createTempFile(in, "temp", "zip");
    }
  }

  @Test
  public void testGetFileExtension_String() {
    assertEquals(TEXT_EXT, FileUtility.getFileExtension(FILE_NAME));
    assertEquals(TEXT_EXT, FileUtility.getFileExtension(FILE_NAME_MULTIPLE_DOTS));
    assertNull(FileUtility.getFileExtension(FILE_NAME_NO_EXT));
    assertNull(FileUtility.getFileExtension((String) null));
  }

  @Test
  public void testGetFileExtension_File() {
    assertEquals(TEXT_EXT, FileUtility.getFileExtension(new File(FILE_NAME)));
    assertEquals(TEXT_EXT, FileUtility.getFileExtension(new File(FILE_NAME_MULTIPLE_DOTS)));
    assertNull(FileUtility.getFileExtension(new File(FILE_NAME_NO_EXT)));
    assertNull(FileUtility.getFileExtension((File) null));
  }

  @Test
  public void testGetFilenameParts_String() {
    String[] tmp = FileUtility.getFilenameParts(FILE_NAME);
    assertEquals("fooBar", tmp[0]);
    assertEquals(TEXT_EXT, tmp[1]);
    assertNull(FileUtility.getFilenameParts((String) null));
    // other tests already covered by testGetFileExtension_String
  }

  @Test
  public void testGetFilenameParts_File() {
    String[] tmp = FileUtility.getFilenameParts(new File(FILE_NAME));
    assertEquals("fooBar", tmp[0]);
    assertEquals(TEXT_EXT, tmp[1]);
    assertNull(FileUtility.getFilenameParts((File) null));
    // other tests already covered by testGetFileExtension_File
  }

  @Test
  public void testGetContentTypeForExtension_xml() {
    assertEquals("text/xml", FileUtility.getContentTypeForExtension("xml"));
  }

  @Test
  public void testGetContentTypeForExtension_XML() {
    assertEquals("text/xml", FileUtility.getContentTypeForExtension("XML"));
  }

  @Test
  public void testGetContentTypeForExtension_m4v() {
    assertEquals("application/octet-stream", FileUtility.getMimeType(Paths.get("m4v")));
  }

  /**
   * Test {@link FileUtility#toValidFilename(String)}.
   */
  @Test
  public void testToValidFilename() {
    // filename is only changed if necessary
    assertEquals("document.docx", FileUtility.toValidFilename("document.docx"));
    assertEquals("()+§#&~¨$¬€.docx", FileUtility.toValidFilename("\\/:()+§#&*?\"<>~¨$¬€|.docx"));
    assertEquals("愛", FileUtility.toValidFilename("愛"));
    assertEquals("☃𐒄𐐝𠂊.𠆡", FileUtility.toValidFilename("☃𐒄𐐝𠂊.𠆡"));
    assertEquals("♕  ☢  ☣  ☠  ☤  ♍  ☀  ♯   . t x t", FileUtility.toValidFilename(" ♕  ☢  ☣  ☠  ☤  ♍  ☀  ♯   . t x t "));
    assertEquals("♕☢☣☠☤.. . .♍☀♯", FileUtility.toValidFilename(".♕☢☣☠☤.. . .♍☀♯."));
    assertEquals("_", FileUtility.toValidFilename("*?:"));
    assertEquals("_", FileUtility.toValidFilename("   "));
    assertEquals("_", FileUtility.toValidFilename(" . "));
    assertEquals("_", FileUtility.toValidFilename("..."));
    assertEquals("_", FileUtility.toValidFilename(". ."));

    // tests from former CoreUtility.cleanStringForFileName
    assertEquals("_", FileUtility.toValidFilename(" "));
    assertEquals("_", FileUtility.toValidFilename("\t"));
    assertEquals("_", FileUtility.toValidFilename("\n"));
    assertEquals("_", FileUtility.toValidFilename("\n \t"));
    assertEquals("_", FileUtility.toValidFilename("/"));
    assertEquals("_", FileUtility.toValidFilename(":"));
    assertEquals("_", FileUtility.toValidFilename("*"));
    assertEquals("_", FileUtility.toValidFilename("\\"));
    assertEquals("_", FileUtility.toValidFilename("?"));
    assertEquals("_", FileUtility.toValidFilename("\""));
    assertEquals("_", FileUtility.toValidFilename("?"));
    assertEquals("_", FileUtility.toValidFilename("<"));
    assertEquals("_", FileUtility.toValidFilename(">"));
    assertEquals("_", FileUtility.toValidFilename("|"));
    assertEquals("test .doc", FileUtility.toValidFilename("test\n \t\\/:*?\"<>|.doc"));
    assertEquals(
        "someReallyLongName01234567890123456789.01234567890123456789.01234567890123456789.01234567890123456789.01234567890123456789.01234567890123456789.01234567890123456789.01234567890123456789.01234567890123456789.01234567890123456789.012345678901234567.doc",
        FileUtility
            .toValidFilename(
                "someReallyLongName01234567890123456789.01234567890123456789.01234567890123456789.01234567890123456789.01234567890123456789.01234567890123456789.01234567890123456789.01234567890123456789.01234567890123456789.01234567890123456789.01234567890123456789.01234567890123.doc"));

    assertEquals("_.txt", FileUtility.toValidFilename("...txt"));
    assertEquals("_.txt", FileUtility.toValidFilename("..txt"));
    assertEquals("_.txt", FileUtility.toValidFilename(" .txt"));
    assertEquals("_.txt", FileUtility.toValidFilename("  .txt"));
    assertEquals("_.txt", FileUtility.toValidFilename(" _.txt"));

  }

  @Test
  public void testZip() {
    BinaryResource expectedTextResource = new BinaryResource("text.txt", "some text".getBytes(UTF_8));
    // create empty remote file manually (utility will return null)
    BinaryResource expectedEmptyResource = new BinaryResource("empty_file.txt", new byte[0]);
    BinaryResource expectedDataResource = new BinaryResource("data.dat", new byte[]{12, 4, 23, 41, 2, 5, 72, 12, 3});

    BinaryResource zipResource = FileUtility.zip("archive.zip", CollectionUtility.arrayList(expectedTextResource, expectedEmptyResource, expectedDataResource));
    Assert.assertNotNull(zipResource);
    Assert.assertEquals("archive.zip", zipResource.getFilename());
    Assert.assertTrue(zipResource.getContent().length > 0);

    Collection<BinaryResource> resources = FileUtility.unzip(zipResource, null);
    Assert.assertNotNull(resources);
    Assert.assertEquals(3, resources.size());

    // assuming same file order as when zipping
    Iterator<BinaryResource> it = resources.iterator();

    BinaryResource actualTextResource = it.next();
    Assert.assertNotNull(actualTextResource);
    Assert.assertEquals(expectedTextResource.getFilename(), actualTextResource.getFilename());
    Assert.assertArrayEquals(expectedTextResource.getContent(), actualTextResource.getContent());

    BinaryResource actualEmptyResource = it.next();
    Assert.assertNotNull(actualEmptyResource);
    Assert.assertEquals(expectedEmptyResource.getFilename(), actualEmptyResource.getFilename());
    Assert.assertArrayEquals(expectedEmptyResource.getContent(), actualEmptyResource.getContent());

    BinaryResource actualDataResource = it.next();
    Assert.assertNotNull(actualDataResource);
    Assert.assertEquals(expectedDataResource.getFilename(), actualDataResource.getFilename());
    Assert.assertArrayEquals(expectedDataResource.getContent(), actualDataResource.getContent());
  }

  @Test
  public void testZipAvoidConflicts() {
    BinaryResource expectedEmptyResource1 = new BinaryResource("empty_file.txt", new byte[0]);
    BinaryResource expectedEmptyResource2 = new BinaryResource("empty_file.txt", new byte[0]);
    BinaryResource expectedEmptyResource3 = new BinaryResource("empty_file.txt", new byte[0]);

    BinaryResource zipResource = FileUtility.zip("archive.zip", CollectionUtility.arrayList(expectedEmptyResource1, expectedEmptyResource2, expectedEmptyResource3), true);
    Assert.assertNotNull(zipResource);
    Assert.assertEquals("archive.zip", zipResource.getFilename());
    Assert.assertTrue(zipResource.getContent().length > 0);

    Collection<BinaryResource> resources = FileUtility.unzip(zipResource, null);
    Assert.assertNotNull(resources);
    Assert.assertEquals(3, resources.size());

    Iterator<BinaryResource> it = resources.iterator();

    BinaryResource actualEmptyResource1 = it.next();
    Assert.assertNotNull(expectedEmptyResource1);
    Assert.assertEquals(FileUtility.getFileName(expectedEmptyResource1.getFilename(), false) + "(1)." + FileUtility.getFileExtension(expectedEmptyResource1.getFilename()), actualEmptyResource1.getFilename());
    Assert.assertArrayEquals(expectedEmptyResource1.getContent(), actualEmptyResource1.getContent());

    BinaryResource actualEmptyResource2 = it.next();
    Assert.assertNotNull(actualEmptyResource2);
    Assert.assertEquals(FileUtility.getFileName(expectedEmptyResource2.getFilename(), false) + "(2)." + FileUtility.getFileExtension(expectedEmptyResource2.getFilename()), actualEmptyResource2.getFilename());
    Assert.assertArrayEquals(expectedEmptyResource2.getContent(), actualEmptyResource2.getContent());

    BinaryResource actualEmptyResource3 = it.next();
    Assert.assertNotNull(actualEmptyResource3);
    Assert.assertEquals(FileUtility.getFileName(expectedEmptyResource3.getFilename(), false) + "(3)." + FileUtility.getFileExtension(expectedEmptyResource3.getFilename()), actualEmptyResource3.getFilename());
    Assert.assertArrayEquals(expectedEmptyResource3.getContent(), actualEmptyResource3.getContent());
  }

  @Test
  public void testZipAvoidConflictsRepeatedly() {
    BinaryResource expectedEmptyResource1 = new BinaryResource("empty_file.txt", new byte[0]);
    BinaryResource expectedEmptyResource2 = new BinaryResource("empty_file.txt", new byte[0]);
    BinaryResource expectedEmptyResource3 = new BinaryResource("empty_file(1).txt", new byte[0]);
    BinaryResource expectedEmptyResource4 = new BinaryResource("empty_file(2).txt", new byte[0]);
    BinaryResource expectedEmptyResource5 = new BinaryResource("empty_file(1)(3).txt", new byte[0]);
    BinaryResource expectedEmptyResource6 = new BinaryResource("empty_file(2)(2).txt", new byte[0]);

    BinaryResource zipResource = FileUtility.zip("archive.zip", CollectionUtility.arrayList(
        expectedEmptyResource1,
        expectedEmptyResource2,
        expectedEmptyResource3,
        expectedEmptyResource4,
        expectedEmptyResource5,
        expectedEmptyResource6), true);
    Assert.assertNotNull(zipResource);
    Assert.assertEquals("archive.zip", zipResource.getFilename());
    Assert.assertTrue(zipResource.getContent().length > 0);

    Collection<BinaryResource> resources = FileUtility.unzip(zipResource, null);
    Assert.assertNotNull(resources);
    Assert.assertEquals(6, resources.size());
  }

  @Test
  public void testZipConflict() {
    BinaryResource expectedEmptyResource1 = new BinaryResource("empty_file.txt", new byte[0]);
    BinaryResource expectedEmptyResource2 = new BinaryResource("empty_file.txt", new byte[0]);
    BinaryResource expectedEmptyResource3 = new BinaryResource("empty_file.txt", new byte[0]);
    try {
      FileUtility.zip("archive.zip", CollectionUtility.arrayList(expectedEmptyResource1, expectedEmptyResource2, expectedEmptyResource3));
    }
    catch (ProcessingException e) {
      Assert.assertTrue(e.getCause() instanceof IOException);
    }
  }

  @Test
  public void testUnzipArchive() throws IOException {
    File zipFile = null;
    File[] files = null;
    try {
      zipFile = createTempFile("zip.zip");
      files = FileUtility.unzipArchive(zipFile);
      assertEquals(1, files.length);
      assertEquals("zip", files[0].getName());
    }
    finally {
      IOUtility.deleteFile(zipFile);
      if (files != null) {
        Arrays.stream(files).forEach(IOUtility::deleteFile);
      }
    }
  }

  @Test
  public void testGetFilename() {
    assertEquals("foo", FileUtility.getFileName("foo.bar", false));
    assertEquals("foo.bar", FileUtility.getFileName("foo.bar", true));
    assertEquals("foo.bar", FileUtility.getFileName("foo.bar.baz", false));
    assertEquals("foo.bar.baz", FileUtility.getFileName("foo.bar.baz", true));

    assertEquals("foo.bar", FileUtility.getFileName("/folder/foo.bar", true));
    assertEquals("foo.bar", FileUtility.getFileName("/folder/subfolder/foo.bar", true));
    assertEquals("foo.bar", FileUtility.getFileName("\\folder\\foo.bar", true));
    assertEquals("foo.bar", FileUtility.getFileName("\\folder\\subfolder\\foo.bar", true));
  }
}

