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
package org.eclipse.scout.commons.html;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.html.internal.HtmlContentBuilder;
import org.junit.Test;

/**
 * Tests for {@link HtmlContentBuilder}
 *
 * @since 6.0 (backported)
 */
public class HtmlContentBuilderTest {

  @Test
  public void testManyBinds() throws Exception {
    IHtmlElement h2 = HTML.h2("h2");
    IHtmlTable table = createTable("0");

    IHtmlElement html = HTML.div(
        h2,
        table);

    String exp = "<div><h2>h2</h2>" + createTableString("0") + "</div>";
    assertEquals(exp, html.toHtml());
  }

  private String createTableString(String prefix) {
    List<String> rows = new ArrayList<String>();
    for (int i = 0; i < 1; i++) {
      rows.add(createRowString(prefix, i));
    }
    return "<table>" + CollectionUtility.format(rows, "") + "</table>";

  }

  private String createRowString(String prefix, int i) {
    return HTML.tr(HTML.td("A" + prefix + i), HTML.td("B" + prefix + i)).toHtml();
  }

  private IHtmlTable createTable(String prefix) {
    List<IHtmlTableRow> rows = new ArrayList<IHtmlTableRow>();
    for (int i = 0; i < 1; i++) {
      rows.add(createRow(prefix, i));
    }
    return HTML.table(rows);
  }

  private IHtmlTableRow createRow(String prefix, int i) {
    return HTML.tr(HTML.td("A" + prefix + i), HTML.td("B" + prefix + i));
  }

}
