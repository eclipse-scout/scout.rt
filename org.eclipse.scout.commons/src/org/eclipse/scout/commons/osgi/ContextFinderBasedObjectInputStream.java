package org.eclipse.scout.commons.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Resolve an object using first the context class loader (ContextFinder) and then a custom resolver.
 * <p>
 * The ContextFinder has the "flaw" that it uses the caller class loader(s) but it accepts the first class loader of
 * type BundleClassLoader. Since this guess is correct in some / many cases it is wrong for other cases when a
 * specialized bundle uses code from a common bundle and the common bundle loads classes using the ContextFinder. Then
 * the other BundleClassLoader should be considered as well.
 */
public class ContextFinderBasedObjectInputStream extends ObjectInputStream {
  private static final class ClassContextAccessor extends SecurityManager {
    @Override
    public Class[] getClassContext() {
      return super.getClassContext();
    }
  }

  private static ClassContextAccessor ccAccessor;

  static {
    AccessController.doPrivileged(new PrivilegedAction<Object>() {
      @Override
      public Object run() {
        ccAccessor = new ClassContextAccessor();
        return null;
      }
    });
  }

  private ClassLoader m_primaryClassLoader;

  public ContextFinderBasedObjectInputStream(InputStream in) throws IOException {
    this(in, null);
  }

  public ContextFinderBasedObjectInputStream(InputStream in, ClassLoader primaryClassLoader) throws IOException {
    super(in);
    m_primaryClassLoader = primaryClassLoader;
    enableResolveObject(true);
  }

  @Override
  protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
    try {
      //pass 1
      return defaultResolveClass(desc.getName());
    }
    catch (ClassNotFoundException e) {
      //pass 2
      return pass2ResolveClass(desc.getName());
    }
  }

  private Class<?> defaultResolveClass(String className) throws ClassNotFoundException, IOException {
    return Class.forName(className);
  }

  private Class<?> pass2ResolveClass(String className) throws ClassNotFoundException, IOException {
    if (m_primaryClassLoader != null) {
      try {
        return Class.forName(className, false, m_primaryClassLoader);
      }
      catch (Throwable t) {
        //nop
      }
    }
    //
    Class[] cc = ccAccessor.getClassContext();
    ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
    ClassLoader lastLoader = null;
    for (int i = 1; i < cc.length; i++) {
      ClassLoader cl = cc[i].getClassLoader();
      if (cl == null) {
        continue;
      }
      if (cl == m_primaryClassLoader) {
        continue;
      }
      if (cl == contextLoader) {
        continue;
      }
      if (cl == lastLoader) {
        continue;
      }
      lastLoader = cl;
      try {
        return Class.forName(className, false, cl);
      }
      catch (Throwable t) {
        //nop
      }
    }
    throw new ClassNotFoundException(className);
  }

}
