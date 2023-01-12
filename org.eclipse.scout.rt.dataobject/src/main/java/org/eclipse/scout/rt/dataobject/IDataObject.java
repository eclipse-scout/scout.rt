/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

/**
 * Marker interface for a data object.
 * <p>
 * Object-like data objects are represented using {@link IDoEntity} interface, {@link DoEntity} default implementation
 * and its subclasses, collection-like data objects are represented using {@link DoList}.
 * <p>
 * Use this interface for deserialize any data object, whose content is not further specified, e.g:
 *
 * <pre>
 * IDataObject dataObject = BEANS.get(IDataObjectMapper.class).readValue(value, IDataObject.class);
 * if (dataObject instanceof IDoEntity) {
 *   // handle IDoEntity object content
 * }
 * else if (dataObject instanceof DoList) {
 *   // handle DoList content
 * }
 * </pre>
 *
 * @see DoList
 * @see IDoEntity
 */
public interface IDataObject {
}
