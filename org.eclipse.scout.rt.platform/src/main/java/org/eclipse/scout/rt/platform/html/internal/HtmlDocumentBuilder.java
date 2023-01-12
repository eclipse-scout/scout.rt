/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.html.internal;

import org.eclipse.scout.rt.platform.html.IHtmlDocument;

/**
 * Builder for a HTML document (&lt;html&gt;).
 */
public class HtmlDocumentBuilder extends HtmlNodeBuilder implements IHtmlDocument {

  private static final long serialVersionUID = 1L;

  private String m_docType;

  public HtmlDocumentBuilder(CharSequence... texts) {
    super("html", texts);
  }

  @Override
  public void build() {
    if (m_docType != null) {
      append(m_docType, false);
    }
    super.build();
  }

  @Override
  public IHtmlDocument doctype(String doctype) {
    m_docType = doctype;
    return this;
  }

  @Override
  public IHtmlDocument doctype() {
    return doctype(IHtmlDocument.HTML5_DOCTYPE);
  }
}
