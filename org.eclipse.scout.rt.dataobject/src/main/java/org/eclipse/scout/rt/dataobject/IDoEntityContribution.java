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
 * Interface for DO entity contributions.
 * <ul>
 * <li>Each implementation must be final (no subclassing of contributions).
 * <li>Each implementation must have a {@link ContributesTo} annotation with a non-empty {@link ContributesTo#value()}
 * attribute.
 * </ul>
 */
public interface IDoEntityContribution extends IDoEntity {
}
