package org.eclipse.scout.rt.platform;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.cdi.Bean;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.inventory.IClassInventory;

/**
 * extract {@link Bean} annotated classes
 */
public class BeanFilter {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BeanFilter.class);

  /**
   * @return all {@link Bean} annotated classes
   *         <p>
   *         Includes all classes that implement an interface that has a {@link Bean} annotation
   */
  public Set<Class> collect(IClassInventory classInventory) {
    Set<Class> allBeans = new HashSet<>();

    // 1. collect all annotations annotated with @Bean and register all classes that are directly annotated with @Bean
    Set<IClassInfo> beanAnnotations = new HashSet<>();
    for (IClassInfo ci : classInventory.getKnownAnnotatedTypes(Bean.class)) {
      if (ci.isAnnotation()) {
        beanAnnotations.add(ci);
      }
      else {
        collectWithSubClasses(classInventory, ci, allBeans);
      }
    }

    // 2. register all classes that are indirectly annotated with @Bean
    for (IClassInfo annotation : beanAnnotations) {
      try {
        for (IClassInfo ci : classInventory.getKnownAnnotatedTypes(annotation.resolveClass())) {
          collectWithSubClasses(classInventory, ci, allBeans);
        }
      }
      catch (ClassNotFoundException e) {
        LOG.warn("Error loading class '" + annotation.name() + "' with flags 0x" + Integer.toHexString(annotation.flags()), e);
      }
    }

    return allBeans;
  }

  /**
   * @param ci
   */
  private void collectWithSubClasses(IClassInventory classInventory, IClassInfo ci, Set<Class> collector) {
    if (ci.isEnum() || ci.isAnnotation() || ci.isSynthetic() || !ci.isPublic()) {
      LOG.debug("Skipping bean candidate '{0}' because it is no supported class type (enum, annotation, anonymous class) or is not public.", ci.name());
      return;
    }

    collect(ci, collector);

    if (!ci.isFinal()) {
      try {
        Set<IClassInfo> allKnownSubClasses = classInventory.getAllKnownSubClasses(ci.resolveClass());
        for (IClassInfo subClass : allKnownSubClasses) {
          collect(subClass, collector);
        }
      }
      catch (ClassNotFoundException e) {
        LOG.warn("Error loading class '" + ci.name() + "' with flags 0x" + Integer.toHexString(ci.flags()), e);
      }
    }
  }

  private void collect(IClassInfo ci, Set<Class> collector) {
    if (!ci.isInstanciable()) {
      LOG.debug("Skipping bean candidate '{0}' because it is not instanciable.", ci.name());
      return;
    }
    try {
      collector.add(ci.resolveClass());
    }
    catch (ClassNotFoundException ex) {
      LOG.warn("Error loading class '" + ci.name() + "' with flags 0x" + Integer.toHexString(ci.flags()), ex);
    }
  }
}
