#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.data.person;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toCollection;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("PersonResponse")
public class PersonResponse extends DoEntity {

  public DoList<PersonDo> items() {
    return doList("items");
  }

  public PersonResponse withItem(PersonDo persons) {
    items().get().add(persons);
    return this;
  }

  public PersonResponse withItems(Stream<PersonDo> persons) {
    items().clear();
    persons.collect(toCollection(items()::get));
    return this;
  }

  public PersonResponse withItems(Collection<? extends PersonDo> persons) {
    items().clear();
    items().get().addAll(persons);
    return this;
  }

  public PersonResponse withItems(PersonDo... persons) {
    items().clear();
    return withItems(asList(persons));
  }

  public List<PersonDo> getItems() {
    return items().get();
  }

}
