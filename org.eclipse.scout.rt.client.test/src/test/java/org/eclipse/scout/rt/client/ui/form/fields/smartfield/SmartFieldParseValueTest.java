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
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
@SuppressWarnings("deprecation")
public class SmartFieldParseValueTest {

  private SmartField m_smartField;

  @Before
  public void setUp() throws ProcessingException {
    m_smartField = new SmartField();
    m_smartField.registerProposalChooserInternal();
  }

  @Test
  public void testSingleMatch() throws ProcessingException {
    // single match
    m_smartField.getUIFacadeLegacy().setTextFromUI("a");
    assertEquals(Long.valueOf(1L), m_smartField.getValue());
    assertNull(m_smartField.getProposalChooser());
  }

  @Test
  public void testMultiMatch() throws ProcessingException {
    // match with two elements
    m_smartField.getUIFacadeLegacy().setTextFromUI("b");
    assertNull(m_smartField.getValue());
    assertNotNull(m_smartField.getProposalChooser());
    assertEquals(2, m_smartField.getProposalChooser().getSearchResult().getLookupRows().size());
    // select first
    m_smartField.getProposalChooser().forceProposalSelection();
    // close the proposal form
    m_smartField.getProposalChooser().doOk();
    assertNull(m_smartField.getProposalChooser());
    assertEquals(Long.valueOf(1L), m_smartField.getValue());
  }

  @Test
  public void testNoMatch() throws ProcessingException {
    // single match
    m_smartField.getUIFacadeLegacy().setTextFromUI("c");
    assertNull(m_smartField.getValue());
    assertNotNull(m_smartField.getProposalChooser());
    assertEquals(0, m_smartField.getProposalChooser().getSearchResult().getLookupRows().size());

    // proposal chooser should not be closed when proposal is not valid
    try {
      m_smartField.getProposalChooser().doOk();
      fail();
    }
    catch (VetoException e) {
      // void
    }
    assertNotNull(m_smartField.getProposalChooser());
  }

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
