/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.id;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.LazyValue;

/**
 * TODO [24.0] PBZ remove this class, was replaced by {@link IdCodec} and {@link IdInventory}
 *
 * @deprecated use {@link IdCodec} and {@link IdInventory} instead
 */
@Deprecated
@ApplicationScoped
public class IdExternalFormatter {

  private final LazyValue<IdCodec> m_codec = new LazyValue<>(IdCodec.class);
  private final LazyValue<IdInventory> m_inventory = new LazyValue<>(IdInventory.class);

  /**
   * Returns a string in the format <code>"[type-name]:[raw-id;raw-id;...]"</code>.
   * <ul>
   * <li><b>type-name</b> is computed by {@link IdInventory#getTypeName(IId)}.
   * <li><b>raw-id's</b> are the wrapped ids converted to their string representation (see {@link IdCodec)}, composite
   * ids are unwrapped to their root ids and then converted to their string representation, separated by ';'.
   * </ul>
   */
  public String toExternalForm(IId id) {
    return m_codec.get().toQualified(id);
  }

  /**
   * Parses a string in the format <code>"[type-name]:[raw-id]"</code>.
   *
   * @throws IllegalArgumentException
   *           if the given string does not match the expected format.
   * @throws ProcessingException
   *           If the referenced class is not found
   */
  public IId fromExternalForm(String externalForm) {
    return m_codec.get().fromQualified(externalForm);
  }

  /**
   * Parses a string in the format <code>"[type-name]:[raw-id]"</code>. If {@code externalForm} has not the expected
   * format or there is no type {@code null} is returned.
   */
  public IId fromExternalFormLenient(String externalForm) {
    return m_codec.get().fromQualifiedLenient(externalForm);
  }

  /**
   * @return the type name of the id class as defined by the {@link IdTypeName} annotation or <code>null</code> if the
   *         annotation is not present.
   */
  public String getTypeName(Class<? extends IId> idClass) {
    return m_inventory.get().getTypeName(idClass);
  }

  /**
   * @return id class which declares {@link IdTypeName} with {@code typeName}
   */
  public Class<? extends IId> getIdClass(String typeName) {
    return m_inventory.get().getIdClass(typeName);
  }

  /**
   * @return the type name of the {@link IId} as defined by the {@link IdTypeName} annotation or <code>null</code> if
   *         the annotation is not present.
   */
  public String getTypeName(IId id) {
    return m_inventory.get().getTypeName(id.getClass());
  }
}
