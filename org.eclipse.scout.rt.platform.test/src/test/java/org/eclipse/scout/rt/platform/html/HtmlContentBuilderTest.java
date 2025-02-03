/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.html;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.html.internal.HtmlContentBuilder;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Test;

/**
 * Tests for {@link HtmlContentBuilder}
 */
public class HtmlContentBuilderTest {

  @Test
  public void testManyBinds() {
    IHtmlElement h2 = HTML.h2("h2");
    IHtmlTable table = createTable("0");

    IHtmlElement html = HTML.div(
        h2,
        table);

    String exp = "<div><h2>h2</h2>" + createTableString("0") + "</div>";
    assertEquals(exp, html.toHtml());
  }

  private String createTableString(String prefix) {
    List<String> rows = new ArrayList<>();
    for (int i = 0; i < 1; i++) {
      rows.add(createRowString(prefix, i));
    }
    return "<table>" + CollectionUtility.format(rows, "") + "</table>";
  }

  private String createRowString(String prefix, int i) {
    return HTML.tr(HTML.td("A" + prefix + i), HTML.td("B" + prefix + i)).toHtml();
  }

  private IHtmlTable createTable(String prefix) {
    List<IHtmlTableRow> rows = new ArrayList<>();
    for (int i = 0; i < 1; i++) {
      rows.add(createRow(prefix, i));
    }
    return HTML.table(rows);
  }

  private IHtmlTableRow createRow(String prefix, int i) {
    return HTML.tr(HTML.td("A" + prefix + i), HTML.td("B" + prefix + i));
  }
}
