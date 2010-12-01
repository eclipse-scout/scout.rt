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
    public Class[] getClassContext() {
      return super.getClassContext();
    }
  }

  private static ClassContextAccessor ccAccessor;

  static {
    AccessController.doPrivileged(new PrivilegedAction<Object>() {
      public Object run() {
        ccAccessor = new ClassContextAccessor();
        return null;
      }
    });
  }

  public ContextFinderBasedObjectInputStream(InputStream in) throws IOException {
    super(in);
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
    Class[] cc = ccAccessor.getClassContext();
    ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
    ClassLoader lastLoader = null;
    for (int i = 1; i < cc.length; i++) {
      ClassLoader cl = cc[i].getClassLoader();
      if (cl == null) {
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
        return cl.loadClass(className);
      }
      catch (Throwable t) {
        //nop
      }
    }
    throw new ClassNotFoundException(className);
  }

}
