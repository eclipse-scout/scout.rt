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
package org.eclipse.scout.rt.ui.swt.extension;

import java.util.ArrayList;
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
import org.eclipse.scout.rt.ui.swt.Activator;
import org.eclipse.scout.rt.ui.swt.extension.internal.LookAndFeelDecorations;
import org.eclipse.scout.rt.ui.swt.extension.internal.LookAndFeelProperties;
import org.eclipse.scout.rt.ui.swt.extension.internal.UiDecoration;
import org.eclipse.swt.SWT;

public final class UiDecorationExtensionPoint {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(UiDecorationExtensionPoint.class);
  private static final UiDecoration LOOK_AND_FEEL;

  public static final int SCOPE_DEFAULT = 1;
  public static final int SCOPE_GLOBAL = 2;

  private UiDecorationExtensionPoint() {
  }

  public static IUiDecoration getLookAndFeel() {
    return LOOK_AND_FEEL;
  }

  private static ILookAndFeelDecorations parseDecorations(IConfigurationElement decorationsElement) {
    LookAndFeelDecorations decorations = new LookAndFeelDecorations();
    decorations.setScope(getScopePriority(decorationsElement.getAttribute("scope")));

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
    props.setScope(getScopePriority(propertiesElement.getAttribute("scope")));
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
    if (StringUtility.isNullOrEmpty(value) || value.equalsIgnoreCase("beforeLabel")) {
      pos = ILookAndFeelDecorations.STAR_MARKER_BEFORE_LABEL;
    }
    else if (value.equalsIgnoreCase("afterLabel")) {
      pos = ILookAndFeelDecorations.STAR_MARKER_AFTER_LABEL;
    }
    return pos;
  }

