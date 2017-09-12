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
package org.eclipse.scout.rt.platform.security;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.IConfigProperty;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Credential verifier against credentials configured in <i>config.properties</i> file.
 * <p>
 * By default, this verifier expects the passwords in 'config.properties' to be a hash produced with SHA-512 algorithm.
 * To generate a password hash, you can use this class' main method.
 * <p>
 * Credentials are loaded from property {@link CredentialsProperty}. Multiple credentials are separated with the
 * semicolon, username and password with the colon. If using hashed passwords (by default), the password's salt and hash
 * are separated with the dot.
 * <p/>
 * Example of hashed passwords: <code>scott:SALT.PASSWORD-HASH;jack:SALT.PASSWORD-HASH;john:SALT.PASSWORD-HASH</code>
 * <br/>
 * Example of plain-text passwords: <code>scott:*****;jack:*****;john:*****</code>
 */
@Bean
public class ConfigFileCredentialVerifier implements ICredentialVerifier {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigFileCredentialVerifier.class);

  private final Map<String, IPassword> m_credentials;

  public ConfigFileCredentialVerifier() {
    m_credentials = new HashMap<>();
  }

  /**
   * Overwrite to initialize this verifier with credentials.
   * <p>
   * The default implementation loads credentials from {@link CredentialsProperty}.
   */
  @PostConstruct
  protected void init() {
    loadCredentials(BEANS.get(CredentialsProperty.class));
  }

  /**
   * Invoke to load credentials represented by the given property.
   */
  protected final void loadCredentials(final IConfigProperty<String> configProperty) {
    m_credentials.clear();

    final String credentialsAsLine = configProperty.getValue();
    if (credentialsAsLine == null) {
      return;
    }

    final String credentialSample = String.format("%s=scott:salt.password-hash;jack:salt.password-hash;john:salt.password-hash", configProperty.getKey());

    for (final String credentialAsLine : credentialsAsLine.split(";")) {
      final String[] credential = credentialAsLine.split(":", 2);
      if (credential.length == 2) {
        final String username = credential[0];
        if (!StringUtility.hasText(username)) {
          LOG.warn("Configured username must not be empty. [example={}]", credentialSample);
          continue;
        }

        final String password = credential[1];
        if (!StringUtility.hasText(password)) {
          LOG.warn("Configured password must not be empty. [example={}]", credentialSample);
          continue;
        }

        m_credentials.put(username, createPassword(password));
      }
      else {
        LOG.warn("Username and password must be separated with the 'colon' sign.  [example={}]", credentialSample);
      }
    }
  }

  @Override
  public int verify(final String username, final char[] passwordPlainText) {
    if (StringUtility.isNullOrEmpty(username) || passwordPlainText == null || passwordPlainText.length == 0) {
      return AUTH_CREDENTIALS_REQUIRED;
    }

    final IPassword password = m_credentials.get(username.toLowerCase());
    if (password == null || !password.isEqual(passwordPlainText)) {
      return AUTH_FORBIDDEN;
    }

    return AUTH_OK;
  }

  /**
   * Method invoked to create the {@link IPassword} for a password from config.properties.
   */
  protected IPassword createPassword(final String password) {
    if (CONFIG.getPropertyValue(CredentialPlainTextProperty.class)) {
      return new PlainTextPassword(password.toCharArray());
    }
    else {
      return new HashedPassword(password);
    }
  }

  // ==== Config Properties ==== //

  /**
   * Represents credentials to be loaded into {@link ConfigFileCredentialVerifier}.
   * <p>
   * Multiple credentials are separated with a semicolon, username and password with the 'colon' sign.
   * <p/>
   * Example: <code>scott:*****;jack:*****;john:*****</code>
   */
  public static class CredentialsProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.auth.credentials";
    }
  }

  /**
   * Indicates whether plain-text or hashed passwords are stored in 'config.properties'. By default, this verifier
   * expects hashed passwords.
   */
  public static class CredentialPlainTextProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.auth.credentials.plaintext";
    }

    @Override
    protected Boolean getDefaultValue() {
      return Boolean.FALSE;
    }
  }

  // ==== Password Objects ==== //

  /**
   * Represents a password from 'config.properties'.
   */
  @FunctionalInterface
  protected interface IPassword {

    /**
     * Returns whether the given password matches the password in 'config.properties'.
     */
    boolean isEqual(char[] password);
  }

  /**
   * Represents a plain text password from 'config.properties'.
   */
  protected static class PlainTextPassword implements IPassword {

    private final char[] m_password;

    public PlainTextPassword(final char[] password) {
      m_password = password;
    }

    @Override
    public boolean isEqual(final char[] password) {
      return Arrays.equals(m_password, password);
    }
  }

  /**
   * Represents a password from 'config.properties' with its salt and hash separated by a dot.
   */
  protected static class HashedPassword implements IPassword {

    protected static Charset CHARSET = StandardCharsets.UTF_16;

    private final byte[] m_salt;
    private final byte[] m_hash;

    public HashedPassword(final String saltAndHash) {
      final String[] tokens = saltAndHash.split("\\.");
      Assertions.assertEqual(2, tokens.length, "Invalid password entry: salt and password-hash are to be separated with the dot (.).");
      Assertions.assertGreater(tokens[0].length(), 0, "Invalid password entry: 'salt' must not be empty");
      Assertions.assertGreater(tokens[1].length(), 0, "Invalid password entry: 'password-hash' must not be empty");
      m_salt = Base64Utility.decode(tokens[0]);
      m_hash = Base64Utility.decode(tokens[1]);
    }

    private HashedPassword(final char[] password, final byte[] salt) {
      m_salt = salt;
      m_hash = createPasswordHash(password, salt);
    }

    @Override
    public boolean isEqual(final char[] password) {
      return Arrays.equals(m_hash, createPasswordHash(password, m_salt));
    }

    protected byte[] createPasswordHash(final char[] password, final byte[] salt) {
      return SecurityUtility.hash(toBytes(password), salt);
    }

    protected byte[] toBytes(final char[] password) {
      ByteBuffer bytes = CHARSET.encode(CharBuffer.wrap(password));
      byte[] result = new byte[bytes.remaining()];
      bytes.get(result);
      return result;
    }

    @Override
    public String toString() {
      return String.format("%s.%s", Base64Utility.encode(m_salt), Base64Utility.encode(m_hash));
    }
  }

  /**
   * Helper main method to generate the hash for a password to be put into 'config.properties'.
   */
  public static void main(final String[] args) {
    final String plainTextPassword = args[0];

    System.out.printf("plain-text: %s,  password-hash: %s", plainTextPassword, new HashedPassword(plainTextPassword.toCharArray(), SecurityUtility.createRandomBytes())); // NOSONAR
  }
}
