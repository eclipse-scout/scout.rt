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
