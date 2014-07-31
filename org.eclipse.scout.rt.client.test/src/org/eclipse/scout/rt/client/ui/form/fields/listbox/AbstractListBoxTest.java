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
package org.eclipse.scout.rt.client.ui.form.fields.listbox;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.junit.Test;

/**
 * Tests for {@link AbstractListBox}
 */
public class AbstractListBoxTest extends AbstractListBox<Long> {

  @Override
  protected void execFilterLookupResult(ILookupCall<Long> call, List<ILookupRow<Long>> result) throws ProcessingException {
    result.add(new LookupRow<Long>(1L, "a"));
    result.add(new LookupRow<Long>(2L, "b"));
    result.add(new LookupRow<Long>(3L, "c"));
    result.add(new LookupRow<Long>(null, "null value"));
  }

  @Test
  public void testNoNullKeys() throws Exception {
    List<? extends ILookupRow<Long>> rows = execLoadTableData();
    assertEquals(3, rows.size());
  }

  @Test
  public void testGetForm() {
    IForm formMock = mock(IForm.class);
    setFormInternal(formMock);
    assertEquals(formMock, getForm());
    assertEquals(formMock, getListBoxFilterBox().getForm());
  }
}
