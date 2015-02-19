/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.activitymap;

import java.util.Date;

public final class TestTimeScaleBuilder {

  private TestTimeScaleBuilder() {
  }

  public static void main(String[] args) {
    IActivityMap<Long, Long> map = new AbstractActivityMap<Long, Long>() {

    };
    map.setDays(new Date[]{new Date(), new Date(System.currentTimeMillis() + 1000L * 3600L * 24L * 5), new Date(System.currentTimeMillis() + 1000L * 3600L * 24L * 7)});
    TimeScale scale = new TimeScaleBuilder(map).build();
    System.out.println("small:  " + scale.toString(TimeScale.SMALL));
    System.out.println("medium: " + scale.toString(TimeScale.MEDIUM));
    System.out.println("large:  " + scale.toString(TimeScale.LARGE));
  }
}
