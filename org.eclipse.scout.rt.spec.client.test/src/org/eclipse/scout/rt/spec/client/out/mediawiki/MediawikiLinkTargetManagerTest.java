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

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.spec.client.config.SpecFileConfig;
import org.eclipse.scout.rt.spec.client.link.LinkTarget;
import org.eclipse.scout.rt.spec.client.out.ILinkTarget;
import org.eclipse.scout.rt.spec.client.utility.SpecIOUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link MediawikiLinkTargetManager}
 */
public class MediawikiLinkTargetManagerTest {
  private File m_testFile;
  private File m_specDir;
  private MediawikiLinkTargetManager m_writer;
  private static final String TEST_FILENAME = "test.mediawiki";

  @Before
  public void setUp() throws ProcessingException {
    SpecFileConfig c = new SpecFileConfig("org.eclipse.scout.rt.spec.client.test");
    m_specDir = c.getSpecDir();
    m_specDir.mkdirs();
    m_testFile = new File(m_specDir, "test.properties");
    m_writer = new MediawikiLinkTargetManager(m_testFile);
  }

  @After
  public void tearDown() throws ProcessingException {
    IOUtility.deleteDirectory(m_specDir);
  }

  @Test
  public void testWriteEmptyLinks() throws ProcessingException {
    m_writer.writeLinks(new ArrayList<ILinkTarget>());
    Properties p = SpecIOUtility.loadProperties(m_testFile);
    assertTrue(p.isEmpty());
  }

  @Test
  public void testWriteLinks() throws Exception {
    ArrayList<ILinkTarget> links = new ArrayList<ILinkTarget>();
    links.add(new LinkTarget("target1", "fileA"));
    links.add(new LinkTarget("target2", "fileB"));
    m_writer.writeLinks(links);
    assertEquals("fileA#target1", m_writer.readLinks().get("target1"));
    assertEquals("fileB#target2", m_writer.readLinks().get("target2"));
  }

}
