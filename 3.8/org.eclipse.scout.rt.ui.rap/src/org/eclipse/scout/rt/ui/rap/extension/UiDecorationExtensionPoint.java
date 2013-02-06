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
package org.eclipse.scout.rt.ui.rap.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.ui.IUiDeviceType;
import org.eclipse.scout.rt.ui.rap.Activator;
import org.eclipse.scout.rt.ui.rap.extension.internal.LookAndFeelDecorations;
import org.eclipse.scout.rt.ui.rap.extension.internal.LookAndFeelProperties;
import org.eclipse.scout.rt.ui.rap.extension.internal.UiDecoration;
import org.eclipse.scout.rt.ui.rap.util.DeviceUtility;
import org.eclipse.swt.SWT;

public final class UiDecorationExtensionPoint {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(UiDecorationExtensionPoint.class);
  private static Map<IUiDeviceType, IUiDecoration> LOOK_AND_FEEL_MAP = new HashMap<IUiDeviceType, IUiDecoration>();

  public static final int SCOPE_DEFAULT = 1;
  public static final int SCOPE_GLOBAL = 100;
  private static final String ATTR_SCOPE = "scope";
  private static final String ATTR_DEVICE_TYPE = "deviceType";
  private static final String ATTR_DEVICE_TYPE_KEYWORD_TOUCH = "touch";

  private UiDecorationExtensionPoint() {
  }

  public static synchronized IUiDecoration getLookAndFeel() {
    IUiDeviceType currentDeviceType = DeviceUtility.getCurrentDeviceType();
    if (!LOOK_AND_FEEL_MAP.containsKey(currentDeviceType)) {
      IUiDecoration uiDecoration = loadUiDecoration(currentDeviceType);
      LOOK_AND_FEEL_MAP.put(currentDeviceType, uiDecoration);
    }

    return LOOK_AND_FEEL_MAP.get(currentDeviceType);
  }

  private static ILookAndFeelDecorations parseDecorations(IConfigurationElement decorationsElement) {
    LookAndFeelDecorations decorations = new LookAndFeelDecorations();
    decorations.setScope(getScopePriority(decorationsElement.getAttribute(ATTR_SCOPE)));
    decorations.setDeviceTypeIdentifier(decorationsElement.getAttribute(ATTR_DEVICE_TYPE));

    IConfigurationElement[] mandatoryElement = decorationsElement.getChildren("mandatory");
    if (mandatoryElement.length > 0) {
      // only 1 mandatory extension
      // background
      IConfigurationElement[] fieldBackground = mandatoryElement[0].getChildren("fieldBackground");
      if (fieldBackground.length > 0) {
        decorations.setMandatoryFieldBackgroundColor(fieldBackground[0].getAttribute("color"));
      }
      // label text color
      IConfigurationElement[] labelTextColor = mandatoryElement[0].getChildren("labelTextColor");
      if (labelTextColor.length > 0) {
        decorations.setMandatoryLabelTextColor(labelTextColor[0].getAttribute("color"));
      }
      // label Font
      IConfigurationElement[] labelFont = mandatoryElement[0].getChildren("labelFont");
      if (labelFont.length > 0) {
        FontSpec spec = FontSpec.parse(labelFont[0].getAttribute("font"));
        decorations.setMandatoryLabelFont(spec);
      }
      // star marker
      IConfigurationElement[] starMarker = mandatoryElement[0].getChildren("starMarker");
      if (starMarker.length > 0) {
        decorations.setStarMarkerPosition(parseStarMarkerPosition(starMarker[0].getAttribute("placement")));
      }
    }
    return decorations;
  }

