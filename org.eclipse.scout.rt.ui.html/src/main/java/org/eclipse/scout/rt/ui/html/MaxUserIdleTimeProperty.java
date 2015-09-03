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
