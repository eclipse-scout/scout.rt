/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.page;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeVersionRequired;

/**
 * Marker interface for all data objects describing search restrictions.
 * <p>
 * Note: This interface is not part of module 'com.bsiag.suite.shared' because apart from its usage in client code it is
 * used for CRM and StART application backends and StART backends do not use shared modules.
 */
@TypeVersionRequired
public interface ISearchDo extends IDoEntity {
}
