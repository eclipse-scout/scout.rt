package org.eclipse.scout.rt.platform.dataobject.value;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("DoEntityListValue")
public class DoEntityListValueDo extends DoEntity implements IValueDo<List<IDoEntity>> {

  public static DoEntityListValueDo of(IDoEntity value) {
    return BEANS.get(DoEntityListValueDo.class).withValue(value);
  }

  @Override
  public DoList<IDoEntity> value() {
    return doList(VALUE_ATTRIBUTE);
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public DoEntityListValueDo withValue(Collection<? extends IDoEntity> value) {
    value().clear();
    value().get().addAll(value);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DoEntityListValueDo withValue(IDoEntity... value) {
    return withValue(Arrays.asList(value));
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<IDoEntity> getValue() {
    return value().get();
  }
}
