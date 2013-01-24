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
package org.eclipse.scout.rt.client.test;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.test.DefaultClientTestForm.MainBox.ResultsGroupBox.ResultsTableField;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.shared.services.common.test.BasicTestContext;
import org.eclipse.scout.rt.shared.services.common.test.TestStatus;

/**
 * @deprecated Use Scout JUnit Testing Support: {@link org.eclipse.scout.testing.client.runner.ScoutClientTestRunner} or
 *             {@link org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner} to run Unit tests.
 */
@Deprecated
@SuppressWarnings("deprecation")
public class FormBasedTestContext extends BasicTestContext {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(FormBasedTestContext.class);
  private final DefaultClientTestForm m_form;

  public FormBasedTestContext(DefaultClientTestForm form) {
    m_form = form;
  }

  @Override
  public void begin() {
    super.begin();
    m_form.getResultsTableField().getTable().discardAllRows();
    m_form.getSuccessfulTestsField().setValue(null);
    m_form.getWarningTestsField().setValue(null);
    m_form.getFailedTestsField().setValue(null);
    m_form.getTotalTestsField().setValue(null);
  }

  @Override
  public void end() {
    super.end();
    // summary
    m_form.getSuccessfulTestsField().setValue((long) getSeverityCount(TestStatus.INFO));
    m_form.getWarningTestsField().setValue((long) getSeverityCount(TestStatus.WARNING));
    m_form.getFailedTestsField().setValue((long) (getSeverityCount(TestStatus.ERROR) + getSeverityCount(TestStatus.FATAL)));
    m_form.getTotalTestsField().setValue((long) getStatusList().size());
  }

  @Override
  public void addStatus(TestStatus s) {
    super.addStatus(s);
    if ((s.getSeverity() == TestStatus.INFO && m_form.getLevel1Box().getValue()) ||
        (s.getSeverity() == TestStatus.WARNING && m_form.getLevel2Box().getValue()) ||
        (s.getSeverity() == TestStatus.ERROR && m_form.getLevel3Box().getValue()) ||
        (s.getSeverity() == TestStatus.FATAL && m_form.getLevel3Box().getValue())) {
      try {
        ResultsTableField.Table table = m_form.getResultsTableField().getTable();
        ITableRow r = table.createRow();
        table.getProductColumn().setValue(r, s.getProduct());
        table.getTitleColumn().setValue(r, s.getTitle());
        table.getSubTitleColumn().setValue(r, s.getSubTitle());
        table.getResultColumn().setValue(r, TestStatus.getSeverityAsText(s.getSeverity()));
        table.getDurationColumn().setValue(r, s.getDuration());
        //
        table.addRow(r);
      }
      catch (ProcessingException e) {
        LOG.warn(null, e);
      }
    }
  }

}
