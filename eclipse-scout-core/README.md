<p align="center">
  <a href="https://www.eclipse.org/scout/" target="_blank" rel="noopener noreferrer"><img src="https://eclipsescout.github.io/assets/img/scout_logo.gif"></a>
</p>

<p align="center">
  <a href="https://ci.eclipse.org/scout/view/Scout%20Nightly%20Jobs/job/scout-integration-10.0-RT-nightly/" target="_blank" rel="noopener noreferrer"><img alt="Jenkins" src="https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.eclipse.org%2Fscout%2Fview%2FScout%2520Nightly%2520Jobs%2Fjob%2Fscout-integration-10.0-RT-nightly%2F"></a>
  <a href="https://ci.eclipse.org/scout/view/Scout%20Nightly%20Jobs/job/scout-integration-10.0-RT-nightly/" target="_blank" rel="noopener noreferrer"><img alt="Jenkins tests" src="https://img.shields.io/jenkins/tests?compact_message&jobUrl=https%3A%2F%2Fci.eclipse.org%2Fscout%2Fview%2FScout%2520Nightly%2520Jobs%2Fjob%2Fscout-integration-10.0-RT-nightly%2F"></a>
  <a href="https://www.npmjs.com/package/@eclipse-scout/core" target="_blank" rel="noopener noreferrer"><img alt="npm" src="https://img.shields.io/npm/dm/@eclipse-scout/core"></a>
  <a href="https://www.eclipse.org/legal/epl-v10.html" target="_blank" rel="noopener noreferrer"><img alt="NPM" src="https://img.shields.io/npm/l/@eclipse-scout/core"></a>
  <a href="https://www.npmjs.com/package/@eclipse-scout/core" target="_blank" rel="noopener noreferrer"><img alt="npm (scoped)" src="https://img.shields.io/npm/v/@eclipse-scout/core"></a>
  <a href="https://www.npmjs.com/package/@eclipse-scout/core" target="_blank" rel="noopener noreferrer"><img alt="node" src="https://img.shields.io/node/v/@eclipse-scout/core"></a>
  <a href="https://www.eclipse.org/scout/" target="_blank" rel="noopener noreferrer"><img alt="Website" src="https://img.shields.io/website?url=https%3A%2F%2Fwww.eclipse.org%2Fscout%2F"></a>  
</p>


<p align="center"><h1>Eclipse Scout</h1></p>

## Introduction

Scout is an open source framework for implementing business applications. The framework is based on EcmaScript6+, HTML5, LESS and Java and covers most recurring aspects of enterprise applications.

Scout defines an abstract application model that makes developing applications faster and helps to decouple the business code as much as possible from any specific technologies. This is particularly useful as the life span of today’s web technologies is substantially shorter then the life span of large enterprise applications.

Scout comes with multi-device support. With a single code base Scout applications run on desktop, tablet and mobile devices. The framework automatically adapts the rendering of the application to the form factor of the used device.

![alt text][logo]

[logo]: https://eclipsescout.github.io/10.0/images/bsi_crm_indigo.png "Scout Application"

Scout supports a modularization of applications into layers and slices. This helps to define a clean architecture for large applications. The layering is driven by the fact the Scout applications have a rendering part, a frontend part and a backend part. The modularization into slices is controlled by different business needs such as front office, back office, reporting or the administration of application users, roles and permissions.

The goals of the Scout framework can be summarized as follows.
* Boost developer productivity
* Make the framework simple to learn
* Support building large applications with long life spans

Boosting developer productivity is of a very high importance and developers should be able to focus on the business value of the application. This is why Scout provides abstractions for areas/topics that are needed in most business applications again and again. Example areas/topics that are abstracted by the Scout framework are user interface (UI) technologies, databases, client-server communication or logging. For each of these abstractions Scout provides a default implementation out of the box. Typically, the default implementation of such an abstraction integrates a framework or technology that is commonly used.

Learning a new framework should be efficient and enjoyable. For developers that have a good understanding of the Java language learning the Scout framework will be straight forward. The required skill level roughly corresponds to the Oracle Certified Professional Java SE Programmer for Java version 11 or higher. As the Scout framework takes care of the transformation of the user interface from Java to HTML5, Scout developers only needs a minimal understanding of HTML5/CSS3/JavaScript. In the case of writing project specific UI components a deeper understanding of today’s web technologies might be required of course.

When needing a working prototype application by the end of the week, the developer just needs to care about the desired functionality. The necessary default implementations are then automatically included by the Scout tooling into the Scout project setup. The provided Scout SDK tooling also helps to get started quickly with Scout. It also allows to efficiently implement application components such as user interface components, server services or connections to databases.

In the case of applications with long life spans, the abstractions provided by Scout help the developer to concentrate on the actual business functionality. As all the implemented business functionality is written against abstractions only, no big rewrite of the application is necessary when individual technologies reach their end of life. In such cases it is sufficient to exchange the implementation of the adaptor for the legacy technology with a new one.

## Documentation

Check out the demo applications [JS Widgets](https://scout.bsi-software.com/jswidgets/), [Java Widgets](https://scout.bsi-software.com/widgets/) or [Contacts](https://scout.bsi-software.com/contacts/).

On our [github.io](https://eclipsescout.github.io/) site you find beginner guides, technical documentation, release notes and migration guides.

## Questions

For questions visit our [Forum](https://www.eclipse.org/forums/index.php?t=thread&frm_id=174) or use [Stack Overflow](https://stackoverflow.com/tags/eclipse-scout)

## Issues

For Issues checkout our [Bugzilla](https://bugs.eclipse.org/bugs/buglist.cgi?bug_status=UNCONFIRMED&bug_status=NEW&bug_status=ASSIGNED&bug_status=REOPENED&bug_status=RESOLVED&bug_status=VERIFIED&columnlist=bug_id%2Cbug_severity%2Cpriority%2Ctarget_milestone%2Cbug_status%2Cresolution%2Ccomponent%2Cassigned_to%2Cshort_desc&list_id=19084396&product=Scout&query_format=advanced).

## Stay in Touch

* [Twitter](https://twitter.com/EclipseScout)
* [Blog](https://www.bsi-software.com/en/scout-blog)


## Contribution

* [BSI Software](https://www.bsi-software.com/): The contributing organization
* [Contribution Guide](https://wiki.eclipse.org/Scout/Contribution)
* [Source on Eclipse.org](https://git.eclipse.org/c/scout/org.eclipse.scout.rt.git?h=releases%2F10.0)
* [Source on GitHub](https://github.com/eclipse/scout.rt/tree/releases/10.0) (Mirror)

Thank you to all the people who already contributed to Scout!

## License

[EPL-1.0](https://www.eclipse.org/legal/epl-v10.html)