  private static ILookAndFeelProperties parseProperties(IConfigurationElement propertiesElement) {
    LookAndFeelProperties props = new LookAndFeelProperties();
    props.setScope(getScopePriority(propertiesElement.getAttribute(ATTR_SCOPE)));
    props.setDeviceTypeIdentifier(propertiesElement.getAttribute(ATTR_DEVICE_TYPE));

    IConfigurationElement[] properties = propertiesElement.getChildren("property");
    for (IConfigurationElement prop : properties) {
      String propName = null, propValue = null;
      propName = prop.getAttribute("name");
      propValue = prop.getAttribute("value");
      props.setProperty(propName, propValue);
      props.setContributor(propertiesElement.getContributor().getName());
    }
    return props;
  }

  private static int getScopePriority(String scope) {
    int prio = SCOPE_DEFAULT;
    if (StringUtility.isNullOrEmpty(scope) || scope.equalsIgnoreCase("default")) {
      prio = SCOPE_DEFAULT;
    }
    else if (scope.equalsIgnoreCase("global")) {
      prio = SCOPE_GLOBAL;
    }
    return prio;
  }

  private static int parseStarMarkerPosition(String value) {
    int pos = ILookAndFeelDecorations.STAR_MARKER_NONE;
    if (StringUtility.isNullOrEmpty(value) || value.equalsIgnoreCase("afterLabel")) {
      pos = ILookAndFeelDecorations.STAR_MARKER_AFTER_LABEL;
    }
    else if (value.equalsIgnoreCase("beforeLabel")) {
      pos = ILookAndFeelDecorations.STAR_MARKER_BEFORE_LABEL;
    }
    return pos;
  }

  private static IUiDecoration loadUiDecoration(IUiDeviceType deviceType) {

    ArrayList<ILookAndFeelProperties> propertyExtensions = new ArrayList<ILookAndFeelProperties>();
    ArrayList<ILookAndFeelDecorations> decorationExtensions = new ArrayList<ILookAndFeelDecorations>();

    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IExtensionPoint xp = reg.getExtensionPoint(Activator.PLUGIN_ID, "lookAndFeel");
    IExtension[] extensions = xp.getExtensions();

    for (IExtension extension : extensions) {
      IConfigurationElement[] elements = extension.getConfigurationElements();
      for (IConfigurationElement element : elements) {
        if ("properties".equals(element.getName())) {
          propertyExtensions.add(parseProperties(element));
        }
        else if ("decorations".equals(element.getName())) {
          decorationExtensions.add(parseDecorations(element));
        }
      }
    }

    UiDecoration uiDecoration = new UiDecoration();
    loadDecorations(uiDecoration, decorationExtensions, deviceType);
    loadProperties(uiDecoration, propertyExtensions, deviceType);

    return uiDecoration;
  }

