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
package org.eclipse.scout.rt.spec.client;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.spec.client.gen.PageSpecGenerator;
import org.eclipse.scout.rt.spec.client.out.IDocSection;

/**
 *
 */
public abstract class AbstractTablePageSpecGen extends AbstractSpecGen {

  public void printAllFields() throws ProcessingException {
    IPageWithTable<? extends ITable> page = createAndStartTablePage();
    IDocSection doc = generate(page);
    write(doc, page.classId(), new String[]{}, page.getClass().getSimpleName());
  }

  protected IDocSection generate(IPageWithTable<? extends ITable> page) {
    PageSpecGenerator g = new PageSpecGenerator(getConfiguration());
    return g.getDocSection(page);
  }

  /**
   * @return
   */
  public abstract IPageWithTable<? extends ITable> createAndStartTablePage() throws ProcessingException;

}
