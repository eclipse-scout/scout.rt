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
package org.eclipse.scout.rt.ui.swing.basic;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;

import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;

/**
 * Convenience for managing a set of decoration icons using an icon id
 */
public class IconGroup {

  public static enum IconState {
    NORMAL,
    ROLLOVER,
    SELECTED,
    DISABLED
  }

  private final Map<IconState, Icon> iconMap = new HashMap<IconState, Icon>();

  public IconGroup() {
  }

  public IconGroup(ISwingEnvironment env, String iconId) {
    if (iconId != null) {
      Icon normal = env.getIcon(iconId);
      Icon rollover = env.getIcon(iconId + "_mouse_over");
      if (rollover == null) {
        rollover = env.getIcon(iconId + "_rollover");
        if (rollover == null) {
          rollover = normal;
        }
      }
      Icon selected = env.getIcon(iconId + "_active");
      if (selected == null) {
        selected = env.getIcon(iconId + "_pressed");
        if (selected == null) {
          selected = env.getIcon(iconId + "_selected");
          if (selected == null) {
            selected = normal;
          }
        }
      }
      Icon disabled = env.getIcon(iconId + "_disabled");
      if (disabled == null) {
        disabled = normal;
      }
      iconMap.put(IconState.NORMAL, normal);
      iconMap.put(IconState.ROLLOVER, rollover);
      iconMap.put(IconState.SELECTED, selected);
      iconMap.put(IconState.DISABLED, disabled);
    }
  }

  public Icon getIcon(IconState state) {
    return iconMap.get(state);
  }

  public boolean hasIcon(IconState state) {
    return iconMap.containsKey(state);
  }

}
