/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.security;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class CrypterTest {

  @Test
  public void testEncryptDecrypt() {
    char[] password = "insecure".toCharArray();
    int keyLength = 128;

    Crypter crypter = BEANS.get(Crypter.class).init(password, keyLength);
    String clearTextData = "lorem ipsum dolor";
    String encryptedData = crypter.encrypt(clearTextData);
    String decryptedData = crypter.decrypt(encryptedData);
    assertEquals(clearTextData, decryptedData);
  }

  @Test
  public void testUrlSafeEncryptDecrypt() {
    char[] password = "insecure".toCharArray();
    int keyLength = 128;

    Crypter crypter = BEANS.get(Crypter.class).init(password, keyLength);
    String clearTextData = "lorem ipsum dolor";
    String encryptedData = crypter.encryptUrlSafe(clearTextData);
    String decryptedData = crypter.decryptUrlSafe(encryptedData);
    assertEquals(clearTextData, decryptedData);
  }

  @Test(expected = ProcessingException.class)
  public void testDecryptUnsupportedProfile() {
    char[] password = "insecure".toCharArray();
    int keyLength = 128;

    Crypter crypter = BEANS.get(Crypter.class).init(password, keyLength);
    String clearTextData = "lorem ipsum dolor";
    String encryptedData = crypter.encrypt(clearTextData);

    // replace current profile by an own one
    encryptedData = "unsupported-profile" + encryptedData.substring(Crypter.CURRENT_PROFILE.length());

    crypter.decrypt(encryptedData);
  }

  /**
   * Profile name must not contain a dot.
   */
  @Test
  public void testProperCurrentProfileName() {
    assertEquals(-1, Crypter.CURRENT_PROFILE.indexOf("."));
  }
}