  private static void loadProperties(UiDecoration uiDecoration, ArrayList<ILookAndFeelProperties> propertyExtensions, IUiDeviceType deviceType) {
    TreeMap<Integer, ILookAndFeelProperties> properties = new TreeMap<Integer, ILookAndFeelProperties>();
    for (ILookAndFeelProperties props : propertyExtensions) {
      int scope = props.getScope();
      String deviceTypeIdentifier = props.getDeviceTypeIdentifier();
      int priority = computePriority(scope, deviceTypeIdentifier);

      if (isPropsAlreadLoaded(properties, priority, deviceTypeIdentifier)) {
        LOG.warn("Multiple look and feel properties found with scope '" + scope + "' and deviceType '" + deviceTypeIdentifier + "'");
      }
      else if (matchesDeviceType(deviceType, deviceTypeIdentifier)) {
        properties.put(priority, props);
      }
    }

    for (ILookAndFeelProperties props : properties.values()) {
      if (props.getPropertyInt(ILookAndFeelProperties.PROP_DIALOG_MIN_HEIGHT) != 0) {
        uiDecoration.setDialogMinHeight(props.getPropertyInt(ILookAndFeelProperties.PROP_DIALOG_MIN_HEIGHT));
      }
      if (props.getPropertyInt(ILookAndFeelProperties.PROP_DIALOG_MIN_WIDTH) != 0) {
        uiDecoration.setDialogMinWidth(props.getPropertyInt(ILookAndFeelProperties.PROP_DIALOG_MIN_WIDTH));
      }
      if (props.existsProperty(ILookAndFeelProperties.PROP_FORM_MAINBOX_BORDER_VISIBLE)) {
        uiDecoration.setFormMainBoxBorderVisible(props.getPropertyBool(ILookAndFeelProperties.PROP_FORM_MAINBOX_BORDER_VISIBLE));
      }
      if (props.getPropertyInt(ILookAndFeelProperties.PROP_FORM_FIELD_LABEL_WIDTH) != 0) {
        uiDecoration.setFormFieldLabelWidth(props.getPropertyInt(ILookAndFeelProperties.PROP_FORM_FIELD_LABEL_WIDTH));
      }
      if (props.getPropertyString(ILookAndFeelProperties.PROP_FORM_FIELD_LABEL_ALIGNMENT) != null) {
        String extFormFieldAlignment = props.getPropertyString(ILookAndFeelProperties.PROP_FORM_FIELD_LABEL_ALIGNMENT);
        if ("center".equalsIgnoreCase(extFormFieldAlignment)) {
          uiDecoration.setFormFieldLabelAlignment(SWT.CENTER);
        }
        else if ("left".equalsIgnoreCase(extFormFieldAlignment)) {
          uiDecoration.setFormFieldLabelAlignment(SWT.LEFT);
        }
        else if ("right".equalsIgnoreCase(extFormFieldAlignment)) {
          uiDecoration.setFormFieldLabelAlignment(SWT.RIGHT);
        }
        else {
          LOG.warn("the value '" + extFormFieldAlignment + "' is not valid for the property '" + ILookAndFeelProperties.PROP_FORM_FIELD_LABEL_ALIGNMENT + "'. Expected values are[right,left,center]");
        }
      }
      if (props.getPropertyInt(ILookAndFeelProperties.PROP_LOGICAL_GRID_LAYOUT_DEFAULT_COLUMN_WIDTH) != 0) {
        uiDecoration.setLogicalGridLayoutDefaultColumnWidth(props.getPropertyInt(ILookAndFeelProperties.PROP_LOGICAL_GRID_LAYOUT_DEFAULT_COLUMN_WIDTH));
      }
      if (props.getPropertyInt(ILookAndFeelProperties.PROP_LOGICAL_GRID_LAYOUT_DEFAULT_POPUP_WIDTH) != 0) {
        uiDecoration.setLogicalGridLayoutDefaultPopupWidth(props.getPropertyInt(ILookAndFeelProperties.PROP_LOGICAL_GRID_LAYOUT_DEFAULT_POPUP_WIDTH));
      }
      if (props.getPropertyInt(ILookAndFeelProperties.PROP_LOGICAL_GRID_LAYOUT_HORIZONTAL_GAP) != 0) {
        uiDecoration.setLogicalGridLayoutHorizontalGap(props.getPropertyInt(ILookAndFeelProperties.PROP_LOGICAL_GRID_LAYOUT_HORIZONTAL_GAP));
      }
      if (props.getPropertyInt(ILookAndFeelProperties.PROP_LOGICAL_GRID_LAYOUT_ROW_HEIGHT) != 0) {
        uiDecoration.setLogicalGridLayoutRowHeight(props.getPropertyInt(ILookAndFeelProperties.PROP_LOGICAL_GRID_LAYOUT_ROW_HEIGHT));
      }
      if (props.getPropertyInt(ILookAndFeelProperties.PROP_LOGICAL_GRID_LAYOUT_VERTICAL_GAP) != 0) {
        uiDecoration.setLogicalGridLayoutVerticalGap(props.getPropertyInt(ILookAndFeelProperties.PROP_LOGICAL_GRID_LAYOUT_VERTICAL_GAP));
      }
      if (props.getPropertyInt(ILookAndFeelProperties.PROP_PROCESS_BUTTON_HEIGHT) != 0) {
        uiDecoration.setProcessButtonHeight(props.getPropertyInt(ILookAndFeelProperties.PROP_PROCESS_BUTTON_HEIGHT));
      }
      if (props.getPropertyInt(ILookAndFeelProperties.PROP_PROCESS_BUTTON_MAX_WIDTH) != 0) {
        uiDecoration.setProcessButtonMaxWidth(props.getPropertyInt(ILookAndFeelProperties.PROP_PROCESS_BUTTON_MAX_WIDTH));
      }
      if (props.getPropertyInt(ILookAndFeelProperties.PROP_PROCESS_BUTTON_MIN_WIDTH) != 0) {
        uiDecoration.setProcessButtonMinWidth(props.getPropertyInt(ILookAndFeelProperties.PROP_PROCESS_BUTTON_MIN_WIDTH));
      }
      if (props.getPropertyString(ILookAndFeelProperties.PROP_COLOR_FOREGROUND_DISABLED) != null) {
        uiDecoration.setColorForegroundDisabled(props.getPropertyString(ILookAndFeelProperties.PROP_COLOR_FOREGROUND_DISABLED));
      }
      if (props.getPropertyString(ILookAndFeelProperties.PROP_MESSAGE_BOX_MIN_WIDTH) != null) {
        uiDecoration.setMessageBoxMinWidth(props.getPropertyInt(ILookAndFeelProperties.PROP_MESSAGE_BOX_MIN_WIDTH));
      }
      if (props.getPropertyString(ILookAndFeelProperties.PROP_MESSAGE_BOX_MIN_HEIGHT) != null) {
        uiDecoration.setMessageBoxMinHeight(props.getPropertyInt(ILookAndFeelProperties.PROP_MESSAGE_BOX_MIN_HEIGHT));
      }
      if (props.getPropertyString(ILookAndFeelProperties.PROP_FORM_FIELD_SELECT_ALL_ON_FOCUS_ENABLED) != null) {
        uiDecoration.setFormFieldSelectAllOnFocusEnabled(props.getPropertyBool(ILookAndFeelProperties.PROP_FORM_FIELD_SELECT_ALL_ON_FOCUS_ENABLED));
      }
      if (props.getPropertyString(ILookAndFeelProperties.PROP_DND_SUPPORT_ENABLED) != null) {
        uiDecoration.setDndSupportEnabled(props.getPropertyBool(ILookAndFeelProperties.PROP_DND_SUPPORT_ENABLED));
      }
      if (props.getPropertyString(ILookAndFeelProperties.PROP_BROWSER_HISTORY_ENABLED) != null) {
        uiDecoration.setBrowserHistoryEnabled(props.getPropertyBool(ILookAndFeelProperties.PROP_BROWSER_HISTORY_ENABLED));
      }
      if (props.getPropertyString(ILookAndFeelProperties.PROP_TABLE_ROW_HEIGHT) != null) {
        uiDecoration.setTableRowHeight(props.getPropertyInt(ILookAndFeelProperties.PROP_TABLE_ROW_HEIGHT));
      }
      if (props.getPropertyString(ILookAndFeelProperties.PROP_TREE_NODE_HEIGHT) != null) {
        uiDecoration.setTreeNodeHeight(props.getPropertyInt(ILookAndFeelProperties.PROP_TREE_NODE_HEIGHT));
      }

    }
  }

