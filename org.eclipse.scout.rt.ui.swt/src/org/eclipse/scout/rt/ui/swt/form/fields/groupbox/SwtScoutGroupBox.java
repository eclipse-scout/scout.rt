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
package org.eclipse.scout.rt.ui.swt.form.fields.groupbox;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.ui.swt.DefaultValidateRoot;
import org.eclipse.scout.rt.ui.swt.IValidateRoot;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.ext.ScrolledFormEx;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.extension.IUiDecoration;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.swt.form.fields.ISwtScoutFormField;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFormFieldGridData;
import org.eclipse.scout.rt.ui.swt.window.ISwtScoutPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.Section;

/**
 * <h3>SwtScoutGroupBox</h3>
 */
public class SwtScoutGroupBox extends SwtScoutFieldComposite<IGroupBox> implements ISwtScoutGroupBox {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutGroupBox.class);

  /**
   * is null if the group box is not a scrolled group box
   */
  private ScrolledFormEx m_scrolledForm;
  private Composite m_swtBodyPart;
  private SwtScoutGroupBoxButtonbar m_swtButtonbar;
  private Section m_swtSection;
  private Group m_swtGroup;
  // cache
  private boolean m_containerBorderEnabled;
  private String m_containerBorderDecorationResolved;
  private String m_containerLabel;
  private String m_containerImage;

  @Override
  protected void initializeSwt(Composite parent) {
    m_containerBorderDecorationResolved = resolveBorderDecoration(getScoutObject());
    m_containerBorderEnabled = getScoutObject().isBorderVisible();
    Composite rootPane = createContainer(parent);
    if (getScoutObject().isScrollable()) {
      m_scrolledForm = getEnvironment().getFormToolkit().createScrolledFormEx(rootPane, SWT.V_SCROLL);
      m_swtBodyPart = m_scrolledForm.getBody();
      m_scrolledForm.setData(ISwtScoutPart.MARKER_SCOLLED_FORM, new Object());

      GridData bodyData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL);
      bodyData.horizontalIndent = 0;
      bodyData.verticalIndent = 0;
      m_scrolledForm.setLayoutData(bodyData);

    }
    else {
      m_swtBodyPart = getEnvironment().getFormToolkit().createComposite(rootPane);
      GridData bodyData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL);
      bodyData.horizontalIndent = 0;
      bodyData.verticalIndent = 0;
      m_swtBodyPart.setLayoutData(bodyData);
    }
    m_swtBodyPart.setData(IValidateRoot.VALIDATE_ROOT_DATA, new DefaultValidateRoot(m_swtBodyPart) {
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
    createButtonbar(rootPane);

    IUiDecoration deco = UiDecorationExtensionPoint.getLookAndFeel();
    LogicalGridLayout bodyLayout = new LogicalGridLayout(deco.getLogicalGridLayoutHorizontalGap(), deco.getLogicalGridLayoutVerticalGap());
    m_swtBodyPart.setLayout(bodyLayout);
    installSwtContainerBorder();
    // FIELDS:
    IFormField[] scoutFields = getScoutObject().getControlFields();
    for (IFormField field : scoutFields) {
      ISwtScoutFormField swtScoutComposite = getEnvironment().createFormField(m_swtBodyPart, field);
      SwtScoutFormFieldGridData layoutData = new SwtScoutFormFieldGridData(field);
      swtScoutComposite.getSwtContainer().setLayoutData(layoutData);
    }
  }

  protected Composite createButtonbar(Composite parent) {
    m_swtButtonbar = new SwtScoutGroupBoxButtonbar();
    m_swtButtonbar.createField(parent, getScoutObject(), getEnvironment());
    GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    data.horizontalIndent = 0;
    data.verticalIndent = 0;
    m_swtButtonbar.getSwtContainer().setLayoutData(data);
    m_swtButtonbar.updateButtonbarVisibility();
    return m_swtButtonbar.getSwtContainer();
  }

  protected String resolveBorderDecoration(IGroupBox box) {
    if (IGroupBox.BORDER_DECORATION_SECTION.equals(box.getBorderDecoration())) {
      return IGroupBox.BORDER_DECORATION_SECTION;
    }
    else if (IGroupBox.BORDER_DECORATION_LINE.equals(box.getBorderDecoration())) {
      return IGroupBox.BORDER_DECORATION_LINE;
    }
    else if (IGroupBox.BORDER_DECORATION_EMPTY.equals(box.getBorderDecoration())) {
      return IGroupBox.BORDER_DECORATION_EMPTY;
    }
    else if (IGroupBox.BORDER_DECORATION_AUTO.equals(box.getBorderDecoration())) {
      // best guess
      if (box.isExpandable()) {
        return IGroupBox.BORDER_DECORATION_SECTION;
      }
      else if (box.isMainBox()) {
        return IGroupBox.BORDER_DECORATION_EMPTY;
      }
      else if (box.getParentField() instanceof ITabBox) {
        return IGroupBox.BORDER_DECORATION_EMPTY;
      }
      else {
        return IGroupBox.BORDER_DECORATION_LINE;
      }
    }
    else {
      return IGroupBox.BORDER_DECORATION_EMPTY;
    }
  }

  protected Composite createContainer(Composite parent) {
    Composite rootPane = null;
    GridLayout layout = new GridLayout(1, true);
    setSwtLabel(null);
    //
    if (m_containerBorderEnabled) {
      if (IGroupBox.BORDER_DECORATION_SECTION.equals(m_containerBorderDecorationResolved)) {
        // section
        int style = (getScoutObject().isExpanded() ? Section.EXPANDED : 0) | Section.TITLE_BAR;
        if (getScoutObject().isExpandable()) {
          style |= Section.TWISTIE;
        }
        m_swtSection = getEnvironment().getFormToolkit().createSection(parent, style);
        String label = getScoutObject().getLabel();
        if (label == null) {
          label = "";
        }
        m_swtSection.setText(label);
        m_swtSection.addExpansionListener(new P_ExpansionListener());

        rootPane = getEnvironment().getFormToolkit().createComposite(m_swtSection);
        m_swtSection.setClient(rootPane);
        setSwtContainer(m_swtSection);
        //
        layout.horizontalSpacing = 4;
        layout.verticalSpacing = 4;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.marginTop = 6;
        layout.marginLeft = 6;
        layout.marginBottom = 6;
        layout.marginRight = 6;
        rootPane.setLayout(layout);
      }
      else if (IGroupBox.BORDER_DECORATION_LINE.equals(m_containerBorderDecorationResolved)) {
        m_swtGroup = getEnvironment().getFormToolkit().createGroup(parent, SWT.SHADOW_ETCHED_IN);
        rootPane = m_swtGroup;
        setSwtContainer(m_swtGroup);
        //
        layout.horizontalSpacing = 4;
        layout.verticalSpacing = 4;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.marginTop = 6;
        layout.marginLeft = 6;
        layout.marginBottom = 6;
        layout.marginRight = 6;
        rootPane.setLayout(layout);
      }
      else {
        // none
        rootPane = getEnvironment().getFormToolkit().createComposite(parent);
        setSwtContainer(rootPane);
        //
        layout.horizontalSpacing = 4;
        layout.verticalSpacing = 4;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.marginTop = 12;
        layout.marginLeft = 12;
        layout.marginBottom = 12;
        layout.marginRight = 12;
        rootPane.setLayout(layout);
      }
    }
    else {
      rootPane = getEnvironment().getFormToolkit().createComposite(parent);
      setSwtContainer(rootPane);
      //
      layout.horizontalSpacing = 0;
      layout.verticalSpacing = 0;
      layout.marginWidth = 0;
      layout.marginHeight = 0;
      layout.marginTop = 0;
      layout.marginLeft = 0;
      layout.marginBottom = 0;
      layout.marginRight = 0;
      rootPane.setLayout(layout);
    }
    return rootPane;
  }

  @Override
  public StatusLabelEx getSwtLabel() {
    return null;
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    if (m_swtButtonbar != null) {
      m_swtButtonbar.attachScout();
    }
    updateBackgroundImageFromScout();
    updateBackgroundImageHorizontalAlignFromScout();
    updateBackgroundImageVerticalAlignFromScout();
    setExpandedFromScout();
  }

  @Override
  protected void detachScout() {
    super.detachScout();
    if (m_swtButtonbar != null) {
      m_swtButtonbar.detachScout();
    }
  }

  protected void setExpandedFromScout() {
    if (getScoutObject().isExpandable()) {
      if (m_swtSection != null) {
        //only if necessary
        if (m_swtSection.isExpanded() != getScoutObject().isExpanded()) {
          m_swtSection.setExpanded(getScoutObject().isExpanded());
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
    if (m_swtSection != null) {
      m_swtSection.setText(s);
      m_swtSection.layout(true, true);
    }
    if (m_swtGroup != null) {
      m_swtGroup.setText(s);
    }
  }

  protected void updateBackgroundImageFromScout() {
    String imageName = getScoutObject().getBackgroundImageName();
    if (imageName == m_containerImage || imageName != null && imageName.equals(m_containerImage)) {
      // nop
    }
    else {
      m_containerImage = imageName;
      installSwtContainerBorder();
    }
  }

  protected void updateBackgroundImageHorizontalAlignFromScout() {
    // TODO
  }

  protected void updateBackgroundImageVerticalAlignFromScout() {
    // TODO
  }

  private void installSwtContainerBorder() {
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

  protected void handleSwtGroupBoxExpanded(final boolean expanded) {
    //notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().setExpandedFromUI(expanded);
      }
    };
    getEnvironment().invokeScoutLater(t, 0);
    //end notify
  }

  private class P_ExpansionListener extends ExpansionAdapter {
    @Override
    public void expansionStateChanged(ExpansionEvent e) {
      handleSwtGroupBoxExpanded(e.getState());
    }
  } // end class P_ExpansionListener

}
