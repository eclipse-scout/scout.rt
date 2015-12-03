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
package org.eclipse.scout.rt.client.mobile.ui.basic.table;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.scout.rt.client.ui.basic.table.ColumnSet;
import org.eclipse.scout.rt.client.ui.basic.table.TableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PagingTableRow extends TableRow {
  private static final Logger LOG = LoggerFactory.getLogger(PagingTableRow.class);
  private static String s_htmlCellTemplate;

  static {
    try {
      s_htmlCellTemplate = initHtmlCellTemplate();
    }
    catch (IOException e) {
      LOG.error("Couldn't load html template for page change cell.", e);
    }
  }

  private Type m_type;

  public PagingTableRow(ColumnSet columnSet, Type type) {
    super(columnSet);
    m_type = type;
    updateContent(columnSet);
  }

  private void updateContent(ColumnSet columnSet) {
    IColumn column = CollectionUtility.firstElement(columnSet.getVisibleColumns());
    if (column != null) {
      String content;
      if (Type.back.equals(m_type)) {
        content = TEXTS.get("MobilePagingShowPrevious");
      }
      else {
        content = TEXTS.get("MobilePagingShowNext");
      }
      content = "<b>" + content + "</b>";
      String output = s_htmlCellTemplate.replace("#CONTENT#", content);
      getCellForUpdate(column).setText(output);
    }
  }

  private static String initHtmlCellTemplate() throws IOException {
    return new String(IOUtility.getContent(PagingTableRow.class.getResource("/org/eclipse/scout/rt/client/mobile/html/MobileTableCellMoreElements.html").openStream()), StandardCharsets.ISO_8859_1);
  }

  public enum Type {
    back,
    forward
  }
}
