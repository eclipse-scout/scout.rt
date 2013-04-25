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
package org.eclipse.scout.rt.client.mobile.transformation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.mobile.ui.desktop.MultiPageChangeStrategy;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.IOutlineChooserForm;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.IPageForm;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.PageFormManager;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IPageChangeStrategy;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * @since 3.9.0
 */
public class TabletDeviceTransformer extends MobileDeviceTransformer {
  private static int EAST_FORM_WIDTH = 700;

  public TabletDeviceTransformer() {
    this(null);
  }

  public TabletDeviceTransformer(IDesktop desktop) {
    super(desktop);
  }

  @Override
  protected void initTransformationConfig() {
    super.initTransformationConfig();

    getDeviceTransformationConfig().disableTransformation(MobileDeviceTransformation.MOVE_FIELD_LABEL_TO_TOP);
    getDeviceTransformationConfig().disableTransformation(MobileDeviceTransformation.ADD_MISSING_BACK_ACTION_TO_FORM_HEADER);
  }

  @Override
  protected PageFormManager createPageFormManager(IDesktop desktop) {
    return new PageFormManager(desktop, IForm.VIEW_ID_CENTER, IForm.VIEW_ID_E);
  }

  @Override
  public void adaptDesktopOutlines(Collection<IOutline> outlines) {
    IPageChangeStrategy strategy = new MultiPageChangeStrategy(getPageFormManager());
    for (IOutline outline : outlines) {
      outline.setPageChangeStrategy(strategy);
    }
  }

  @Override
  protected ToolFormHandler createToolFormHandler(IDesktop desktop) {
    ToolFormHandler toolFormHandler = new ToolFormHandler(getDesktop());
    toolFormHandler.setCloseToolFormsAfterTablePageLoaded(false);
    return toolFormHandler;
  }

  /**
   * On tablet devices there are at maximum two view stacks, on mobile only one. So it is not necessary to create the
   * other ones which saves unnecessary composites and therefore loading time.
   */
  @Override
  public List<String> getAcceptedViewIds() {
    List<String> viewIds = new LinkedList<String>();
    viewIds.add(IForm.VIEW_ID_CENTER);
    viewIds.add(IForm.VIEW_ID_E);

    return viewIds;
  }

  @Override
  protected void transformView(IForm form) {
    if (!(form instanceof IPageForm || form instanceof IOutlineChooserForm)) {
      form.setDisplayViewId(IForm.VIEW_ID_E);
    }
    if (IForm.VIEW_ID_E.equals(form.getDisplayViewId())) {
      boolean valueSet = MobileDesktopUtility.setFormWidthHint(form, EAST_FORM_WIDTH);
      if (valueSet) {
        markGridDataDirty();
      }
    }
  }

}
