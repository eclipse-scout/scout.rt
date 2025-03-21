#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.data.person;

import jakarta.annotation.Generated;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("${simpleArtifactName}.Person")
public class PersonDo extends DoEntity {

  public DoValue<String> firstName() {
    return doValue("firstName");
  }

  public DoValue<String> lastName() {
    return doValue("lastName");
  }

  public DoValue<String> id() {
    return doValue("id");
  }

  public DoValue<Integer> salary() {
    return doValue("salary");
  }

  public DoValue<Boolean> external() {
    return doValue("external");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public PersonDo withFirstName(String firstName) {
    firstName().set(firstName);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getFirstName() {
    return firstName().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public PersonDo withLastName(String lastName) {
    lastName().set(lastName);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getLastName() {
    return lastName().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public PersonDo withId(String id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getId() {
    return id().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public PersonDo withSalary(Integer salary) {
    salary().set(salary);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getSalary() {
    return salary().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public PersonDo withExternal(Boolean external) {
    external().set(external);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getExternal() {
    return external().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isExternal() {
    return nvl(getExternal());
  }
}