  static {
    LOOK_AND_FEEL = new UiDecoration();

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
    // decorations
    TreeMap<Integer, ILookAndFeelDecorations> decorations = new TreeMap<Integer, ILookAndFeelDecorations>();
    for (ILookAndFeelDecorations dec : decorationExtensions) {
      int scope = dec.getScope();
      if (decorations.get(scope) != null) {
        LOG.warn("multiple look and feel extension found with scope '" + scope + "'");
      }
      else {
        decorations.put(scope, dec);
      }
    }
    if (decorations.size() > 0) {
      ILookAndFeelDecorations dec = decorations.get(decorations.lastKey());
      if (dec.getMandatoryFieldBackgroundColor() != null) {
        LOOK_AND_FEEL.setMandatoryFieldBackgroundColor(dec.getMandatoryFieldBackgroundColor());
      }
      if (dec.getMandatoryLabelFont() != null) {
        LOOK_AND_FEEL.setMandatoryLabelFont(dec.getMandatoryLabelFont());
      }
      if (dec.getMandatoryLabelTextColor() != null) {
        LOOK_AND_FEEL.setMandatoryLabelTextColor(dec.getMandatoryLabelTextColor());
      }
      if (dec.getStarMarkerPosition() != ILookAndFeelDecorations.STAR_MARKER_NONE) {
        LOOK_AND_FEEL.setMandatoryStarMarkerPosition(dec.getStarMarkerPosition());
      }
    }
    // properties
    TreeMap<Integer, ILookAndFeelProperties> properties = new TreeMap<Integer, ILookAndFeelProperties>();
    for (ILookAndFeelProperties props : propertyExtensions) {
      int scope = props.getScope();
      if (properties.get(scope) != null) {
        LOG.warn("multiple look and feel extension found with scope '" + scope + "'");
      }
      else {
        properties.put(scope, props);
      }
    }
    for (ILookAndFeelProperties props : properties.values()) {
      if (props.getPropertyInt(ILookAndFeelProperties.PROP_DIALOG_MIN_HEIGHT) != 0) {
        LOOK_AND_FEEL.setDialogMinHeight(props.getPropertyInt(ILookAndFeelProperties.PROP_DIALOG_MIN_HEIGHT));
      }
      if (props.getPropertyInt(ILookAndFeelProperties.PROP_DIALOG_MIN_WIDTH) != 0) {
        LOOK_AND_FEEL.setDialogMinWidth(props.getPropertyInt(ILookAndFeelProperties.PROP_DIALOG_MIN_WIDTH));
      }
      int propActivationButtonHeight = props.getPropertyInt(ILookAndFeelProperties.PROP_FORM_FIELD_ACTIVATION_BUTTON_HEIGHT);
      if (propActivationButtonHeight > 0) {
        LOOK_AND_FEEL.setFormFieldActivationButtonHeight(propActivationButtonHeight);
      }
      else {
        LOOK_AND_FEEL.setFormFieldActivationButtonHeight(getSystemRowHeight());
      }
      if (props.getPropertyInt(ILookAndFeelProperties.PROP_FORM_FIELD_ACTIVATION_BUTTON_WIDTH) != 0) {
        LOOK_AND_FEEL.setFormFieldActivationButtonWidth(props.getPropertyInt(ILookAndFeelProperties.PROP_FORM_FIELD_ACTIVATION_BUTTON_WIDTH));
      }
      if (props.getPropertyInt(ILookAndFeelProperties.PROP_FORM_FIELD_ACTIVATION_BUTTON_WITH_MENU_WIDTH) != 0) {
        LOOK_AND_FEEL.setFormFieldActivationButtonWithMenuWidth(props.getPropertyInt(ILookAndFeelProperties.PROP_FORM_FIELD_ACTIVATION_BUTTON_WITH_MENU_WIDTH));
      }
      if (props.getPropertyInt(ILookAndFeelProperties.PROP_FORM_FIELD_LABEL_WIDTH) != 0) {
        LOOK_AND_FEEL.setFormFieldLabelWidth(props.getPropertyInt(ILookAndFeelProperties.PROP_FORM_FIELD_LABEL_WIDTH));
      }
      if (props.getPropertyString(ILookAndFeelProperties.PROP_FORM_FIELD_LABEL_ALIGNMENT) != null) {
        String extFormFieldAlignment = props.getPropertyString(ILookAndFeelProperties.PROP_FORM_FIELD_LABEL_ALIGNMENT);
        if ("center".equalsIgnoreCase(extFormFieldAlignment)) {
          LOOK_AND_FEEL.setFormFieldLabelAlignment(SWT.CENTER);
        }
        else if ("left".equalsIgnoreCase(extFormFieldAlignment)) {
          LOOK_AND_FEEL.setFormFieldLabelAlignment(SWT.LEFT);
        }
        else if ("right".equalsIgnoreCase(extFormFieldAlignment)) {
          LOOK_AND_FEEL.setFormFieldLabelAlignment(SWT.RIGHT);
        }
        else {
          LOG.warn("the value '" + extFormFieldAlignment + "' is not valid for the property '" + ILookAndFeelProperties.PROP_FORM_FIELD_LABEL_ALIGNMENT + "'. Expected values are[right,left,center]");
        }
      }
      if (props.getPropertyInt(ILookAndFeelProperties.PROP_LOGICAL_GRID_LAYOUT_DEFAULT_COLUMN_WIDTH) != 0) {
        LOOK_AND_FEEL.setLogicalGridLayoutDefaultColumnWidth(props.getPropertyInt(ILookAndFeelProperties.PROP_LOGICAL_GRID_LAYOUT_DEFAULT_COLUMN_WIDTH));
      }
      if (props.getPropertyInt(ILookAndFeelProperties.PROP_LOGICAL_GRID_LAYOUT_HORIZONTAL_GAP) != 0) {
        LOOK_AND_FEEL.setLogicalGridLayoutHorizontalGap(props.getPropertyInt(ILookAndFeelProperties.PROP_LOGICAL_GRID_LAYOUT_HORIZONTAL_GAP));
      }
      int gridRowHeight = props.getPropertyInt(ILookAndFeelProperties.PROP_LOGICAL_GRID_LAYOUT_ROW_HEIGHT);
      if (gridRowHeight > 1) {
        LOOK_AND_FEEL.setLogicalGridLayoutRowHeight(gridRowHeight);
      }
      else {
        LOOK_AND_FEEL.setLogicalGridLayoutRowHeight(getSystemRowHeight());
      }
      if (props.getPropertyInt(ILookAndFeelProperties.PROP_LOGICAL_GRID_LAYOUT_VERTICAL_GAP) != 0) {
        LOOK_AND_FEEL.setLogicalGridLayoutVerticalGap(props.getPropertyInt(ILookAndFeelProperties.PROP_LOGICAL_GRID_LAYOUT_VERTICAL_GAP));
      }

      if (props.getPropertyInt(ILookAndFeelProperties.PROP_PROCESS_BUTTON_HEIGHT) != 0) {
        LOOK_AND_FEEL.setProcessButtonHeight(props.getPropertyInt(ILookAndFeelProperties.PROP_PROCESS_BUTTON_HEIGHT));
      }
      if (props.getPropertyInt(ILookAndFeelProperties.PROP_PROCESS_BUTTON_MAX_WIDTH) != 0) {
        LOOK_AND_FEEL.setProcessButtonMaxWidth(props.getPropertyInt(ILookAndFeelProperties.PROP_PROCESS_BUTTON_MAX_WIDTH));
      }
      if (props.getPropertyInt(ILookAndFeelProperties.PROP_PROCESS_BUTTON_MIN_WIDTH) != 0) {
        LOOK_AND_FEEL.setProcessButtonMinWidth(props.getPropertyInt(ILookAndFeelProperties.PROP_PROCESS_BUTTON_MIN_WIDTH));
      }
      if (props.getPropertyString(ILookAndFeelProperties.PROP_COLOR_FOREGROUND_DISABLED) != null) {
        LOOK_AND_FEEL.setColorForegroundDisabled(props.getPropertyString(ILookAndFeelProperties.PROP_COLOR_FOREGROUND_DISABLED));
      }
      if (props.getPropertyString(ILookAndFeelProperties.PROP_MESSAGE_BOX_MIN_WIDTH) != null) {
        LOOK_AND_FEEL.setMessageBoxMinWidth(props.getPropertyInt(ILookAndFeelProperties.PROP_MESSAGE_BOX_MIN_WIDTH));
      }
      if (props.getPropertyString(ILookAndFeelProperties.PROP_MESSAGE_BOX_MIN_HEIGHT) != null) {
        LOOK_AND_FEEL.setMessageBoxMinHeight(props.getPropertyInt(ILookAndFeelProperties.PROP_MESSAGE_BOX_MIN_HEIGHT));
      }
      if (props.getPropertyString(ILookAndFeelProperties.PROP_TABLE_MOUSE_MOVE_SELECTION_SUPPORT_ENABLED) != null) {
        LOOK_AND_FEEL.setTableMouseMoveSelectionSupportEnabled(props.getPropertyBool(ILookAndFeelProperties.PROP_TABLE_MOUSE_MOVE_SELECTION_SUPPORT_ENABLED));
      }
      if (props.getPropertyString(ILookAndFeelProperties.PROP_TABLE_MULTILINE_TOOLTIP_SUPPORT_ENABLED) != null) {
        LOOK_AND_FEEL.setTableMultilineTooltipSupportEnabled(props.getPropertyBool(ILookAndFeelProperties.PROP_TABLE_MULTILINE_TOOLTIP_SUPPORT_ENABLED));
      }
    }
  }

  private static int getSystemRowHeight() {
    String osName = Activator.getDefault().getBundle().getBundleContext().getProperty("org.osgi.framework.os.name");
    if (StringUtility.equalsIgnoreCase("WindowsXP", osName)) {
      return 21;
    }
    else {
      return 23;
    }
  }

}
