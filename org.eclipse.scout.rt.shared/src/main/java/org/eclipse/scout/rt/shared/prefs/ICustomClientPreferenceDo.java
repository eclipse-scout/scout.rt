/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
