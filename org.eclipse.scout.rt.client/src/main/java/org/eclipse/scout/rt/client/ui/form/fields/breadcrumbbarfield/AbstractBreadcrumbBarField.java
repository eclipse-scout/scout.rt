/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.client.ui.form.fields.breadcrumbbarfield;

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.breadcrumbbar.AbstractBreadcrumbBar;
import org.eclipse.scout.rt.client.ui.basic.breadcrumbbar.IBreadcrumbBar;
import org.eclipse.scout.rt.client.ui.basic.breadcrumbbar.IBreadcrumbItem;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

public abstract class AbstractBreadcrumbBarField extends AbstractFormField implements IBreadcrumbBarField {

  @ConfigOperation
  protected void execBreadcrumbItemAction(IBreadcrumbItem breadcrumbItem) {

  }

  @Override
  protected void initConfig() {
    super.initConfig();

    List<IBreadcrumbBar> contributedBreadcrumbs = m_contributionHolder.getContributionsByClass(IBreadcrumbBar.class);
    IBreadcrumbBar breadcrumbBar = CollectionUtility.firstElement(contributedBreadcrumbs);
    if (breadcrumbBar == null) {
      Class<? extends IBreadcrumbBar> configuredBar = getConfiguredBreadcrumbBar();
      if (configuredBar != null) {
        breadcrumbBar = ConfigurationUtility.newInnerInstance(this, configuredBar);
      }
    }
    setBreadcrumbBar(breadcrumbBar);
  }

  public void setBreadcrumbItems(List<IBreadcrumbItem> breadcrumbItems) {
    getBreadcrumbBar().setBreadcrumbItems(breadcrumbItems);
  }

  public List<IBreadcrumbItem> getBreadcrumbItems() {
    return getBreadcrumbBar().getBreadcrumbItems();
  }

  @Override
  public IBreadcrumbBar getBreadcrumbBar() {
    return (IBreadcrumbBar) propertySupport.getProperty(PROP_BREADCRUMB_BAR);
  }

  @Override
  public void setBreadcrumbBar(IBreadcrumbBar bar) {
    propertySupport.setPropertyAlwaysFire(PROP_BREADCRUMB_BAR, bar);
  }

  private Class<? extends IBreadcrumbBar> getConfiguredBreadcrumbBar() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IBreadcrumbBar>> f = ConfigurationUtility.filterClasses(dca, IBreadcrumbBar.class);
    if (f.size() == 1) {
      return CollectionUtility.firstElement(f);
    }
    else {
      for (Class<? extends IBreadcrumbBar> c : f) {
        if (c.getDeclaringClass() != AbstractBreadcrumbBarField.class) {
          return c;
        }
      }
      return null;
    }
  }

  public class DefaultBreadcrumbBar extends AbstractBreadcrumbBar {
    @Override
    protected void execBreadcrumbItemAction(IBreadcrumbItem item) {
      AbstractBreadcrumbBarField.this.execBreadcrumbItemAction(item);
    }
  }
}
