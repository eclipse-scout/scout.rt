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
package org.eclipse.scout.rt.spec.client.link;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link DocLink}
 */
public class SpecLinkTest {

  /**
   * Tests that {@link DocLink#toXML()} and {@link DocLink#parse(String)} leads to the same result.
   * 
   * @throws ProcessingException
   */
  @Test
  public void testXMLConversion() throws ProcessingException {
    DocLink testSpecLink = new DocLink("target", "name");
    String xml = testSpecLink.toXML();
    List<DocLink> resultLinks = DocLink.parse(xml);
    Assert.assertEquals(1, resultLinks.size());
    Assert.assertEquals(testSpecLink, resultLinks.get(0));
  }

}
