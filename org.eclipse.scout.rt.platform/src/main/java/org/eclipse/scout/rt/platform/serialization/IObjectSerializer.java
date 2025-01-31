/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.function.Predicate;

/**
 * This interfaces provides methods for serializing and deserializing java objects.
 *
 * @since 3.8.2
 */
public interface IObjectSerializer {

  /**
   * blacklist check for fully qualified names
   * <p>
   * true if className is blacklisted, it is blocked
   */
  Predicate<String> getBlacklist();

  /**
   * <em>WARNING</em> do not call this method unless you are really sure and using a more advanced blacklist.
   * <p>
   * Replace the default blacklist {@link DefaultSerializerBlacklist}. since 11.0
   */
  IObjectSerializer withBlacklist(Predicate<String> blacklist);

  /**
   * whitelist check for fully qualified names
   * <p>
   * true if className is whitelisted, it is accepted
   */
  Predicate<String> getWhitelist();

  /**
   * <em>DEFINE A WHITELIST</em>. The default whitelist is empty and not recommended. It is only empty in order to not
   * breaking the API. Therefore it is highly recommended to use a very strict and limited whitelist of classnames that
   * are accepted.
   * <p>
   * Replace the default unsecure whitelist.
   */
  IObjectSerializer withWhitelist(Predicate<String> whitelist);

  /**
   * Serializes the given object and returns its binary data.
   */
  byte[] serialize(Object o) throws IOException;

  /**
   * Serializes the given object into the given {@link OutputStream}.
   */
  void serialize(OutputStream out, Object o) throws IOException;

  /**
   * Deserializes the given binary data into a java object. The deserialized object's type is tested with the given
   * expected type, if it is not null, using {@link Class#isAssignableFrom(Class)}.
   */
  <T> T deserialize(byte[] buf, Class<T> expectedType) throws IOException, ClassNotFoundException;

  /**
   * Deserializes a java object from the given {@link InputStream}. The deserialized object's type is tested with the
   * given expected type, if it is not null, using {@link Class#isAssignableFrom(Class)}.
   */
  <T> T deserialize(InputStream stream, Class<T> expectedType) throws IOException, ClassNotFoundException;

  /**
   * @return a new {@link ObjectOutputStream} based on this object serializer
   */
  ObjectOutputStream createObjectOutputStream(OutputStream out) throws IOException;

  /**
   * @return a new {@link ObjectInputStream} based on this object serializer, using {@link #getBlacklist()} and
   * {@link #getWhitelist()}
   */
  ObjectInputStream createObjectInputStream(InputStream in) throws IOException;
}
