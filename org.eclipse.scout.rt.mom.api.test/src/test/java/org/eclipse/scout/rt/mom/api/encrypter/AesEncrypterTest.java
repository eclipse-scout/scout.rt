package org.eclipse.scout.rt.mom.api.encrypter;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Before;
import org.junit.Test;

public class AesEncrypterTest {

  private AesEncrypter m_encrypter;

  @Before
  public void before() {
    m_encrypter = BEANS.get(AesEncrypter.class)
        .withAlgorithmProvider("SunJCE")
        .withPbeSalt("salt".getBytes(StandardCharsets.UTF_8))
        .withPbePassword(new char[]{'s', 'e', 'c', 'r', 'e', 't'})
        .withPbeKeyLength(128)
        .withPbeAlgorithm("PBKDF2WithHmacSHA256")
        .withPbeAlgorithmProvider("SunJCE")
        .withPbeIterationCount(3557)
        .withGcmInitializationVectorByteLength(16)
        .withGcmAuthTagBitLength(128)
        .withPadding("PKCS5Padding")
        .init();
  }

  @Test
  public void testInstanceScoped() {
    AesEncrypter encrypter1 = BEANS.get(AesEncrypter.class);
    AesEncrypter encrypter2 = BEANS.get(AesEncrypter.class);
    assertNotSame(encrypter1, encrypter2);
  }

  @Test
  public void test1() throws GeneralSecurityException {
    Map<String, String> context = m_encrypter.newContext();
    byte[] plainText = "Hello World".getBytes(StandardCharsets.UTF_8);

    byte[] encryptedText = m_encrypter.encrypt(plainText, context);
    assertThat(encryptedText, is(not(equalTo(plainText))));

    byte[] decryptedText = m_encrypter.decrypt(encryptedText, context);
    assertThat(decryptedText, is(equalTo(plainText)));
  }

  @Test
  public void testDifferentEncryptionForSameInput() throws GeneralSecurityException {
    Map<String, String> context1 = m_encrypter.newContext();
    Map<String, String> context2 = m_encrypter.newContext();

    byte[] plainText = "Hello World".getBytes(StandardCharsets.UTF_8);

    // IV of context 1
    byte[] encryptedText1 = m_encrypter.encrypt(plainText, context1);
    byte[] encryptedText2 = m_encrypter.encrypt(plainText, context1);

    // IV of context 2
    byte[] encryptedText3 = m_encrypter.encrypt(plainText, context2);
    byte[] encryptedText4 = m_encrypter.encrypt(plainText, context2);

    assertThat(encryptedText1, is(not(equalTo(plainText))));
    assertThat(encryptedText1, is(equalTo(encryptedText2)));

    assertThat(encryptedText3, is(not(equalTo(plainText))));
    assertThat(encryptedText3, is(equalTo(encryptedText4)));

    assertThat(encryptedText1, is(not(equalTo(encryptedText3))));
    assertThat(encryptedText2, is(not(equalTo(encryptedText4))));

    byte[] decryptedText1 = m_encrypter.decrypt(encryptedText1, context1);
    byte[] decryptedText2 = m_encrypter.decrypt(encryptedText2, context1);
    byte[] decryptedText3 = m_encrypter.decrypt(encryptedText3, context2);
    byte[] decryptedText4 = m_encrypter.decrypt(encryptedText4, context2);

    assertThat(decryptedText1, is(equalTo(plainText)));
    assertThat(decryptedText2, is(equalTo(plainText)));
    assertThat(decryptedText3, is(equalTo(plainText)));
    assertThat(decryptedText4, is(equalTo(plainText)));
  }

  @Test
  public void testEmptyInput() throws GeneralSecurityException {
    Map<String, String> context = m_encrypter.newContext();

    byte[] encryptedText = m_encrypter.encrypt(new byte[0], context);
    assertTrue(encryptedText.length > 0);
    byte[] decrypted = m_encrypter.decrypt(encryptedText, context);
    assertThat(decrypted, is(equalTo(new byte[0])));
  }
}
