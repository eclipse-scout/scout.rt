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

import org.eclipse.scout.rt.dataobject.id.AbstractStringId;
import org.eclipse.scout.rt.dataobject.id.IdTypeName;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * ID to identify a {@link ICustomClientPreferenceDo custom client preference}.
 */
@IdTypeName("scout.CustomClientPreferenceId")
public final class CustomClientPreferenceId extends AbstractStringId {
  private static final long serialVersionUID = 1L;

  private CustomClientPreferenceId(String id) {
    super(id);
  }

  public static CustomClientPreferenceId of(String id) {
    if (StringUtility.isNullOrEmpty(id)) {
      return null;
    }
    return new CustomClientPreferenceId(id);
  }
}
