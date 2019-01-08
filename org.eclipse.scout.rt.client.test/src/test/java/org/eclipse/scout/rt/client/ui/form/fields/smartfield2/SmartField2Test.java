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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield2;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.holders.StringHolder;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class SmartField2Test {

  @Test
  public void testRefreshDisplayText() {
    final StringHolder TEXT_HOLDER = new StringHolder("AAA");

    ISmartField2<Long> field = new AbstractSmartField2<Long>() {
    };
    field.setLookupCall(new LocalLookupCall<Long>() {
      private static final long serialVersionUID = 1L;

      @Override
      protected List<? extends ILookupRow<Long>> execCreateLookupRows() {
        List<ILookupRow<Long>> rows = new ArrayList<>();
        rows.add(new LookupRow<>(1001L, "Lalala"));
        rows.add(new LookupRow<>(1002L, TEXT_HOLDER.getValue()));
        return rows;
      }
    });

    assertEquals(null, field.getValue());
    assertEquals("", field.getDisplayText());

    field.setValue(1001L);
    assertEquals("Lalala", field.getDisplayText());

    field.setValue(1002L);
    assertEquals("AAA", field.getDisplayText());

    // Change holder --> lookup call will return a different text for the same key
    TEXT_HOLDER.setValue("BBB");
    field.refreshDisplayText(); // <-- required to reload the display text
    assertEquals("BBB", field.getDisplayText());

    // Check that it works with null as well
    field.setValue(null);
    assertEquals("", field.getDisplayText());
    field.refreshDisplayText();
    assertEquals("", field.getDisplayText());
  }
}
