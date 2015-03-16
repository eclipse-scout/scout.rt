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
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class SmartFieldParseValueTest {

  @Test
  public void testSingleMatch() throws ProcessingException {
    SmartField smartField = new SmartField();
    // single match
    smartField.getUIFacade().setTextFromUI("a");
    assertEquals(Long.valueOf(1L), smartField.getValue());
    assertNull(smartField.getProposalChooser().getModel());
  }

//  @Test
//  public void testMultiMatch() throws ProcessingException {
//    SmartField smartField = new SmartField();
//    // match with two elements
//    smartField.getUIFacade().setTextFromUI("b");
//    assertNull(smartField.getValue());
//    assertNotNull(smartField.getProposalChooser().getModel());
//    assertEquals(2, smartField.getProposalChooser().getSearchResult().getLookupRows().size());
//    // select first
//    smartField.getProposalChooser().forceProposalSelection();
//    // close the proposal form
//    smartField.getProposalChooser().doOk();
//    assertNull(smartField.getProposalChooser().getModel());
//    assertEquals(Long.valueOf(1L), smartField.getValue());
//  }

//  @Test
//  public void testNoMatch() throws ProcessingException {
//    SmartField smartField = new SmartField();
//    // single match
//    smartField.getUIFacade().setTextFromUI("c");
//    assertNull(smartField.getValue());
//    assertNotNull(smartField.getProposalChooser().getModel());
//    assertEquals(0, smartField.getProposalChooser().getSearchResult().getLookupRows().size());
//    try {
//      smartField.getProposalChooser().doOk();
//      fail();
//    }
//    catch (VetoException e) {
//      // void
//    }
//    assertNotNull(smartField.getProposalChooser().getModel());
//  }

  private static class SmartField extends AbstractSmartField<Long> {
    @Override
    protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
      return P_LookupCall.class;
    }
  }

  public static class P_LookupCall extends LookupCall<Long> {

    private static final long serialVersionUID = -7536271824820806283L;

    @Override
    protected ILookupService<Long> createLookupService() {
      return new P_LookupService();
    }

  }

  public static class P_LookupService implements ILookupService<Long> {

    @Override
    public List<? extends ILookupRow<Long>> getDataByKey(ILookupCall<Long> call) throws ProcessingException {
      return null;
    }

    @Override
    public List<? extends ILookupRow<Long>> getDataByText(ILookupCall<Long> call) throws ProcessingException {
      if ("a*".equals(call.getText())) {
        return CollectionUtility.arrayList(new LookupRow<Long>(1L, "AName"));
      }
      if ("b*".equals(call.getText())) {
        return CollectionUtility.arrayList(new LookupRow<Long>(1L, "AName"), new LookupRow<Long>(2L, "bName"));
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
