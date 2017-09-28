/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

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
    assertNull(FileUtility.getFileExtension((File) new File(FILE_NAME_NO_EXT)));
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
  }

}