  private static void loadDecorations(UiDecoration uiDecoration, ArrayList<ILookAndFeelDecorations> decorationExtensions, IUiDeviceType deviceType) {
    TreeMap<Integer, ILookAndFeelDecorations> decorations = new TreeMap<Integer, ILookAndFeelDecorations>();
    for (ILookAndFeelDecorations dec : decorationExtensions) {
      int scope = dec.getScope();
      String deviceTypeIdentifier = dec.getDeviceTypeIdentifier();
      int priority = computePriority(scope, deviceTypeIdentifier);

      if (isDecosAlreadLoaded(decorations, priority, deviceTypeIdentifier)) {
        LOG.warn("Multiple look and feel decorations found with scope '" + scope + "' and deviceType '" + deviceTypeIdentifier + "'.");
      }
      else if (matchesDeviceType(deviceType, deviceTypeIdentifier)) {
        decorations.put(priority, dec);
      }
    }
    if (decorations.size() > 0) {
      ILookAndFeelDecorations dec = decorations.get(decorations.lastKey());
      if (dec.getMandatoryFieldBackgroundColor() != null) {
        uiDecoration.setMandatoryFieldBackgroundColor(dec.getMandatoryFieldBackgroundColor());
      }
      if (dec.getMandatoryLabelFont() != null) {
        uiDecoration.setMandatoryLabelFont(dec.getMandatoryLabelFont());
      }
      if (dec.getMandatoryLabelTextColor() != null) {
        uiDecoration.setMandatoryLabelTextColor(dec.getMandatoryLabelTextColor());
      }
      if (dec.getStarMarkerPosition() != ILookAndFeelDecorations.STAR_MARKER_NONE) {
        uiDecoration.setMandatoryStarMarkerPosition(dec.getStarMarkerPosition());
      }
    }
  }

