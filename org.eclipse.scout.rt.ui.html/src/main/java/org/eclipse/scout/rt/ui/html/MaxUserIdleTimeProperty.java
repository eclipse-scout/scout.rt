/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.config.AbstractPositiveLongConfigProperty;

/**
 * After expiration of this idle time in seconds without any user activity the user is logged out automatically. The
 * default is 4 hours.
 */
public class MaxUserIdleTimeProperty extends AbstractPositiveLongConfigProperty {

  @Override
  protected Long getDefaultValue() {
    return Long.valueOf(TimeUnit.HOURS.toSeconds(4));
  }

  @Override
  public String getKey() {
    return "scout.max.user.idle.time";
  }
}
