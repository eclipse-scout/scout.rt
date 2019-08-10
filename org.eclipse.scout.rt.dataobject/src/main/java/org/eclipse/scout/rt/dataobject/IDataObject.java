/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
