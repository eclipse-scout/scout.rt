/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.mobile.ui.basic.table;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.html.HTML;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.mobile.Activator;
import org.eclipse.scout.rt.client.ui.basic.table.ColumnSet;
import org.eclipse.scout.rt.client.ui.basic.table.TableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.shared.TEXTS;

public class PagingTableRow extends TableRow {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PagingTableRow.class);
  private static String s_htmlCellTemplate;

  static {
    try {
      s_htmlCellTemplate = initHtmlCellTemplate();
    }
    catch (Throwable e) {
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

      String output = s_htmlCellTemplate.replace("#CONTENT#", HTML.bold(content).toHtml());
      getCellForUpdate(column).setText(output);
    }
  }

  private static String initHtmlCellTemplate() throws Throwable {
    return new String(IOUtility.getContent(Activator.getDefault().getBundle().getResource("resources/html/MobileTableCellMoreElements.html").openStream()), "iso-8859-1");
  }

  public enum Type {
    back,
    forward
  }
}
