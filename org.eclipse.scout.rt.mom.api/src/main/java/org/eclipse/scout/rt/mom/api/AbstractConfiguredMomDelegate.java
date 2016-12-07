package org.eclipse.scout.rt.mom.api;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Message oriented middleware (MOM) transport that uses an implementation based on two configuration methods
 * ({@link #getConfiguredImplementor()} and {@link #getConfiguredEnvironment()}). To further customize the created
 * {@link IMomImplementor}, override {@link #initDelegate()}.
 *
 * @see IMom
 */
public abstract class AbstractConfiguredMomDelegate extends AbstractMomDelegate implements IMomTransport {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractConfiguredMomDelegate.class);

  /**
   * @return the {@link IMomImplementor} class to use in this MOM. If <code>null</code> is returned, the
   *         {@link NullMomImplementor} is used.
   */
  protected abstract Class<? extends IMomImplementor> getConfiguredImplementor();

  /**
   * @return the environment required to initialize the MOM implementor (not <code>null</code>).
   */
  protected abstract Map<String, String> getConfiguredEnvironment();

  /**
   * Returns <code>true</code> if no {@link IMomImplementor} is configured for this MOM or the configured implementor is
   * of type {@link NullMomImplementor}.
   * <p>
   * Unlike the other methods on this class, this method can be called <b>without</b> triggering the initialization of
   * the delegate.
   */
  public boolean isNullTransport() {
    final Class<? extends IMomImplementor> implementorClass = getConfiguredImplementor();
    return implementorClass == null || implementorClass == NullMomImplementor.class;
  }

  @Override
  protected IMomImplementor initDelegate() throws Exception {
    final Class<? extends IMomImplementor> implementorClass = ObjectUtility.nvl(getConfiguredImplementor(), NullMomImplementor.class);

    if (implementorClass == NullMomImplementor.class) {
      LOG.info("+++ Using '{}' for transport '{}'. No messages are published and received.", NullMomImplementor.class.getSimpleName(), getClass().getSimpleName());
      return BEANS.get(NullMomImplementor.class);
    }

    final IMomImplementor implementor = BEANS.get(implementorClass);
    implementor.init(lookupEnvironment());
    return implementor;
  }

  /**
   * @return the environment required to initialize the MOM implementor.
   */
  protected Map<Object, Object> lookupEnvironment() {
    final Map<String, String> env = Assertions.assertNotNull(getConfiguredEnvironment(), "Environment for {} not specified", getClass().getSimpleName());
    // Use the class name as default symbolic name
    if (!env.containsKey(IMomImplementor.SYMBOLIC_NAME)) {
      env.put(IMomImplementor.SYMBOLIC_NAME, getClass().getSimpleName());
    }
    return new HashMap<Object, Object>(env);
  }
}
