/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.prefs;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeVersionRequired;

/**
 * A marker interface for custom client preferences.
 * <p>
 * Client preferences are, as the name suggests, used within the client. Data objects used for custom client preferences
 * might be placed in the client module too as long as the backend server never has to deal with them. Reasons for
 * backend server interaction with custom client preferences are for example validation or migration. In such a case
 * it's recommended to place them in the shared module.
 */
@TypeVersionRequired
public interface ICustomClientPreferenceDo extends IDoEntity {
}
