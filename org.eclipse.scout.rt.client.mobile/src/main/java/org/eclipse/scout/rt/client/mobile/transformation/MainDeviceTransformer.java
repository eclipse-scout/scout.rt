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

  public List<IDeviceTransformer> getTransformers() {
    if (m_transformers == null) {
      m_transformers = createTransformers();
      LOG.info("Using following device transformers{}", m_transformers);
    }
    return m_transformers;
  }

  protected List<IDeviceTransformer> createTransformers() {
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
  public void dispose() {
    if (!isActive()) {
      return;
    }
    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.dispose();
    }
    if (!m_transformedForms.isEmpty()) {
      LOG.warn("Transformed forms map is not empty. Make sure every form gets closed properly to free up memory as quickly as possible. Cleaning up now...");
      m_transformedForms.clear();
    }
    m_transformedOutlines.clear();
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
    if (!isActive() || isFormExcluded(form)) {
      return;
    }

    WeakReference<IForm> formRef = m_transformedForms.get(form);
    if (formRef != null) {
      // already transformed
      // form may be reinitialized any time (e.g. using doReset()) -> don't transform again
      return;
    }

    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.transformForm(form);
    }

    if (isGridDataDirty(form)) {
      FormUtility.rebuildFieldGrid(form, true);
      gridDataRebuilt(form);
      if (isGridDataDirty(form)) {
        throw new IllegalStateException("Potential memory leak: gridData still marked as dirty for form " + form);
      }
    }

    //mark form as transformed
    m_transformedForms.put(form, new WeakReference<IForm>(form));
  }

  @Override
  public void notifyFormDisposed(IForm form) {
    m_transformedForms.remove(form);
  }

  @Override
  public boolean isFormExcluded(IForm form) {
    if (!isActive()) {
      return false;
    }

    for (IDeviceTransformer transformer : getTransformers()) {
      if (transformer.isFormExcluded(form)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isFormFieldExcluded(IFormField formField) {
    if (!isActive()) {
      return false;
    }

    for (IDeviceTransformer transformer : getTransformers()) {
      if (transformer.isFormFieldExcluded(formField)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void transformFormField(IFormField field) {
    if (!isActive() || isFormExcluded(field.getForm()) || isFormFieldExcluded(field)) {
      return;
    }

    WeakReference<IForm> formRef = m_transformedForms.get(field.getForm());
    if (formRef != null) {
      // Already transformed
      // fields can only be added during form initialization -> no need to transform again if form has already been initialized
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
  public void transformPageDetailForm(IForm form) {
    if (!isActive()) {
      return;
    }

    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.transformPageDetailForm(form);
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
  public void notifyPageSearchFormInit(IPageWithTable<ITable> page) {
    if (!isActive()) {
      return;
    }

    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.notifyPageSearchFormInit(page);
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
