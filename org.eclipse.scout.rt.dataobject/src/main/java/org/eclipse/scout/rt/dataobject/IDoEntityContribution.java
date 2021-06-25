/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
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
 * Interface for DO entity contributions.
 * <ul>
 * <li>Each implementation must be final (no subclassing of contributions).
 * <li>Each implementation must have a {@link ContributesTo} annotation with a non-empty {@link ContributesTo#value()}
 * attribute.
 * </ul>
 */
public interface IDoEntityContribution extends IDoEntity {
}
