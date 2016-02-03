/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.rap.basic;

import java.util.Map;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.basic.table.RwtScoutColumnModel;
import org.eclipse.scout.rt.ui.rap.basic.tree.RwtScoutTreeModel;
import org.eclipse.scout.rt.ui.rap.util.HtmlTextUtility;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;

/**
 * Abstract helper class that processes cell text rendering for {@link RwtScoutColumnModel}, {@link RwtScoutTreeModel}
 * and {@link RwtScoutListModel}
 *
 * @since 4.2
 */
public abstract class AbstractRwtScoutCellTextHelper implements IRwtScoutCellTextHelper {

  private final IRwtEnvironment m_env;
  private final IRwtScoutComposite<?> m_uiComposite;

  public AbstractRwtScoutCellTextHelper(IRwtEnvironment env, IRwtScoutComposite<?> uiComposite) {
    m_env = env;
    m_uiComposite = uiComposite;
  }

  @Override
  public String processCellText(ICell cell) {
    if (cell == null) {
      return "";
    }

    String text = cell.getText();

    if (text == null) {
      text = "";
    }
    else if (HtmlTextUtility.isTextWithHtmlMarkup(text)) {
      text = m_env.adaptHtmlCell(m_uiComposite, text);
      text = m_env.convertLinksInHtmlCell(m_uiComposite, text, createAdditionalLinkParams());
    }
    else { // handle multi lines
      boolean multilineScoutObject = isMultilineScoutObject();
      if (!multilineScoutObject && isMultilineText(text)) {
        text = StringUtility.replaceNewLines(text, " ");
      }

      if (RwtUtility.isMarkupEnabled(m_uiComposite.getUiField()) || multilineScoutObject) {
        text = HtmlTextUtility.escapeHtmlCapableText(m_env.getHtmlValidator(), cell, text);
        return StringUtility.convertPlainTextNewLinesToHtml(text, !isWrapText()); // !wrapText means that breakable chars should be replaced
      }
    }

    return HtmlTextUtility.escapeHtmlCapableText(m_env.getHtmlValidator(), cell, text);
  }

  protected boolean isMultilineText(String text) {
    return text.indexOf("\n") >= 0;
  }

  protected abstract Map<String, String> createAdditionalLinkParams();

  protected abstract boolean isMultilineScoutObject();

  protected abstract boolean isWrapText();

}
