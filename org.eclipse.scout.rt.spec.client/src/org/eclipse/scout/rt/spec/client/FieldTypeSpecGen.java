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
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.spec.client.gen.TypeSpecGenerator;
import org.eclipse.scout.rt.spec.client.out.IDocSection;

/**
 *
 */
public abstract class FieldTypeSpecGen extends AbstractSpecGen {

  public void printAllFields() throws ProcessingException {

    Class[] fieldTypes = new Class[]{AbstractStringField.class, AbstractDateField.class};
    IDocSection doc = generate(fieldTypes);
    write(doc, "SpecTypes", new String[]{});
  }

  protected IDocSection generate(Class[] fieldTypes) {
    TypeSpecGenerator g = new TypeSpecGenerator(getConfiguration());
    return g.getDocSection(fieldTypes);
  }

  /**
   * @return
   */
  public abstract IPageWithTable<? extends ITable> createAndStartTablePage() throws ProcessingException;

}
