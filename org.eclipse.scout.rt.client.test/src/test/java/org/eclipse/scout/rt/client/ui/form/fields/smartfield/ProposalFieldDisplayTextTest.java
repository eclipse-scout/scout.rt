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
 * Tests the behavior of the display text in a {@link AbstractProposalField}.
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
@SuppressWarnings("deprecation")
public class ProposalFieldDisplayTextTest {

  private ProposalField m_proposalField = new ProposalField();

  @Before
  public void setUp() throws ProcessingException {
    m_proposalField.registerProposalChooserInternal();
  }

  @Test
  public void testNoLookupRowDisplayText() throws ProcessingException {
    // no lookup row before and after text entry
    assertNull(m_proposalField.getCurrentLookupRow());
    m_proposalField.getUIFacadeLegacy().setTextFromUI("c");
    assertEquals("c", m_proposalField.getValue());
    assertEquals("c", m_proposalField.getDisplayText());
    assertNull(m_proposalField.getCurrentLookupRow());

    m_proposalField.setValue("d");
    assertEquals("d", m_proposalField.getValue());
    assertEquals("d", m_proposalField.getDisplayText());
    assertNull(m_proposalField.getCurrentLookupRow());
  }

  @Test
  public void testLookupRowDisplayText() throws ProcessingException {
    // single match
    m_proposalField.getUIFacadeLegacy().setTextFromUI("a");
    // select proposal
    m_proposalField.getProposalChooser().forceProposalSelection();
    m_proposalField.getProposalChooser().doOk();
    assertEquals("aName", m_proposalField.getValue());
    assertEquals("aName", m_proposalField.getDisplayText());
    // lookup row available now
    assertNotNull(m_proposalField.getCurrentLookupRow());

    m_proposalField.setValue("d");
    assertEquals("d", m_proposalField.getValue());
    assertEquals("d", m_proposalField.getDisplayText());
    assertNull(m_proposalField.getCurrentLookupRow());
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
