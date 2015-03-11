package org.eclipse.scout.rt.platform.cdi.internal.scan;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.cdi.Bean;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;

public class BeanFinderWithJandex extends AbstractBeanFinder {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BeanFinderWithJandex.class);

  private final HashSet<Class> m_classes = new HashSet<Class>();
  private final Indexer indexer = new Indexer();

  public BeanFinderWithJandex() {
    super();
  }

  @Override
  protected void handleClass(String classname, URL url) {
    try (InputStream in = url.openStream()) {
      indexer.index(in);
    }
    catch (IOException ex) {
      LOG.error("class " + classname + " at " + url, ex);
    }
  }

  /*
   * ClassInfo#flags
   *   0x01 public or protected
   *   0x10 final
   *   0x20 class
   *  0x200 interface
   *  0x400 abstract
   * 0x1000 anonymous inner
   * 0x2000 annotation
   * 0x4000 enum
   */
  @Override
  public Collection<Class> finish() {
    Index index = indexer.complete();
    //find all annotations that themselves have a @Bean annotation
    HashSet<DotName> beanAnnotations = new HashSet<DotName>();
    beanAnnotations.add(DotName.createSimple(Bean.class.getName()));
    for (AnnotationInstance ai : index.getAnnotations(DotName.createSimple(Bean.class.getName()))) {
      if (ai.target() instanceof ClassInfo) {
        ClassInfo ci = (ClassInfo) ai.target();
        if ((ci.flags() & 0x2000) != 0) {
          beanAnnotations.add(ci.name());
        }
      }
    }
    //now find all classes with any of these annotation
    for (DotName beanAnnotation : beanAnnotations) {
      for (AnnotationInstance ai : index.getAnnotations(beanAnnotation)) {
        //class or interface
        if (!(ai.target() instanceof ClassInfo)) {
          continue;
        }
        ClassInfo ci = (ClassInfo) ai.target();
        //public or protected, no enum/anno/anon
        if ((ci.flags() & 0x7001) != 0x0001) {
          continue;
        }
        try {
          Class<?> clazz = Class.forName(ci.name().toString());
          //top level or static inner
          if (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())) {
            continue;
          }
          m_classes.add(clazz);
        }
        catch (ClassNotFoundException ex) {
          LOG.warn("class " + ci.name() + " with flags 0x" + Integer.toHexString(ci.flags()), ex);
        }
      }
    }
    return m_classes;
  }
}
