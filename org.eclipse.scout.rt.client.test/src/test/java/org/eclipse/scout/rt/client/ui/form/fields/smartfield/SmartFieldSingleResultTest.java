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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class SmartFieldSingleResultTest {

  private static List<IBean<?>> m_beans;

  private SmartField m_smartField;

  @BeforeClass
  public static void beforeClass() throws Exception {
    m_beans = TestingUtility.registerBeans(
        new BeanMetaData(P_SingleResultLookupCall.class));
  }

  @AfterClass
  public static void afterClass() {
    TestingUtility.unregisterBeans(m_beans);
  }

  @Before
  public void setUp() throws Exception {
    m_smartField = new SmartField();
    m_smartField.registerProposalChooserInternal();
  }

  @Test
  public void test() throws Exception {
    final IBlockingCondition bc = Jobs.newBlockingCondition(true);

    m_smartField.getLookupRowFetcher().addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (IContentAssistFieldLookupRowFetcher.PROP_SEARCH_RESULT.equals(evt.getPropertyName())) {
          bc.setBlocking(false);
        }
      }
    });

    m_smartField.getUIFacade().openProposalChooserFromUI("", true, false);
    bc.waitFor();

    // Without the bugfix the accepted proposal would be "SingleResult"
    assertNull(m_smartField.getProposalChooser().getAcceptedProposal());
    m_smartField.getUIFacade().acceptProposalFromUI("", false, false);
    // Without the bugfix the current lookup-row would be "SingleResult"
    assertFalse(m_smartField.isCurrentLookupRowSet());
  }

  private static class SmartField extends AbstractSmartField<Long> {
    @Override
    protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
      return P_SingleResultLookupCall.class;
    }
  }

  /**
   * Returns always only a single result.
   */
  public static class P_SingleResultLookupCall extends LocalLookupCall<Long> {

    private static final long serialVersionUID = 1;

    @Override
    public List<? extends ILookupRow<Long>> getDataByAll() {
      return LookupRows.firstRow();
    }

    @Override
    public List<? extends ILookupRow<Long>> getDataByText() {
      return LookupRows.firstRow();
    }

  }
}
