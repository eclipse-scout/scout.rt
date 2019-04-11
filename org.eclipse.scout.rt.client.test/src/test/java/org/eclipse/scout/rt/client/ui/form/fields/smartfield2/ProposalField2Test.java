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

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ProposalField2Test {

  private AbstractProposalField2<String> m_proposalField2;

  @Before
  public void setUp() {
    m_proposalField2 = new AbstractProposalField2<String>() {
    };
  }

  @Test
  public void testMaxLength() {
    int initialMaxLength = m_proposalField2.getMaxLength();
    assertEquals(m_proposalField2.getConfiguredMaxLength(), initialMaxLength);
    m_proposalField2.setMaxLength(1234);
    assertEquals(1234, m_proposalField2.getMaxLength());
    m_proposalField2.setMaxLength(0);
    assertEquals(0, m_proposalField2.getMaxLength());
    m_proposalField2.setMaxLength(-2);
    assertEquals(0, m_proposalField2.getMaxLength());

    // set value
    m_proposalField2.setValueAsString("the clown has a red nose");
    assertEquals(null, m_proposalField2.getValue());
    m_proposalField2.setMaxLength(9);
    m_proposalField2.setValueAsString("the clown has a red nose");
    assertEquals("the clown", m_proposalField2.getValue());
    m_proposalField2.setMaxLength(4);
    assertEquals("the", m_proposalField2.getValue());
  }

  @Test
  public void testTrimText() {
    m_proposalField2.setMultilineText(true);

    m_proposalField2.setTrimText(true);
    m_proposalField2.setValueAsString("  a  b  ");
    assertEquals("a  b", m_proposalField2.getValue());
    m_proposalField2.setValueAsString("\n  a \n b  \n");
    assertEquals("a \n b", m_proposalField2.getValue());
    m_proposalField2.setValueAsString(null);
    assertEquals(null, m_proposalField2.getValue());

    m_proposalField2.setTrimText(false);
    m_proposalField2.setValueAsString("  a  b  ");
    assertEquals("  a  b  ", m_proposalField2.getValue());
    m_proposalField2.setValueAsString("\n  a \n b  \n");
    assertEquals("\n  a \n b  \n", m_proposalField2.getValue());
    m_proposalField2.setValueAsString(null);
    assertEquals(null, m_proposalField2.getValue());

    // set value
    m_proposalField2.setValueAsString("  a  b  ");
    assertEquals("  a  b  ", m_proposalField2.getValue());
    m_proposalField2.setTrimText(true);
    assertEquals("a  b", m_proposalField2.getValue());
  }

  @Test
  public void testMultilineText() {
    m_proposalField2.setMultilineText(false);

    m_proposalField2.setValueAsString("a\n\nb");
    assertEquals("a  b", m_proposalField2.getValue());
    m_proposalField2.setValue(null);
    assertEquals(null, m_proposalField2.getValue());

    m_proposalField2.setMultilineText(true);
    m_proposalField2.setValueAsString("a\n\nb");
    assertEquals("a\n\nb", m_proposalField2.getValue());
    m_proposalField2.setValue(null);
    assertEquals(null, m_proposalField2.getValue());

    // set value
    m_proposalField2.setMultilineText(true);
    m_proposalField2.setValueAsString("a\nb");
    assertEquals("a\nb", m_proposalField2.getValue());
    m_proposalField2.setMultilineText(false);
    assertEquals("a b", m_proposalField2.getValue());
  }
}
