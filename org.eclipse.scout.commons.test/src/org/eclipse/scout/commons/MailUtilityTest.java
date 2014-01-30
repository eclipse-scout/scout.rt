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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

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

}
