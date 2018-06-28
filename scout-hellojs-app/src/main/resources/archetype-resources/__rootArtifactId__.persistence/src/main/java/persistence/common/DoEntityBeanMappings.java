#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.persistence.common;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoValue;

public class DoEntityBeanMappings<DO_ENTITY extends DoEntity, ENTITY_BEAN> {

  private List<DoEntityBeanMapping<DO_ENTITY, ENTITY_BEAN, ?>> m_mappings = new LinkedList<>();

  public List<DoEntityBeanMapping<DO_ENTITY, ENTITY_BEAN, ?>> getMappings() {
    return m_mappings;
  }

  /**
   * Read-only, no setter for entity bean.
   */
  public <VALUE> DoEntityBeanMappings<DO_ENTITY, ENTITY_BEAN> with(Function<DO_ENTITY, DoValue<VALUE>> doRef, Function<ENTITY_BEAN, VALUE> entityBeanGetter) {
    return with(doRef, entityBeanGetter, null);
  }

  /**
   * There is no duplicate detection meaning that adding a DO node multiple times will execute the corresponding action
   * multiple times.
   */
  public <VALUE> DoEntityBeanMappings<DO_ENTITY, ENTITY_BEAN> with(Function<DO_ENTITY, DoValue<VALUE>> doRef, Function<ENTITY_BEAN, VALUE> entityBeanGetter, BiConsumer<ENTITY_BEAN, VALUE> entityBeanSetter) {
    getMappings().add(new DoEntityBeanMapping<>(doRef, entityBeanGetter, entityBeanSetter));
    return this;
  }

  public void fromEntityBeanToDo(ENTITY_BEAN entityBean, DO_ENTITY doNode) {
    getMappings().stream().forEach(n -> n.fromEntityBeanToDo(entityBean, doNode));
  }

  public void fromDoToRecord(DO_ENTITY doNode, ENTITY_BEAN entityBean) {
    getMappings().stream().forEach(n -> n.fromDoToEntityBean(doNode, entityBean));
  }
}
