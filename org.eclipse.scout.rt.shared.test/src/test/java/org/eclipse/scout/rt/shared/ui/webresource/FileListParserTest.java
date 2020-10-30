/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.ui.webresource;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.scout.rt.shared.ui.webresource.FileListParser.FileListEntry;
import org.junit.Test;

public class FileListParserTest {

  @Test
  public void testDev() {
    String[] rawLines = {
        "widgets-theme-dark.css", // 1
        "widgets-theme-dark.css.map",
        "widgets-theme.css", // 2
        "widgets-theme.css.map",
        "widgets.js", // 3
        "widgets.js.map",
        "widgets~login~logout.js", // 4
        "widgets~login~logout.js.map",
        "widgets.js", // 5
        "widgets.js.map",
        "login.js", // 6
        "login.js.map",
        "logout.js", // 7
        "logout.js.map",
        "vendors~widgets.js", // 8
        "vendors~widgets.js.map",
        "vendors~widgets~login~logout.js", // 9
        "vendors~widgets~login~logout.js.map",
        "vendors~widgets.js", // 10
        "vendors~widgets.js.map"};
    List<FileListEntry> parsed = parseLines(rawLines);
    assertEquals(10, parsed.size());
    assertEntryPoints(parsed, "widgets~login~logout.js", "widgets", "login", "logout");
    assertEntryPoints(parsed, "vendors~widgets~login~logout.js", "vendors", "widgets", "login", "logout");
    assertEntryPoints(parsed, "widgets.js", "widgets");
  }

  @Test
  public void testProd() {
    String[] rawLines = {"widgets-3b5331af613bf5a7803d.min.js", // 1
        "widgets-3b5331af613bf5a7803d.min.js.LICENSE.txt",
        "widgets-3b5331af613bf5a7803d.min.js.map",
        "widgets-theme-c80da972b730c67a11ed.min.css", // 2
        "widgets-theme-dark-abc1c9b44e8e42702061.min.css", // 3
        "login-1db4f970039af71104cb.min.js", // 4
        "login-1db4f970039af71104cb.min.js.map",
        "login~logout-8862c78025b29bca5767.min.js", // 5
        "login~logout-8862c78025b29bca5767.min.js.map",
        "logout-8ca5e0149d7ab33f4f20.min.js", // 6
        "logout-8ca5e0149d7ab33f4f20.min.js.map",
        "vendors~widgets~login~logout-546ee42899f2ccc6205f.min.js", // 7
        "vendors~widgets~login~logout-546ee42899f2ccc6205f.min.js.LICENSE.txt",
        "vendors~widgets~login~logout-546ee42899f2ccc6205f.min.js.map",
        "vendors~widgets-945482a5b2d8d312fd1b.min.js", // 8
        "vendors~widgets-945482a5b2d8d312fd1b.min.js.LICENSE.txt",
        "vendors~widgets-945482a5b2d8d312fd1b.min.js.map",
        "file-list",
        "",
        ".js"};
    List<FileListEntry> parsed = parseLines(rawLines);
    assertEquals(8, parsed.size());
    assertEntryPoints(parsed, "login~logout-8862c78025b29bca5767.min.js", "login", "logout");
    assertEntryPoints(parsed, "vendors~widgets~login~logout-546ee42899f2ccc6205f.min.js", "vendors", "widgets", "login", "logout");
    assertEntryPoints(parsed, "widgets-3b5331af613bf5a7803d.min.js", "widgets");
  }

  protected void assertEntryPoints(List<FileListEntry> entries, String entryName, String... expectedEntryPoints) {
    for (FileListEntry entry : entries) {
      if (entryName.equals(entry.rawLine())) {
        assertEquals(new HashSet<>(Arrays.asList(expectedEntryPoints)), entry.entryPoints());
        return;
      }
    }
    fail("Entry '" + entryName + "' could not be found.");
  }

  protected List<FileListEntry> parseLines(String[] inputLines) {
    return createParserForLines(inputLines).parse((URL) null).collect(toList());
  }

  protected static FileListParser createParserForLines(String... lines) {
    return new FileListParser() {
      @Override
      protected Stream<String> readAllLinesFromUrl(URL url) {
        return Stream.of(lines);
      }
    };
  }
}
