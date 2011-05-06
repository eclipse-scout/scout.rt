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
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIDefaults;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.IconUIResource;
import javax.swing.plaf.InsetsUIResource;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swing.Activator;
import org.eclipse.scout.rt.ui.swing.SwingIcons;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.splash.SplashWindow;
import org.osgi.framework.Bundle;

/**
 * Sets the default ui properties used in swing scout composites
 */
public class UIDefaultsInjector {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(UIDefaultsInjector.class);

  public UIDefaultsInjector() {
  }

  /**
   * used by swingscout widgets and {@link SplashWindow}
   */
  public void inject(UIDefaults defaults) {
    /*
     * Defaults
     */
    putIfUndefined(defaults, "control", new ColorUIResource(0xffffff));
    putIfUndefined(defaults, "desktop", new ColorUIResource(0xffffff));
    putIfUndefined(defaults, "textInactiveText", new ColorUIResource(0x666666));
    putIfUndefined(defaults, "DateChooser.applyButtonText", SwingUtility.getNlsText("Apply"));
    putIfUndefined(defaults, "GroupBoxButtonBar.border", new BorderUIResource(new EmptyBorder(12, 0, 6, 0)));//XXX synth?
    putIfUndefined(defaults, "GroupBox.border", new BorderUIResource(new EmptyBorder(6, 3, 3, 3)));//XXX synth?
    putIfUndefined(defaults, "Label.defaultHorizontalAlignment", "LEFT");
    putIfUndefined(defaults, "Label.font", new FontUIResource("Dialog", Font.PLAIN, 12));
    putIfUndefined(defaults, "Hyperlink.foreground", new ColorUIResource(0x445599));
    putIfUndefined(defaults, "List.selectionBackground", new ColorUIResource(0x6cbbe4));
    putIfUndefined(defaults, "ListBox.rowHeight", 20);
    putIfUndefined(defaults, "MenuBar.policy", "menubar");
    putIfUndefined(defaults, "PopupMenu.innerBorder", null);
    putIfUndefined(defaults, "Splash.icon", getSplashUIResource());
    putIfUndefined(defaults, "Splash.text", new ColorUIResource(0x0086A6));
    //putIfUndefined(defaults, "Splash.versionLocation", new Point(0,200));
    //putIfUndefined(defaults, "Splash.statusTextLocation", new Point(0,180));
    putIfUndefined(defaults, "StatusBar.StopButton.icon", createIconUIResource(SwingIcons.StatusInterrupt));
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
    putIfUndefined(defaults, "Tooltip.icon", createIconUIResource(SwingIcons.Tooltip));
    putIfUndefined(defaults, "Tree.closedIcon", createIconUIResource(SwingIcons.FolderClosed));
    putIfUndefined(defaults, "Tree.openIcon", createIconUIResource(SwingIcons.FolderOpen));
    putIfUndefined(defaults, "Tree.rowHeight", 24);
    putIfUndefined(defaults, "TreeBox.rowHeight", 20);
    Icon icon = Activator.getIcon("window");
    if (icon != null) { // legacy
      putIfUndefined(defaults, "Window.icon", icon);// must be an ImageIcon, not an IconUIResource
    }
    else {
      // multiple icons for newer versions of Java
      List<Image> icons = new ArrayList<Image>();
      icons.add(Activator.getImage("window16"));
      icons.add(Activator.getImage("window32"));
      icons.add(Activator.getImage("window48"));
      icons.add(Activator.getImage("window256"));
      putIfUndefined(defaults, "Window.icons", icons);
    }

    /*
     * Texts
     */
    defaults.put("OptionPane.okButtonText", SwingUtility.getNlsText("Ok"));
    defaults.put("OptionPane.cancelButtonText", SwingUtility.getNlsText("Cancel"));
    defaults.put("OptionPane.yesButtonText", SwingUtility.getNlsText("Yes"));
    defaults.put("OptionPane.noButtonText", SwingUtility.getNlsText("No"));
    defaults.put("OptionPane.copyPasteHint", SwingUtility.getNlsText("CopyPasteHint"));
    defaults.put("OptionPane.copy", SwingUtility.getNlsText("Copy"));
    defaults.put("FileChooser.lookInLabelText", SwingUtility.getNlsText("LookIn"));
    defaults.put("FileChooser.filesOfTypeLabelText", SwingUtility.getNlsText("FilesOfType"));
    defaults.put("FileChooser.fileNameLabelText", SwingUtility.getNlsText("FileName"));
    defaults.put("FileChooser.saveButtonText", SwingUtility.getNlsText("Save"));
    defaults.put("FileChooser.saveDialogTitleText", SwingUtility.getNlsText("Save"));
    defaults.put("FileChooser.openButtonText", SwingUtility.getNlsText("Open"));
    defaults.put("FileChooser.openDialogTitleText", SwingUtility.getNlsText("Open"));
    defaults.put("FileChooser.cancelButtonText", SwingUtility.getNlsText("Cancel"));
    defaults.put("FileChooser.updateButtonText", SwingUtility.getNlsText("Update"));
    defaults.put("FileChooser.helpButtonText", SwingUtility.getNlsText("Help"));
    defaults.put("ProgressWindow.interruptedText", SwingUtility.getNlsText("Interrupted"));
    defaults.put("ProgressWindow.interruptText", SwingUtility.getNlsText("Cancel"));
    defaults.put("Calendar.condensedText", SwingUtility.getNlsText("Condensed"));
    defaults.put("Calendar.monthText", SwingUtility.getNlsText("Month"));
    defaults.put("Calendar.weekText", SwingUtility.getNlsText("Week"));
    defaults.put("Calendar.workWeekText", SwingUtility.getNlsText("WorkWeek"));
    defaults.put("Calendar.dayText", SwingUtility.getNlsText("Day"));
    defaults.put("Calendar.hourText", SwingUtility.getNlsText("Hour"));
    defaults.put("Calendar.minuteText", SwingUtility.getNlsText("Minute"));
    defaults.put("Calendar.chooseText", SwingUtility.getNlsText("Choose"));
    defaults.put("Calendar.weekShortText", SwingUtility.getNlsText("WeekShort"));
    defaults.put("Calendar.itemUntil", SwingUtility.getNlsText("Calendar_itemUntil"));
    defaults.put("Calendar.itemFrom", SwingUtility.getNlsText("Calendar_itemFrom"));
    defaults.put("Calendar.itemCont", SwingUtility.getNlsText("Calendar_itemCont"));
    defaults.put("Calendar.earlier", SwingUtility.getNlsText("Calendar_earlier"));
    defaults.put("Calendar.later", SwingUtility.getNlsText("Calendar_later"));
    defaults.put("Planner.week", SwingUtility.getNlsText("Week"));
    defaults.put("Planner.doubleWeek", SwingUtility.getNlsText("DoubleWeek"));
    defaults.put("Planner.involvedPersons", SwingUtility.getNlsText("InvolvedPersons"));
    defaults.put("Planner.displayedTimerange", SwingUtility.getNlsText("DisplayedTimerange"));
    defaults.put("Planner.today", SwingUtility.getNlsText("Today"));
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

  protected IconUIResource createIconUIResource(String resourceSimpleName) {
    Icon icon = Activator.getIcon(resourceSimpleName);
    if (icon != null) {
      return new IconUIResource(icon);
    }
    else {
      return null;
    }
  }

  protected String[] m_splashExtensions = new String[]{"png", "jpg", "bmp"};

  protected IconUIResource getSplashUIResource() {
    IconUIResource iconresource = null;
    String splashPathProp = Activator.getDefault().getBundle().getBundleContext().getProperty("osgi.splashPath");
    try {
      if (!StringUtility.isNullOrEmpty(splashPathProp)) {
        Path p = new Path(splashPathProp);
        String bunldeName = p.lastSegment();
        Bundle splashBundle = Platform.getBundle(bunldeName);
        if (splashBundle != null) {
          for (String ext : m_splashExtensions) {
            String imageName = "splash." + ext;
            URL[] entries = FileLocator.findEntries(splashBundle, new Path(imageName));
            if (entries != null && entries.length > 0) {
              URL splashUrl = entries[entries.length - 1];
              if (splashUrl != null) {
                Image img = Toolkit.getDefaultToolkit().createImage(IOUtility.getContent(splashUrl.openStream()));
                if (img != null) {
                  iconresource = new IconUIResource(new ImageIcon(img, imageName));
                  break;
                }
              }
            }
          }
        }
      }
    }
    catch (Exception e) {
      LOG.error("could not find splash for config.ini property 'osgi.splashPath' -> value '" + splashPathProp + "'.", e);
    }
    if (iconresource == null) {
      iconresource = createIconUIResource("splash");
    }
    return iconresource;
  }

}
