/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.userfilter;

/**
 * This marker interface indicates that the filtering happens in the ui by the user. This is mainly necessary to decide
 * whether an object (table row, tree node) is allowed to be sent to the ui or not. Objects filtered by a regular (not
 * user) filter are never sent to the ui.
 *
 * @since 5.1
 */
public interface IUserFilter {

}
