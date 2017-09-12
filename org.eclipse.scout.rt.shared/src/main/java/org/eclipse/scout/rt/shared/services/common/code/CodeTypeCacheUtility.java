package org.eclipse.scout.rt.shared.services.common.code;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;

/**
 * Utilities and factory methods for {@link CodeTypeCache}
 */
@ApplicationScoped
public class CodeTypeCacheUtility {

  /**
   * Creates a new code type cache key. Method hook allows to customize cache key instances.
   *
   * @param type
   * @return new cache key
   */
  public <T extends ICodeType<?, ?>> CodeTypeCacheKey createCacheKey(Class<T> type) {
    if (type == null) {
      return null;
    }
    return new CodeTypeCacheKey(resolveCodeTypeClass(type));
  }

  public <T extends ICodeType<?, ?>> CodeTypeCacheEntryFilter createEntryFilter(Class<T> type) {
    return new CodeTypeCacheEntryFilter(resolveCodeTypeClass(type));
  }

  public <T extends ICodeType<?, ?>> CodeTypeCacheEntryFilter createEntryFilter(List<Class<? extends ICodeType<?, ?>>> types) {
    return new CodeTypeCacheEntryFilter(resolveCodeTypeClasses(types));
  }

  protected <T extends ICodeType<?, ?>> Class<T> resolveCodeTypeClass(Class<T> type) {
    if (type == null) {
      return null;
    }
    final IBean<T> bean = BEANS.getBeanManager().optBean(type);
    if (bean == null || bean.getBeanClazz() == null) {
      return type;
    }
    @SuppressWarnings("unchecked")
    Class<T> activeCodeTypeClass = (Class<T>) bean.getBeanClazz();
    return activeCodeTypeClass;
  }

  protected List<Class<? extends ICodeType<?, ?>>> resolveCodeTypeClasses(List<Class<? extends ICodeType<?, ?>>> types) {
    List<Class<? extends ICodeType<?, ?>>> result = new ArrayList<>();
    if (types == null) {
      return result;
    }
    for (Class<? extends ICodeType<?, ?>> type : types) {
      result.add(resolveCodeTypeClass(type));
    }
    return result;
  }

}
