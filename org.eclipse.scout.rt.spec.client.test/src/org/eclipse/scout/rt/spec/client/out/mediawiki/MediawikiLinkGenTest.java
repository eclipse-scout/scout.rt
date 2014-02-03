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
package org.eclipse.scout.rt.spec.client.out.mediawiki;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.scout.rt.spec.client.out.IDocSection;
import org.eclipse.scout.rt.spec.client.out.ILinkTarget;
import org.eclipse.scout.rt.spec.client.out.internal.DocTable;
import org.eclipse.scout.rt.spec.client.out.internal.SectionWithTable;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class MediawikiLinkGenTest {
  private static final String TEST_KEY = "PersonForm";
  private static final String TEST_VALUE = "Person Form";
  private static final String TEST_FILENAME = "Test.mediawiki";

  private MediawikiLinkGen m_linkGen;
  private IDocSection m_emptySection;
  private IDocSection m_testSection;

  @Before
  public void setUp() {
    m_linkGen = new MediawikiLinkGen();
    m_emptySection = new SectionWithTable(null, null);
    DocTable testTable = new DocTable(new String[]{"x"}, new String[][]{{"t"}});
    m_testSection = new SectionWithTable(TEST_KEY, TEST_VALUE, testTable);
  }

  @Test
  public void testLinksForEmptySection() throws Exception {
    assertNoLinks(m_emptySection);
  }

  @Test
  public void testLinksWithSpace() throws Exception {
    assertTestLinkGenerated(m_testSection);
  }

  @Test
  public void testEmptySubSections() throws Exception {
    assertNoLinks(new SectionWithTable(null, null, m_emptySection));
  }

  @Test
  public void testSubSections() throws Exception {
    SectionWithTable s = new SectionWithTable(null, null, m_testSection);
    assertTestLinkGenerated(s);
  }

  private void assertTestLinkGenerated(IDocSection section) {
    List<ILinkTarget> links = m_linkGen.getLinkAnchors(section, TEST_FILENAME);
    assertEquals(links.size(), 1);
    String displayName = links.get(0).getDisplayName();
    assertTrue("Invalid display name" + displayName, displayName.matches(TEST_FILENAME + "#Person\\S+Form\\S*"));
    assertEquals(links.get(0).getTargetId(), TEST_KEY);
  }

  private void assertNoLinks(IDocSection s) {
    List<ILinkTarget> links = m_linkGen.getLinkAnchors(s, TEST_FILENAME);
    assertTrue(links.isEmpty());
  }

}
