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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Object replacing strategy used for replacing and resolving objects during the serialization and deserialization
 * process, respectively.
 *
 * @since 3.8.2
 */
public interface IObjectReplacer {

  /**
   * Replaces the given object with a different one during object serialization. The method is invoked by an
   * {@link ObjectOutputStream}'s replaceObject method. Typically, replacements performed by this method are restored by
   * {@link #resolveObject(Object)}
   */
  Object replaceObject(Object obj) throws IOException;

  /**
   * Substitutes an object with a different one during object deserialization. The method is invoked by an
   * {@link ObjectInputStream}'s resolveObject method. Typically, this method restores the changes done by
   * {@link #replaceObject(Object)}.
   */
  Object resolveObject(Object obj) throws IOException;
}
