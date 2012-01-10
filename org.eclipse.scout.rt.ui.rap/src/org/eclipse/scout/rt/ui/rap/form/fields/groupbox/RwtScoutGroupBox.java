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
package org.eclipse.scout.rt.ui.rap.form.fields.groupbox;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.core.DefaultValidateRoot;
import org.eclipse.scout.rt.ui.rap.core.IValidateRoot;
import org.eclipse.scout.rt.ui.rap.ext.ScrolledFormEx;
import org.eclipse.scout.rt.ui.rap.extension.IUiDecoration;
import org.eclipse.scout.rt.ui.rap.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFormFieldGridData;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.Section;

/**
 * <h3>RwtScoutGroupBox</h3>
 */
public class RwtScoutGroupBox extends RwtScoutFieldComposite<IGroupBox> implements IRwtScoutGroupBox {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutGroupBox.class);

  static final String VARIANT_GROUP_BOX_WITH_LINE_FRAME = "groupBoxWithLineFrame";
  static final String VARIANT_LABEL = "GroupBoxLabel";
  static final String VARIANT_LINE = "GroupBoxLine";

  public static class BorderDecoration {
    /**
     * whether there is a border at all (false: panel is simply for grouping of fields)
     */
    public boolean visible;
    /**
     * one of the IGroupBox.BORDER_DECORATION_* values
     */
    public String decoration;
  }

  /**
   * is null if the group box is not a scrolled group box
   */
  private ScrolledFormEx m_scrolledForm;
  private Section m_section;
  private Group m_group;
  private Label m_label;
  private Label m_line;
  private Composite m_bodyPart;
  private RwtScoutGroupBoxButtonbar m_buttonbar;
  // cache
  private BorderDecoration m_borderDecoration;
  private String m_containerLabel;
  private String m_containerImage;

  @Override
  protected void initializeUi(Composite parent) {
    m_borderDecoration = resolveBorderDecoration();
    Composite rootPane = createContainer(parent);
    if (getScoutObject().isScrollable()) {
      m_scrolledForm = getUiEnvironment().getFormToolkit().createScrolledFormEx(rootPane, SWT.V_SCROLL);
      GridData bodyData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL);
      bodyData.horizontalIndent = 0;
      bodyData.verticalIndent = 0;
      m_scrolledForm.setLayoutData(bodyData);
      m_bodyPart = m_scrolledForm.getBody();
    }
    else {
      m_bodyPart = getUiEnvironment().getFormToolkit().createComposite(rootPane);
      GridData bodyData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
      bodyData.horizontalIndent = 0;
      bodyData.verticalIndent = 0;
      m_bodyPart.setLayoutData(bodyData);
    }
    m_bodyPart.setData(IValidateRoot.VALIDATE_ROOT_DATA, new DefaultValidateRoot(m_bodyPart) {
      @Override
      public void validate() {
        super.validate();
        if (m_scrolledForm != null) {
          if (!m_scrolledForm.isDisposed()) {
            m_scrolledForm.reflow(true);
          }
        }
      }
    });
    if (getScoutObject().getCustomProcessButtonCount() + getScoutObject().getSystemProcessButtonCount() > 0) {
      createButtonbar(rootPane);
    }
    IUiDecoration deco = UiDecorationExtensionPoint.getLookAndFeel();
    LogicalGridLayout bodyLayout = new LogicalGridLayout(deco.getLogicalGridLayoutHorizontalGap(), deco.getLogicalGridLayoutVerticalGap());
    m_bodyPart.setLayout(bodyLayout);
    installUiContainerBorder();
    // FIELDS:
    IFormField[] scoutFields = getScoutObject().getControlFields();
    for (IFormField field : scoutFields) {
      IRwtScoutFormField uiScoutComposite = getUiEnvironment().createFormField(m_bodyPart, field);
      RwtScoutFormFieldGridData layoutData = new RwtScoutFormFieldGridData(field);
      uiScoutComposite.getUiContainer().setLayoutData(layoutData);
    }
  }

  protected Composite createButtonbar(Composite parent) {
    m_buttonbar = new RwtScoutGroupBoxButtonbar();
    m_buttonbar.createUiField(parent, getScoutObject(), getUiEnvironment());
    GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    m_buttonbar.getUiContainer().setLayoutData(data);
    return m_buttonbar.getUiContainer();
  }

  protected BorderDecoration resolveBorderDecoration() {
    IGroupBox box = getScoutObject();
    BorderDecoration deco = new BorderDecoration();
    deco.visible = box.isBorderVisible();
    deco.decoration = IGroupBox.BORDER_DECORATION_EMPTY;
    if (IGroupBox.BORDER_DECORATION_SECTION.equals(box.getBorderDecoration())) {
      deco.decoration = IGroupBox.BORDER_DECORATION_SECTION;
      return deco;
    }
    if (IGroupBox.BORDER_DECORATION_LINE.equals(box.getBorderDecoration())) {
      deco.decoration = IGroupBox.BORDER_DECORATION_LINE;
      return deco;
    }
    if (IGroupBox.BORDER_DECORATION_EMPTY.equals(box.getBorderDecoration())) {
      deco.decoration = IGroupBox.BORDER_DECORATION_EMPTY;
      return deco;
    }
    if (!IGroupBox.BORDER_DECORATION_AUTO.equals(box.getBorderDecoration())) {
      deco.decoration = IGroupBox.BORDER_DECORATION_EMPTY;
      return deco;
    }
    // auto: best guess
    if (box.isExpandable()) {
      deco.decoration = IGroupBox.BORDER_DECORATION_SECTION;
      return deco;
    }
    if (box.isMainBox()) {
      if (UiDecorationExtensionPoint.getLookAndFeel().isFormMainBoxBorderVisible()) {
        deco.decoration = IGroupBox.BORDER_DECORATION_EMPTY;
        return deco;
      }
      deco.visible = false;
      return deco;
    }
    if (box.getParentField() instanceof ITabBox) {
      deco.decoration = IGroupBox.BORDER_DECORATION_EMPTY;
      return deco;
    }
    deco.decoration = IGroupBox.BORDER_DECORATION_LINE;
    return deco;
  }

  protected Composite createContainer(Composite parent) {
    Composite rootPane;
    GridLayout layout = new GridLayout(1, true);
    setUiLabel(null);
    //
    if (m_borderDecoration.visible) {
      if (IGroupBox.BORDER_DECORATION_SECTION.equals(m_borderDecoration.decoration)) {
        // section
        int style = (getScoutObject().isExpanded() ? Section.EXPANDED : 0) | Section.TITLE_BAR;
        if (getScoutObject().isExpandable()) {
          style |= Section.TWISTIE;
        }
        m_section = getUiEnvironment().getFormToolkit().createSection(parent, style);
        String label = getScoutObject().getLabel();
        if (label == null) {
          label = "";
        }
        m_section.setText(label);
        m_section.addExpansionListener(new P_ExpansionListener());
        setUiContainer(m_section);
        //
        rootPane = getUiEnvironment().getFormToolkit().createSectionClient(m_section);
        m_section.setClient(rootPane);
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.marginTop = 5;
        layout.marginRight = 7;
        layout.marginBottom = 5;
        layout.marginLeft = 5;
        //margins are NOT handled in CSS
        rootPane.setLayout(layout);
      }
      else if (IGroupBox.BORDER_DECORATION_LINE.equals(m_borderDecoration.decoration)) {
        Composite groupComp = getUiEnvironment().getFormToolkit().createComposite(parent, SWT.NONE);
        m_label = getUiEnvironment().getFormToolkit().createLabel(groupComp, "", SWT.NONE);
        m_label.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_LABEL);
        m_line = getUiEnvironment().getFormToolkit().createLabel(groupComp, "", SWT.NONE);
        m_line.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_LINE);
        rootPane = getUiEnvironment().getFormToolkit().createComposite(groupComp, SWT.NONE);
        rootPane.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_GROUP_BOX_WITH_LINE_FRAME);
        setUiContainer(groupComp);
        //
        GridLayout groupCompLayout = new GridLayout(1, false);
        groupCompLayout.horizontalSpacing = 0;
        groupCompLayout.marginHeight = 0;
        groupCompLayout.marginWidth = 0;
        groupCompLayout.verticalSpacing = 0;
        groupComp.setLayout(groupCompLayout);

        GridData labelLayoutData = new GridData();
        labelLayoutData.horizontalIndent = 14;
        m_label.setLayoutData(labelLayoutData);

        GridData lineLayoutData = new GridData(GridData.FILL_HORIZONTAL);
        lineLayoutData.heightHint = 1;
        m_line.setLayoutData(lineLayoutData);

        GridData groupLayoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
        rootPane.setLayoutData(groupLayoutData);

        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        //margins are handled in CSS
        rootPane.setLayout(layout);
      }
      else {
        // empty
        Group emptyGroup = getUiEnvironment().getFormToolkit().createGroup(parent, 0/* SWT.SHADOW_ETCHED_IN*/);
        rootPane = emptyGroup;
        rootPane.setData(WidgetUtil.CUSTOM_VARIANT, RwtUtility.VARIANT_EMPTY);
        setUiContainer(rootPane);
        //
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        //margins are handled in CSS
        rootPane.setLayout(layout);
      }
    }
    else {
      rootPane = getUiEnvironment().getFormToolkit().createComposite(parent);
      setUiContainer(rootPane);
      //
      layout.horizontalSpacing = 0;
      layout.verticalSpacing = 0;
      layout.marginWidth = 0;
      layout.marginHeight = 0;
      rootPane.setLayout(layout);
    }
    return rootPane;
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    updateBackgroundImageFromScout();
    updateBackgroundImageHorizontalAlignFromScout();
    updateBackgroundImageVerticalAlignFromScout();
    setExpandedFromScout();
  }

  protected void setExpandedFromScout() {
    if (getScoutObject().isExpandable()) {
      if (m_section != null) {
        //only if necessary
        if (m_section.isExpanded() != getScoutObject().isExpanded()) {
          m_section.setExpanded(getScoutObject().isExpanded());
        }
      }
    }
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    // deactivated
  }

  // override to set outer border to line border
  @Override
  protected void setLabelFromScout(String s) {
    if (s == null) {
      s = "";
    }
    if (m_section != null) {
      m_section.setText(s);
      m_section.layout(true, true);
    }
    if (m_group != null) {
      m_group.setText(s);
    }
    if (m_label != null && m_line != null) {
      m_label.setText(s);
      if (StringUtility.hasText(s)) {
        ((GridData) m_label.getLayoutData()).exclude = false;
        m_label.setVisible(true);
        ((GridData) m_line.getLayoutData()).exclude = false;
        m_line.setVisible(true);
      }
      else {
        ((GridData) m_label.getLayoutData()).exclude = true;
        m_label.setVisible(false);
        ((GridData) m_line.getLayoutData()).exclude = true;
        m_line.setVisible(false);
      }
    }
  }

  protected void updateBackgroundImageFromScout() {
    String imageName = getScoutObject().getBackgroundImageName();
    if (imageName == m_containerImage || imageName != null && imageName.equals(m_containerImage)) {
      // nop
    }
    else {
      m_containerImage = imageName;
      installUiContainerBorder();
    }
  }

  protected void updateBackgroundImageHorizontalAlignFromScout() {
    // not implemented
  }

  protected void updateBackgroundImageVerticalAlignFromScout() {
    // not implemented
  }

  private void installUiContainerBorder() {
  }

  /**
   * scout property observer
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IGroupBox.PROP_EXPANDED)) {
      setExpandedFromScout();
    }
    else if (name.equals(IGroupBox.PROP_BACKGROUND_IMAGE_NAME)) {
      updateBackgroundImageFromScout();
    }
    else if (name.equals(IGroupBox.PROP_BACKGROUND_IMAGE_HORIZONTAL_ALIGNMENT)) {
      updateBackgroundImageHorizontalAlignFromScout();
    }
    else if (name.equals(IGroupBox.PROP_BACKGROUND_IMAGE_VERTICAL_ALIGNMENT)) {
      updateBackgroundImageVerticalAlignFromScout();
    }
  }

  protected void handleUiGroupBoxExpanded(final boolean expanded) {
    //notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().setExpandedFromUI(expanded);
      }
    };
    getUiEnvironment().invokeScoutLater(t, 0);
    //end notify
  }

  private class P_ExpansionListener extends ExpansionAdapter {
    @Override
    public void expansionStateChanged(ExpansionEvent e) {
      handleUiGroupBoxExpanded(e.getState());
    }
  } // end class P_ExpansionListener

}
