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
package org.eclipse.scout.commons;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.junit.Test;

/**
 * JUnit tests for {@link MailUtility}
 */
@SuppressWarnings("restriction")
public class MailUtilityTest {

  /**
   * Message without sender can be created
   * 
   * @throws ProcessingException
   *           ,MessagingException
   */
  @Test
  public void testMimeMessageWithoutSender() throws ProcessingException, MessagingException {
    MimeMessage message = MailUtility.createMimeMessage("Body", null, null);
    assertNotNull(message);
    message = MailUtility.createMimeMessage(null, null, "Subject", "Body", null);
    assertNotNull(message);
  }

  @Test
  public void testWordSpecificPattern() throws Exception {
    assertFalse(isMatching("foobar.xml"));
    assertFalse(isMatching("item.xml"));
    assertFalse(isMatching("item0.xml"));
    assertFalse(isMatching("item00.xml"));
    assertTrue(isMatching("item000.xml"));
    assertTrue(isMatching("item001.xml"));
    assertTrue(isMatching("item0000.xml"));
    assertFalse(isMatching("item00000.xml"));

    assertFalse(isMatching("props.xml"));
    assertFalse(isMatching("props0.xml"));
    assertFalse(isMatching("props01.xml"));
    assertTrue(isMatching("props002.xml"));
    assertTrue(isMatching("props012.xml"));
    assertTrue(isMatching("props0123.xml"));
    assertFalse(isMatching("props01234.xml"));
  }

  private boolean isMatching(String fileName) {
    return MailUtility.wordPatternItem.matcher(fileName).matches() || MailUtility.wordPatternProps.matcher(fileName).matches();
  }

  @Test
  public void testDataSourceWithoutFileExtension() throws ProcessingException, IOException, MessagingException {
    final byte[] sampleData = new byte[]{0x0, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF};
    final String fileName = "test.file";

    DataSource ds = MailUtility.createDataSource(new ByteArrayInputStream(sampleData), fileName, null);
    assertNotNull(ds);
    assertEquals(fileName, ds.getName());
    assertTrue(ds instanceof ByteArrayDataSource);
    ByteArrayDataSource bds = (ByteArrayDataSource) ds;
    assertEquals("application/octet-stream", bds.getContentType());
    byte[] data = IOUtility.getContent(bds.getInputStream());
    assertArrayEquals(sampleData, data);

    MimeMessage message = MailUtility.createMimeMessage("test", null, new DataSource[]{ds});
    message.writeTo(new ByteArrayOutputStream());
  }
}