  private static boolean isPropsAlreadLoaded(TreeMap<Integer, ILookAndFeelProperties> properties, Integer priority, String deviceTypeIdentifier) {
    ILookAndFeelProperties alreadyLoadedProps = properties.get(priority);
    if (alreadyLoadedProps == null) {
      return false;
    }
    if (deviceTypeIdentifier != null && alreadyLoadedProps.getDeviceTypeIdentifier().equalsIgnoreCase(deviceTypeIdentifier)) {
      return true;
    }

    return true;
  }

  private static boolean isDecosAlreadLoaded(TreeMap<Integer, ILookAndFeelDecorations> docorations, Integer priority, String deviceTypeIdentifier) {
    ILookAndFeelDecorations alreadyLoadedDecos = docorations.get(priority);
    if (alreadyLoadedDecos == null) {
      return false;
    }
    if (deviceTypeIdentifier != null && alreadyLoadedDecos.getDeviceTypeIdentifier().equalsIgnoreCase(deviceTypeIdentifier)) {
      return true;
    }

    return true;
  }

  private static boolean matchesDeviceType(IUiDeviceType uiDeviceType, String deviceTypeIdentifier) {
    if (deviceTypeIdentifier == null) {
      return true;
    }

    if (uiDeviceType.getIdentifier().equalsIgnoreCase(deviceTypeIdentifier)) {
      return true;
    }

    if (deviceTypeIdentifier.equalsIgnoreCase(ATTR_DEVICE_TYPE_KEYWORD_TOUCH) && uiDeviceType.isTouchDevice()) {
      return true;
    }

    return false;
  }

  /**
   * Computes the priority in which the properties and decorations should be considered.<br>
   * A greater value means higher priority and overrides values with lower priority.
   * <p>
   * Basically the scope is used as priority. If a deviceTypeIdentifier is specified it gets a little higher priority
   * than scope which makes it possible to define default values for each device type. <br>
   * If the deviceTypeIdentifier is not a concrete device type but covers more than one device types, it gets lower
   * priority than a concrete device type.
   */
  private static int computePriority(int scope, String deviceTypeIdentifier) {
    if (!StringUtility.hasText(deviceTypeIdentifier)) {
      return scope;
    }

    if (isCombinedDeviceTypeIdentifier(deviceTypeIdentifier)) {
      return scope + 1;
    }

    return scope + 2;
  }

  private static boolean isCombinedDeviceTypeIdentifier(String deviceTypeIdentifier) {
    if (deviceTypeIdentifier == null) {
      return false;
    }

    return deviceTypeIdentifier.equalsIgnoreCase(ATTR_DEVICE_TYPE_KEYWORD_TOUCH);
  }

}
