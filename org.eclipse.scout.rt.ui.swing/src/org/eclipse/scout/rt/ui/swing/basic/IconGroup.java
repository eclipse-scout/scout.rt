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

import org.eclipse.scout.rt.ui.swing.Activator;
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

  public IconGroup(String iconId) {
    if (iconId == null) {
      return;
    }
    Icon normal = Activator.getIcon(iconId);
    Icon rollover = Activator.getIcon(iconId + "_mouse_over");
    if (rollover == null) {
      rollover = Activator.getIcon(iconId + "_rollover");
    }

    Icon selected = Activator.getIcon(iconId + "_active");
    if (selected == null) {
      selected = Activator.getIcon(iconId + "_pressed");
      if (selected == null) {
        selected = Activator.getIcon(iconId + "_selected");
      }
    }
    Icon disabled = Activator.getIcon(iconId + "_disabled");

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

  public IconGroup(ISwingEnvironment env, String iconId) {
    if (iconId == null) {
      return;
    }
    Icon normal = env.getIcon(iconId);
    Icon rollover = env.getIcon(iconId + "_mouse_over");
    if (rollover == null) {
      rollover = env.getIcon(iconId + "_rollover");
    }

    Icon selected = env.getIcon(iconId + "_active");
    if (selected == null) {
      selected = env.getIcon(iconId + "_pressed");
      if (selected == null) {
        selected = env.getIcon(iconId + "_selected");
      }
    }
    Icon disabled = env.getIcon(iconId + "_disabled");

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

  public Icon getIcon(IconState state) {
    Icon icon = iconMap.get(state);
    if (icon == null && state != IconState.NORMAL) {
      icon = iconMap.get(IconState.NORMAL);
    }
    return icon;
  }

  public void setIcon(IconState state, Icon icon) {
    iconMap.put(state, icon);
  }

  public boolean hasIcon(IconState state) {
    return iconMap.containsKey(state);
  }

}
