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
package org.eclipse.scout.rt.spec.client.gen.extract.form.page;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.gen.extract.AbstractNamedTextExtractor;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiUtility;

/**
 *
 */
public class TablePageExtractor<T extends IPageWithTable<? extends ITable>> extends AbstractNamedTextExtractor<T> {

  public TablePageExtractor() {
    super(TEXTS.get("org.eclipse.scout.rt.spec.label"));
  }

  @Override
  public String getText(T field) {
    return MediawikiUtility.transformToWiki(field.getCell().getText());
  }
}
