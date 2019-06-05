#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.data.person;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("${simpleArtifactName}.Person")
public class PersonDo extends DoEntity {
  public DoValue<String> firstName() {
    return doValue("firstName");
  }

  public PersonDo withFirstName(String firstName) {
    firstName().set(firstName);
    return this;
  }

  public String getFirstName() {
    return firstName().get();
  }

  public DoValue<String> lastName() {
    return doValue("lastName");
  }

  public PersonDo withLastName(String lastName) {
    lastName().set(lastName);
    return this;
  }

  public String getLastName() {
    return lastName().get();
  }

  public DoValue<String> personId() {
    return doValue("personId");
  }

  public PersonDo withPersonId(String personId) {
    personId().set(personId);
    return this;
  }

  public String getPersonId() {
    return personId().get();
  }

  public DoValue<Integer> salary() {
    return doValue("salary");
  }

  public PersonDo withSalary(Integer salary) {
    salary().set(salary);
    return this;
  }

  public Integer getSalary() {
    return salary().get();
  }

  public DoValue<Boolean> external() {
    return doValue("external");
  }

  public PersonDo withExternal(boolean external) {
    external().set(external);
    return this;
  }

  public Boolean getExternal() {
    return external().get();
  }
}
