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
 * Interface to a data mapper that uses lenient serialization/deserialization:
 * <ul>
 * <li>is able to serialize a data object that doesn't conform to their structure (e.g. DoValue&lt;IId&gt; but contains
 * a String)
 * <li>might return a data object that causes a {@link ClassCastException} if the data object itself or its attributes
 * are accessed in case they couldn't be deserialized successfully and therefore haven't the expected type
 * </ul>
 *
 * @see IDataObjectMapper
 */
public interface ILenientDataObjectMapper extends IDataObjectMapper {
}
