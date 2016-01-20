package org.eclipse.scout.rt.platform;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <h3>{@link InterfaceBeanTest}</h3>
 *
 * @author Matthias Villiger
 */
public class InterfaceBeanTest {

  private IBean<Object> m_baseBean;
  private IBean<Object> m_childBean;
  private IBean<Object> m_wrongChildBean;

  @Before
  public void registerTestServices() {
    m_baseBean = Platform.get().getBeanManager().registerBean(new BeanMetaData(IBaseTest.class).withApplicationScoped(false));
    m_childBean = Platform.get().getBeanManager().registerBean(new BeanMetaData(IChildTest.class).withApplicationScoped(false));
    m_wrongChildBean = Platform.get().getBeanManager().registerBean(new BeanMetaData(IWrongChildTest.class).withApplicationScoped(false));
  }

  /**
   * Tests that interface beans can be registered and retrieved
   */
  @Test
  public void testInterfaceBeans() {
    IBean<IBaseTest> baseBean = BEANS.getBeanManager().getBean(IBaseTest.class);
    Assert.assertNotNull(baseBean);
    Assert.assertEquals(IBaseTest.class, baseBean.getBeanClazz());

    IBean<IChildTest> childBean = BEANS.getBeanManager().getBean(IChildTest.class);
    Assert.assertNotNull(childBean);
    Assert.assertEquals(IChildTest.class, childBean.getBeanClazz());
  }

  /**
   * Test that @Replace on interfaces causes an error
   */
  @Test(expected = IllegalArgumentException.class)
  public void testInterfaceBeanWithReplace() {
    BEANS.getBeanManager().getBean(IWrongChildTest.class);
  }

  @After
  public void unRegisterTestServices() {
    Platform.get().getBeanManager().unregisterBean(m_baseBean);
    Platform.get().getBeanManager().unregisterBean(m_childBean);
    Platform.get().getBeanManager().unregisterBean(m_wrongChildBean);
  }

  private interface IBaseTest {

  }

  private interface IChildTest extends IBaseTest {

  }

  private interface IWrongBaseTest {

  }

  @Replace
  private interface IWrongChildTest extends IWrongBaseTest {

  }
}
