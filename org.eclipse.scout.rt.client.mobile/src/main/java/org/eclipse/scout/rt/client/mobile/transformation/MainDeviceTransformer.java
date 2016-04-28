package org.eclipse.scout.rt.client.mobile.transformation;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.FormUtility;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainDeviceTransformer implements IDeviceTransformer {
  private static final Logger LOG = LoggerFactory.getLogger(MainDeviceTransformer.class);

  private List<IDeviceTransformer> m_transformers;
  private final Map<IForm, WeakReference<IForm>> m_transformedForms = new WeakHashMap<>();
  private final Map<IOutline, WeakReference<IOutline>> m_transformedOutlines = new WeakHashMap<>();

  private List<IDeviceTransformer> getTransformers() {
    if (m_transformers == null) {
      m_transformers = createTransformers();
      LOG.info("Using following device transformers{}", m_transformers);
    }
    return m_transformers;
  }

  private List<IDeviceTransformer> createTransformers() {
    return BEANS.all(IDeviceTransformer.class, new IFilter<IDeviceTransformer>() {
      @Override
      public boolean accept(IDeviceTransformer transformer) {
        return !(transformer instanceof MainDeviceTransformer) && transformer.isActive();
      }
    });
  }

  @Override
  public boolean isActive() {
    return !getTransformers().isEmpty();
  }

  @Override
  public void setDesktop(IDesktop desktop) {
    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.setDesktop(desktop);
    }
  }

  @Override
  public void transformDesktop() {
    if (!isActive()) {
      return;
    }

    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.transformDesktop();
    }
  }

  @Override
  public void transformForm(IForm form) {
    if (!isActive()) {
      return;
    }

    WeakReference<IForm> formRef = m_transformedForms.get(form);
    if (formRef != null) {
      // already transformed
      return;
    }

    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.transformForm(form);
    }

    if (isGridDataDirty(form)) {
      //FIXME CGU verify functionality and cleanup
      FormUtility.rebuildFieldGrid(form, true);
      gridDataRebuilt(form);
    }

    //mark form as transformed
    m_transformedForms.put(form, new WeakReference<IForm>(form));
  }

  @Override
  public boolean isFormFieldExcluded(IFormField formField) {
    if (!isActive()) {
      return false;
    }

    //FIXME CGU verify this
    for (IDeviceTransformer transformer : getTransformers()) {
      if (transformer.isFormFieldExcluded(formField)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void transformFormField(IFormField field) {
    if (!isActive()) {
      return;
    }

    WeakReference<IForm> formRef = m_transformedForms.get(field.getForm());
    if (formRef != null) {
      //FIXME CGU verify this (search form)
      // fields can only be added during form initialization -> no need to transform again if form has already been initialized
      return;
    }

    if (isFormFieldExcluded(field)) {
      return;
    }

    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.transformFormField(field);
    }
  }

  @Override
  public void transformOutline(IOutline outline) {
    if (!isActive()) {
      return;
    }

    WeakReference<IOutline> outlineRef = m_transformedOutlines.get(outline);
    if (outlineRef != null) {
      // Already transformed
      return;
    }

    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.transformOutline(outline);
    }

    // mark as transformed
    m_transformedOutlines.put(outline, new WeakReference<IOutline>(outline));
  }

  @Override
  public void transformPage(IPage<?> page) {
    if (!isActive()) {
      return;
    }

    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.transformPage(page);
    }
  }

  @Override
  public void transformPageDetailTable(ITable table) {
    if (!isActive()) {
      return;
    }

    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.transformPageDetailTable(table);
    }
  }

  @Override
  public void adaptDesktopActions(Collection<IAction> actions) {
    if (!isActive()) {
      return;
    }

    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.adaptDesktopActions(actions);
    }
  }

  @Override
  public void notifyDesktopClosing() {
    if (!isActive()) {
      return;
    }

    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.notifyDesktopClosing();
    }
  }

  @Override
  public void notifyTablePageLoaded(IPageWithTable<?> tablePage) {
    if (!isActive()) {
      return;
    }

    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.notifyTablePageLoaded(tablePage);
    }
  }

  @Override
  public boolean isGridDataDirty(IForm form) {
    if (!isActive()) {
      return false;
    }

    for (IDeviceTransformer transformer : getTransformers()) {
      if (transformer.isGridDataDirty(form)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void gridDataRebuilt(IForm form) {
    if (!isActive()) {
      return;
    }

    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.gridDataRebuilt(form);
    }
  }

  @Override
  public boolean acceptFormAddingToDesktop(IForm form) {
    if (!isActive()) {
      return true;
    }

    for (IDeviceTransformer transformer : getTransformers()) {
      if (!transformer.acceptFormAddingToDesktop(form)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public DeviceTransformationConfig getDeviceTransformationConfig() {
    return null;
  }

}
