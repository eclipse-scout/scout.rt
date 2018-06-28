#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.data.person;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("Person")
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

  public DoValue<String> sex() {
    return doValue("sex");
  }

  public PersonDo withSex(String sex) {
    personId().set(sex);
    return this;
  }

  public String getSex() {
    return sex().get();
  }
}
