/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.util.uuid;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Base64Utility;

@ApplicationScoped
public class UuidHelper {

  protected static final int UUID_BYTE_LENGTH = 16;
  protected static final int URL_SAFE_ENCODED_UUID_LENGTH = 22;

  /**
   * @return byte representation of specified {@link UUID}
   */
  public byte[] toByteArray(UUID uuid) {
    ByteBuffer bb = ByteBuffer.wrap(new byte[UUID_BYTE_LENGTH]);
    bb.putLong(uuid.getMostSignificantBits());
    bb.putLong(uuid.getLeastSignificantBits());
    return bb.array();
  }

  /**
   * @return UUID build out of specified byte[]
   */
  public UUID fromByteArray(byte[] bytes) {
    Assertions.assertEqual(UUID_BYTE_LENGTH, bytes.length, "invalid byte[] length {}", bytes.length);
    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
    long high = byteBuffer.getLong();
    long low = byteBuffer.getLong();
    return new UUID(high, low);
  }

  /**
   * @return UUID base64 decoded from specified {@link String}
   */
  public UUID decodeUrlSafe(String string) {
    byte[] bytes = Base64Utility.decodeUrlSafe(string);
    return fromByteArray(bytes);
  }

  /**
   * @return URL-safe base64 encoded {@link UUID}
   */
  public String encodeUrlSafe(UUID uuid) {
    byte[] bytes = toByteArray(uuid);
    String encoded = Base64Utility.encodeUrlSafe(bytes);
    return encoded.substring(0, URL_SAFE_ENCODED_UUID_LENGTH); // length of base64-encoded UUID is fixed, remove padding characters
  }
}
