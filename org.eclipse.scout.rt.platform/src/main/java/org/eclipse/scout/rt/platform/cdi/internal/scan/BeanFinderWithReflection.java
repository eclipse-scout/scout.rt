package org.eclipse.scout.rt.platform.cdi.internal.scan;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.cdi.Bean;

public class BeanFinderWithReflection extends AbstractBeanFinder {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BeanFinderWithReflection.class);
  private final HashSet<Class> m_classes = new HashSet<Class>();

  public BeanFinderWithReflection() {
  }

  @Override
  protected void handleClass(String classname, URL url) {
    try {
      Class<?> clazz = Class.forName(classname);
      //public or protected
      int m = clazz.getModifiers();
      if ((m & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0) {
        return;
      }
      //no enum/anno/anon
      if (clazz.isEnum() || clazz.isAnnotation() || clazz.isAnonymousClass()) {
        return;
      }
      //top level or static inner
      if (clazz.isMemberClass() && !Modifier.isStatic(m)) {
        return;
      }
      //direct bean
      if (clazz.getAnnotation(Bean.class) != null) {
        m_classes.add(clazz);
        return;
      }
      //indirect bean
      for (Annotation a : clazz.getAnnotations()) {
        if (a.annotationType().getAnnotation(Bean.class) != null) {
          m_classes.add(clazz);
          return;
        }
      }
    }
    catch (ClassNotFoundException ex) {
      LOG.error("class " + classname + " at " + url, ex);
    }
  }

  @Override
  public Collection<Class> finish() {
    return m_classes;
  }

}
