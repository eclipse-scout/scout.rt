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
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
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
 *
 */
@RunWith(ScoutClientTestRunner.class)
public class SmartFieldParseValueTest {

  @Test
  public void testSingleMatch() throws ProcessingException {
    SmartField smartField = new SmartField();
    // single match
    smartField.getUIFacade().setTextFromUI("a");
    assertEquals(new Long(1L), smartField.getValue());
    assertNull(smartField.getProposalForm());

  }

  @Test
  public void testMultiMatch() throws ProcessingException {
    SmartField smartField = new SmartField();
    // match with two elements
    smartField.getUIFacade().setTextFromUI("b");
    assertNull(smartField.getValue());
    assertNotNull(smartField.getProposalForm());
    assertEquals(2, smartField.getProposalForm().getSearchResult().getLookupRows().size());
    // select first
    smartField.getProposalForm().forceProposalSelection();
    // close the proposal form
    smartField.getProposalForm().doOk();
    assertNull(smartField.getProposalForm());
    assertEquals(new Long(1L), smartField.getValue());
  }

  @Test
  public void testNoMatch() throws ProcessingException {
    SmartField smartField = new SmartField();
    // single match
    smartField.getUIFacade().setTextFromUI("c");
    assertNull(smartField.getValue());
    IContentAssistFieldProposalForm<Long> form = smartField.getProposalForm();
    assertNotNull(form);
    assertEquals(0, form.getSearchResult().getLookupRows().size());
    try {
      form.doOk();
      fail();
    }
    catch (VetoException e) {
      // void
    }
    assertNotNull(smartField.getProposalForm());

  }

  private static class SmartField extends AbstractSmartField<Long> {
    @Override
    protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
      return P_LookupCall.class;
    }
  }

  @ClassId("fa2d4f62-e609-4e62-b8ba-fb76c7368af2")
  public static class P_LookupCall extends LookupCall<Long> {

    private static final long serialVersionUID = -7536271824820806283L;

    @Override
    protected ILookupService<Long> createLookupService() {
      return new P_LookupService();
    }

  }

  public static class P_LookupService implements ILookupService<Long> {

    @Override
    public void initializeService(ServiceRegistration registration) {
    }

    @Override
    public List<? extends ILookupRow<Long>> getDataByKey(ILookupCall<Long> call) throws ProcessingException {
      return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends ILookupRow<Long>> getDataByText(ILookupCall<Long> call) throws ProcessingException {
      if ("a*".equals(call.getText())) {
        return CollectionUtility.arrayList(new LookupRow<Long>(1l, "AName"));
      }
      if ("b*".equals(call.getText())) {
        return CollectionUtility.arrayList(new LookupRow<Long>(1l, "AName"), new LookupRow<Long>(2l, "bName"));
      }
      return Collections.emptyList();
    }

    @Override
    public List<? extends ILookupRow<Long>> getDataByAll(ILookupCall<Long> call) throws ProcessingException {
      return null;
    }

    @Override
    public List<? extends ILookupRow<Long>> getDataByRec(ILookupCall<Long> call) throws ProcessingException {
      return null;
    }

  }
}
