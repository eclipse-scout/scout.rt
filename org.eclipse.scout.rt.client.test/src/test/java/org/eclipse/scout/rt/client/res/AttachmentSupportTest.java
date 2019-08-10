/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.res;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Test;

public class AttachmentSupportTest {

  private AttachmentSupport m_support = new AttachmentSupport();

  @Test
  public void testStatus() throws Exception {
    BinaryResource br1 = newBinaryResource("foo");

    // initial status
    m_support.addAttachment(br1);
    assertEquals(false, m_support.isUploaded(br1));
    assertEquals(true, m_support.isReferenced(br1));

    // change status
    m_support.setUploaded(br1, true);
    m_support.setReferenced(br1, false);
    assertEquals(true, m_support.isUploaded(br1));
    assertEquals(false, m_support.isReferenced(br1));
  }

  @Test
  public void testGetAttachments() throws Exception {
    BinaryResource br1 = newBinaryResource("foo");
    BinaryResource br2 = newBinaryResource("bar");

    // all
    m_support.addAttachment(br1);
    m_support.addAttachment(br2);
    assertEquals(2, m_support.getAttachments().size());

    // only referenced
    m_support.setReferenced(br1, false);
    assertEquals(br2, CollectionUtility.firstElement(m_support.getAttachments(null, true)));

    // only uploaded
    m_support.setUploaded(br2, true);
    assertEquals(br2, CollectionUtility.firstElement(m_support.getAttachments(true, null)));
  }

  @Test
  public void testCleanup() throws Exception {
    BinaryResource br1 = newBinaryResource("foo");

    // nothing to clean up
    m_support.addAttachment(br1);
    m_support.cleanup();
    assertEquals(1, m_support.getAttachments().size());

    // remove un-referenced
    m_support.setReferenced(br1, false);
    m_support.cleanup();
    assertEquals(0, m_support.getAttachments().size());
  }

  protected BinaryResource newBinaryResource(String filename) {
    return new BinaryResource(filename, new byte[]{});
  }

}
