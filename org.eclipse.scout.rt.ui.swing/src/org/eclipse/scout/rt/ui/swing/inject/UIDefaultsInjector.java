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
package org.eclipse.scout.rt.ui.swing.inject;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.UIDefaults;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.IconUIResource;
import javax.swing.plaf.InsetsUIResource;

import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.SwingIcons;

/**
 *
 */
public class UIDefaultsInjector {

  public UIDefaultsInjector() {
  }

  public void inject(ISwingEnvironment env, UIDefaults defaults) {
    /*
     * Defaults
     */
    putIfUndefined(defaults, "control", new ColorUIResource(0xffffff));
    putIfUndefined(defaults, "desktop", new ColorUIResource(0xffffff));
    putIfUndefined(defaults, "textInactiveText", new ColorUIResource(0x666666));
    putIfUndefined(defaults, "DateChooser.applyButtonText", ScoutTexts.get("Apply"));
    putIfUndefined(defaults, "GroupBoxButtonBar.border", new BorderUIResource(new EmptyBorder(12, 0, 6, 0)));//XXX synth?
    putIfUndefined(defaults, "GroupBox.border", new BorderUIResource(new EmptyBorder(6, 3, 3, 3)));//XXX synth?
    putIfUndefined(defaults, "Label.defaultHorizontalAlignment", "LEFT");
    putIfUndefined(defaults, "Label.font", new FontUIResource("Dialog", Font.PLAIN, 12));
    putIfUndefined(defaults, "Hyperlink.foreground", new ColorUIResource(0x445599));
    putIfUndefined(defaults, "List.selectionBackground", new ColorUIResource(0x6cbbe4));
    putIfUndefined(defaults, "ListBox.rowHeight", 20);
    putIfUndefined(defaults, "MenuBar.policy", "menubar");
    putIfUndefined(defaults, "PopupMenu.innerBorder", null);
    putIfUndefined(defaults, "Splash.icon", createIconUIResource(env, "splash"));
    putIfUndefined(defaults, "Splash.text", new ColorUIResource(0x0086A6));
    putIfUndefined(defaults, "StatusBar.StopButton.icon", createIconUIResource(env, SwingIcons.StatusInterrupt));
    putIfUndefined(defaults, "StatusBar.height", 29);
    putIfUndefined(defaults, "StatusBar.icon", null);
    putIfUndefined(defaults, "StatusBar.visible", true);
    putIfUndefined(defaults, "SwingScoutPopup.border", new BorderUIResource(new LineBorder(Color.gray, 1, true)));
    putIfUndefined(defaults, "SystemButton.cancelIcon", null);
    putIfUndefined(defaults, "SystemButton.noIcon", null);
    putIfUndefined(defaults, "SystemButton.yesIcon", null);
    putIfUndefined(defaults, "TabbedPane.tabAreaInsets", new InsetsUIResource(2, 2, 2, 6));
    putIfUndefined(defaults, "TabItem.foreground", new ColorUIResource(0, 0, 0));
    putIfUndefined(defaults, "TabItem.selected.foreground", new ColorUIResource(0, 0, 0));
    putIfUndefined(defaults, "Table.focusCellForeground", new ColorUIResource(0x000000));
    putIfUndefined(defaults, "Table.rowHeight", 24);
    putIfUndefined(defaults, "TableHeader.rowHeight", 26);
    putIfUndefined(defaults, "TextField.border", new BorderUIResource(new EmptyBorder(0, 0, 0, 0)));
    putIfUndefined(defaults, "TitledBorder.border", new BorderUIResource(new EmptyBorder(0, 0, 0, 0)));
    putIfUndefined(defaults, "TitledBorder.font", new FontUIResource("Dialog", Font.PLAIN, 12));
    putIfUndefined(defaults, "TitledBorder.titleColor", new ColorUIResource(0x000000));
    putIfUndefined(defaults, "Tooltip.icon", createIconUIResource(env, SwingIcons.Tooltip));
    putIfUndefined(defaults, "Tree.closedIcon", createIconUIResource(env, AbstractIcons.Folder));
    putIfUndefined(defaults, "Tree.openIcon", createIconUIResource(env, AbstractIcons.FolderOpen));
    putIfUndefined(defaults, "Tree.rowHeight", 24);
    putIfUndefined(defaults, "TreeBox.rowHeight", 20);
    Icon icon = env.getIcon("window");
    if (icon != null) { // legacy
      putIfUndefined(defaults, "Window.icon", icon);// must be an ImageIcon, not an IconUIResource
    }
    else {
      // multiple icons for newer versions of Java
      List<Image> icons = new ArrayList<Image>();
      icons.add(env.getImage("window16"));
      icons.add(env.getImage("window32"));
      icons.add(env.getImage("window48"));
      icons.add(env.getImage("window256"));
      putIfUndefined(defaults, "Window.icons", icons);
    }

    /*
     * Texts
     */
    defaults.put("OptionPane.okButtonText", ScoutTexts.get("Ok"));
    defaults.put("OptionPane.cancelButtonText", ScoutTexts.get("Cancel"));
    defaults.put("OptionPane.yesButtonText", ScoutTexts.get("Yes"));
    defaults.put("OptionPane.noButtonText", ScoutTexts.get("No"));
    defaults.put("OptionPane.copyPasteHint", ScoutTexts.get("CopyPasteHint"));
    defaults.put("OptionPane.copy", ScoutTexts.get("Copy"));
    defaults.put("FileChooser.lookInLabelText", ScoutTexts.get("LookIn"));
    defaults.put("FileChooser.filesOfTypeLabelText", ScoutTexts.get("FilesOfType"));
    defaults.put("FileChooser.fileNameLabelText", ScoutTexts.get("FileName"));
    defaults.put("FileChooser.saveButtonText", ScoutTexts.get("Save"));
    defaults.put("FileChooser.saveDialogTitleText", ScoutTexts.get("Save"));
    defaults.put("FileChooser.openButtonText", ScoutTexts.get("Open"));
    defaults.put("FileChooser.openDialogTitleText", ScoutTexts.get("Open"));
    defaults.put("FileChooser.cancelButtonText", ScoutTexts.get("Cancel"));
    defaults.put("FileChooser.updateButtonText", ScoutTexts.get("Update"));
    defaults.put("FileChooser.helpButtonText", ScoutTexts.get("Help"));
    defaults.put("ProgressWindow.interruptedText", ScoutTexts.get("Interrupted"));
    defaults.put("ProgressWindow.interruptText", ScoutTexts.get("Cancel"));
    defaults.put("Calendar.condensedText", ScoutTexts.get("Condensed"));
    defaults.put("Calendar.monthText", ScoutTexts.get("Month"));
    defaults.put("Calendar.weekText", ScoutTexts.get("Week"));
    defaults.put("Calendar.workWeekText", ScoutTexts.get("WorkWeek"));
    defaults.put("Calendar.dayText", ScoutTexts.get("Day"));
    defaults.put("Calendar.hourText", ScoutTexts.get("Hour"));
    defaults.put("Calendar.minuteText", ScoutTexts.get("Minute"));
    defaults.put("Calendar.chooseText", ScoutTexts.get("Choose"));
    defaults.put("Calendar.weekShortText", ScoutTexts.get("WeekShort"));
    defaults.put("Calendar.itemUntil", ScoutTexts.get("Calendar_itemUntil"));
    defaults.put("Calendar.itemFrom", ScoutTexts.get("Calendar_itemFrom"));
    defaults.put("Calendar.itemCont", ScoutTexts.get("Calendar_itemCont"));
    defaults.put("Calendar.earlier", ScoutTexts.get("Calendar_earlier"));
    defaults.put("Calendar.later", ScoutTexts.get("Calendar_later"));
    defaults.put("Planner.week", ScoutTexts.get("Week"));
    defaults.put("Planner.doubleWeek", ScoutTexts.get("DoubleWeek"));
    defaults.put("Planner.involvedPersons", ScoutTexts.get("InvolvedPersons"));
    defaults.put("Planner.displayedTimerange", ScoutTexts.get("DisplayedTimerange"));
    defaults.put("Planner.today", ScoutTexts.get("Today"));
    //XXX transient, will be moved to synth and custom L&F
    putIfUndefined(defaults, "Synth.ViewTab.foreground", new ColorUIResource(0x486685));
    putIfUndefined(defaults, "Synth.ViewTab.foregroundSelected", new ColorUIResource(254, 154, 35));
    putIfUndefined(defaults, "Synth.ViewTab.font", new FontUIResource("Arial", Font.PLAIN, 12));
    putIfUndefined(defaults, "Synth.ViewTab.fontSelected", new FontUIResource("Arial", Font.BOLD, 12));
  }

  /**
   * only set value if the existing value inthe map is null or undefined
   */
  protected void putIfUndefined(UIDefaults defaults, Object key, Object defaultValue) {
    if (defaults.get(key) == null) {
      defaults.put(key, defaultValue);
    }
  }

  protected IconUIResource createIconUIResource(ISwingEnvironment env, String resourceSimpleName) {
    Icon icon = env.getIcon(resourceSimpleName);
    if (icon != null) {
      return new IconUIResource(icon);
    }
    else {
      return null;
    }
  }

}
