/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.security;

import static org.eclipse.scout.rt.platform.util.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import org.eclipse.scout.rt.platform.util.MutablePair;

/**
 * This class represents a password hash and can be used to hash a password and to verify it.
 * Hash creation and hash verification are separated and take into account all known variations of hash computation.
 * <p>
 * Use {@link SecurityUtility#createPasswordHash(char[], byte[])} to create a {@link PasswordHash} that hashes a password
 * with respect to the current security standard and all known older variations.
 */
public final class PasswordHash {

  private final char[] m_password;
  private final byte[] m_salt;
  private final List<MutablePair<BiFunction<char[], byte[], byte[]>, byte[]>> m_hashGeneratorAndHash;

  private PasswordHash(char[] password, byte[] salt, List<BiFunction<char[], byte[], byte[]>> hashGenerators) {
    m_password = password;
    m_salt = salt;
    m_hashGeneratorAndHash = hashGenerators.stream()
        .map(generator -> MutablePair.of(generator, (byte[]) null))
        .toList();
  }

  public char[] getPassword() {
    return Arrays.copyOf(m_password, m_password.length);
  }

  public byte[] getSalt() {
    return Arrays.copyOf(m_salt, m_salt.length);
  }

  /**
   * Creates a password hash. The hashing of the password is performed only once. The result will be cached.
   *
   * @return A {@code byte[]} containing the password hash.
   */
  public byte[] get() {
    return get(0);
  }

  private byte[] get(int index) {
    MutablePair<BiFunction<char[], byte[], byte[]>, byte[]> hashGeneratorAndHash = m_hashGeneratorAndHash.get(index);
    if (hashGeneratorAndHash.getRight() == null) {
      BiFunction<char[], byte[], byte[]> hashGenerator = hashGeneratorAndHash.getLeft();
      hashGeneratorAndHash.setRight(hashGenerator.apply(m_password, m_salt));
    }
    byte[] hash = hashGeneratorAndHash.getRight();
    return Arrays.copyOf(hash, hash.length);
  }

  /**
   * Verifies that the given expected hash matches this {@link PasswordHash}. Taking into account all known variations
   * of hash computation.
   *
   * @param expectedHash
   *     The expected hash the given {@link PasswordHash} is tested against.
   * @return true if the {@link PasswordHash} matches the given expected hash against one of the known hashing variations.
   */
  public boolean verify(byte[] expectedHash) {
    if (expectedHash == null) {
      return false;
    }
    return IntStream.range(0, m_hashGeneratorAndHash.size())
        .mapToObj(this::get)
        .anyMatch(hash -> Arrays.equals(expectedHash, hash));
  }

  public static final class PasswordHashBuilder {

    private final char[] m_password;
    private final byte[] m_salt;
    private final List<BiFunction<char[], byte[], byte[]>> m_hashGenerators;

    /**
     * Use this method to get a {@link PasswordHashBuilder} to build a new {@link PasswordHash}.
     *
     * @param password
     *     The password to create the hash for. Must not be {@code null} or empty.
     * @param salt
     *     The salt to use. Use {@link SecurityUtility#createRandomBytes(int)} to generate a new random salt for each
     *     credential. Do not use the same salt for multiple credentials. The salt should be at least 32 bytes long.
     *     Remember to save the salt with the hashed password! Must not be {@code null} or an empty array.
     * @param defaultHashGenerator
     *     A {@link BiFunction} that can compute a hash from a password and a salt.
     *     The default generator must be selected so that the hash is computed in line with the current security standard.
     * @return A {@link PasswordHashBuilder} used to build a new {@link PasswordHash}.
     */
    public static PasswordHashBuilder of(char[] password, byte[] salt, BiFunction<char[], byte[], byte[]> defaultHashGenerator) {
      return new PasswordHashBuilder(password, salt)
          .withVariant(defaultHashGenerator);
    }

    private PasswordHashBuilder(char[] password, byte[] salt) {
      assertGreater(assertNotNull(password, "password must not be null.").length, 0, "empty password is not allowed.");
      assertGreater(assertNotNull(salt, "salt must not be null.").length, 0, "empty salt is not allowed.");

      m_password = password;
      m_salt = salt;
      m_hashGenerators = new ArrayList<>();
    }

    /**
     * Use this method to add variations of the hash computation. This is necessary when the security standard for hashing
     * changes, for example, due to a change in the number of iterations or the algorithm itself.
     *
     * @param hashGenerator
     *     A {@link BiFunction} that can compute a hash from a password and a salt.
     * @return A {@link PasswordHashBuilder} used to build a new {@link PasswordHash}.
     */
    public PasswordHashBuilder withVariant(BiFunction<char[], byte[], byte[]> hashGenerator) {
      m_hashGenerators.add(assertNotNull(hashGenerator, "hashGenerator must not be null."));
      return this;
    }

    public PasswordHash build() {
      return new PasswordHash(m_password, m_salt, m_hashGenerators);
    }
  }
}
