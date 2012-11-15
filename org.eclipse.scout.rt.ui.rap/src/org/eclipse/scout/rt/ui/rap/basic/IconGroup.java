/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.basic;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.swt.graphics.Image;

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

  private final Map<IconState, Image> iconMap = new HashMap<IconState, Image>();

  public IconGroup() {
  }

//  public IconGroup(String iconId) {
//    if (iconId != null) {
//      Image normal = Activator.getIcon(iconId);
//      Image rollover = Activator.getIcon(iconId + "_mouse_over");
//      if (rollover == null) {
//        rollover = Activator.getIcon(iconId + "_rollover");
//        if (rollover == null) {
//          rollover = normal;
//        }
//      }
//      Image selected = Activator.getIcon(iconId + "_active");
//      if (selected == null) {
//        selected = Activator.getIcon(iconId + "_pressed");
//        if (selected == null) {
//          selected = Activator.getIcon(iconId + "_selected");
//          if (selected == null) {
//            selected = normal;
//          }
//        }
//      }
//      Image disabled = Activator.getIcon(iconId + "_disabled");
//      if (disabled == null) {
//        disabled = normal;
//      }
//      iconMap.put(IconState.NORMAL, normal);
//      iconMap.put(IconState.ROLLOVER, rollover);
//      iconMap.put(IconState.SELECTED, selected);
//      iconMap.put(IconState.DISABLED, disabled);
//    }
//  }

  public IconGroup(IRwtEnvironment env, String iconId) {
    if (iconId == null) {
      return;
    }

    Image normal = env.getIcon(iconId);
    Image rollover = env.getIcon(iconId + "_mouse_over");
    if (rollover == null) {
      rollover = env.getIcon(iconId + "_rollover");
    }
    Image selected = env.getIcon(iconId + "_active");
    if (selected == null) {
      selected = env.getIcon(iconId + "_pressed");
      if (selected == null) {
        selected = env.getIcon(iconId + "_selected");
      }
    }
    Image disabled = env.getIcon(iconId + "_disabled");

    iconMap.put(IconState.NORMAL, normal);
    if (rollover != null) {
      iconMap.put(IconState.ROLLOVER, rollover);
    }
    if (selected != null) {
      iconMap.put(IconState.SELECTED, selected);
    }
    if (disabled != null) {
      iconMap.put(IconState.DISABLED, disabled);
    }
  }

  public Image getIcon(IconState state) {
    Image icon = iconMap.get(state);
    if (icon == null && state != IconState.NORMAL) {
      icon = iconMap.get(IconState.NORMAL);
    }
    return icon;
  }

  public void setIcon(IconState state, Image icon) {
    iconMap.put(state, icon);
  }

  public boolean hasIcon(IconState state) {
    return iconMap.containsKey(state);
  }
}
