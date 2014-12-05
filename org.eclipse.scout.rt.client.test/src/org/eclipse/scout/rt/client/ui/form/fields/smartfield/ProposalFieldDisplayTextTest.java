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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.ServiceRegistration;

/**
 * Tests the behavior of the display text in a {@link AbstractProposalField}.
 */
@RunWith(ScoutClientTestRunner.class)
public class ProposalFieldDisplayTextTest {

  @Test
  public void testNoLookupRowDisplayText() throws ProcessingException {
    ProposalField field = new ProposalField();
    // no lookup row before and after text entry
    assertNull(field.getCurrentLookupRow());
    field.getUIFacade().setTextFromUI("c");
    assertEquals("c", field.getValue());
    assertEquals("c", field.getDisplayText());
    assertNull(field.getCurrentLookupRow());

    field.setValue("d");
    assertEquals("d", field.getValue());
    assertEquals("d", field.getDisplayText());
    assertNull(field.getCurrentLookupRow());
  }

  @Test
  public void testLookupRowDisplayText() throws ProcessingException {
    ProposalField field = new ProposalField();
    // single match
    field.getUIFacade().setTextFromUI("a");
    // select proposal
    field.getProposalForm().forceProposalSelection();
    field.getProposalForm().doOk();
    assertEquals("aName", field.getValue());
    assertEquals("aName", field.getDisplayText());
    // lookup row available now
    assertNotNull(field.getCurrentLookupRow());

    field.setValue("d");
    assertEquals("d", field.getValue());
    assertEquals("d", field.getDisplayText());
    assertNull(field.getCurrentLookupRow());
  }

  private static class ProposalField extends AbstractProposalField<String> {
    @Override
    protected Class<? extends ILookupCall<String>> getConfiguredLookupCall() {
      return P_LookupCall.class;
    }
  }

  @ClassId("608fb4ea-b218-42b1-a31f-d9597193ad6c")
  public static class P_LookupCall extends LookupCall<String> {

    private static final long serialVersionUID = 1L;

    @Override
    protected ILookupService<String> createLookupService() {
      return new P_LookupService();
    }

  }

  public static class P_LookupService implements ILookupService<String> {

    @Override
    public void initializeService(ServiceRegistration registration) {
    }

    @Override
    public List<? extends ILookupRow<String>> getDataByKey(ILookupCall<String> call) throws ProcessingException {
      return null;
    }

    @Override
    public List<? extends ILookupRow<String>> getDataByText(ILookupCall<String> call) throws ProcessingException {
      if ("a*".equals(call.getText())) {
        return CollectionUtility.arrayList(new LookupRow<String>("aName", "aName"));
      }
      return Collections.emptyList();
    }

    @Override
    public List<? extends ILookupRow<String>> getDataByAll(ILookupCall<String> call) throws ProcessingException {
      return null;
    }

    @Override
    public List<? extends ILookupRow<String>> getDataByRec(ILookupCall<String> call) throws ProcessingException {
      return null;
    }

  }
}
