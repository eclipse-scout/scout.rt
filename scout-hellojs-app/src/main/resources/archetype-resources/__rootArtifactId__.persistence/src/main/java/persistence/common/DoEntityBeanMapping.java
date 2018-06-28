#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.persistence.common;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.util.Assertions;

public class DoEntityBeanMapping<DO_ENTITY extends DoEntity, ENTITY_BEAN, VALUE> {

  private final Function<DO_ENTITY, DoValue<VALUE>> m_doRef;
  private final Function<ENTITY_BEAN, VALUE> m_entityBeanGetter;
  private final BiConsumer<ENTITY_BEAN, VALUE> m_entityBeanSetter;

  public DoEntityBeanMapping(Function<DO_ENTITY, DoValue<VALUE>> doRef, Function<ENTITY_BEAN, VALUE> entityBeanGetter, BiConsumer<ENTITY_BEAN, VALUE> entityBeanSetter) {
    m_doRef = Assertions.assertNotNull(doRef, "doRef must be set");
    m_entityBeanGetter = entityBeanGetter;
    m_entityBeanSetter = entityBeanSetter;
  }

  protected Function<DO_ENTITY, DoValue<VALUE>> getDoRef() {
    return m_doRef;
  }

  protected Function<ENTITY_BEAN, VALUE> getEntityBeanGetter() {
    return m_entityBeanGetter;
  }

  protected BiConsumer<ENTITY_BEAN, VALUE> getEntityBeanSetter() {
    return m_entityBeanSetter;
  }

  public void fromEntityBeanToDo(ENTITY_BEAN entityBean, DO_ENTITY doNode) {
    if (getEntityBeanGetter() == null) {
      return;
    }

    VALUE value = getEntityBeanGetter().apply(entityBean);
    DoValue<VALUE> doRef = getDoRef().apply(doNode);
    doRef.set(value);
  }

  public void fromDoToEntityBean(DO_ENTITY doNode, ENTITY_BEAN entityBean) {
    if (getEntityBeanSetter() == null) {
      return;
    }

    DoValue<VALUE> doRef = getDoRef().apply(doNode);
    if (doRef.exists()) {
      getEntityBeanSetter().accept(entityBean, doRef.get());
    }
  }
}
