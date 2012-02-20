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
package org.eclipse.scout.rt.ui.swt.ext;

import java.lang.reflect.Method;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.basic.comp.CLabelEx;
import org.eclipse.scout.rt.ui.swt.util.VersionUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * <p>
 * Contains a label to use on top of a field. The label actually is a {@link StyledText} in order to support line wrap.
 * </p>
 * <p>
 * Compared to {@link StatusLabelEx} which uses a {@link CLabelEx} the text won't be shortened but wrapped instead (if
 * style is set to SWT.WRAP). Additionally the place of the status icon is different. It is located left to the text and
 * not at the right side.
 * </p>
 */
public class StatusLabelTop extends StatusLabelEx {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(StatusLabelTop.class);

  public StatusLabelTop(Composite parent, int style, ISwtEnvironment environment) {
    super(parent, style, environment);
  }

  @Override
  protected void createLayout() {
    GridLayout containerLayout = new GridLayout(2, false);
    containerLayout.horizontalSpacing = 0;
    containerLayout.marginHeight = 0;
    containerLayout.marginWidth = 0;
    containerLayout.verticalSpacing = 0;

    //a little margin between the label and a field
    containerLayout.marginBottom = 2;

    //another margin on the left so it is vertically aligned with the LABEL_POSITION_LEFT-labels
    containerLayout.marginLeft = 1;

    setLayout(containerLayout);
  }

  @Override
  protected void createContent(Composite parent, int style) {
    setStatusLabel(new Label(parent, SWT.NONE));
    getEnvironment().getFormToolkit().getFormToolkit().adapt(getStatusLabel(), false, false);

    style |= SWT.WRAP | SWT.MULTI;
    StyledText label = getEnvironment().getFormToolkit().createStyledText(parent, style);
    label.setEnabled(false);
    setMarginsOnLabel(label);
    setLabel(label);

    //Set the status icon to the top left corner
    GridData data = new GridData(SWT.LEFT, SWT.TOP, false, false);
    getStatusLabel().setLayoutData(data);

    //Make sure the label composite fills the cell so that horizontal alignment of the text works well
    data = new GridData(SWT.FILL, SWT.FILL, true, true);
    label.setLayoutData(data);
  }

  @Override
  protected void setLabelText(String text) {
    if (getLabel() instanceof StyledText) {
      ((StyledText) getLabel()).setText(text);
    }
  }

  @Override
  protected String getLabelText() {
    if (getLabel() instanceof StyledText) {
      return ((StyledText) getLabel()).getText();
    }

    return null;
  }

  protected void setMarginsOnLabel(StyledText label) {
    if (VersionUtility.isEclipseVersionLessThan35()) {
      return;
    }

    try {
      //Make sure the wrap indent is the same as the indent so that the text is vertically aligned
      Method setWrapIndent = StyledText.class.getMethod("setWrapIndent", int.class);
      setWrapIndent.invoke(label, label.getIndent());
    }
    catch (Exception e) {
      LOG.warn("could not access method 'setWrapIndent' on 'StyledText'.", e);
    }
  }
}
