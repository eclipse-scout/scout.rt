#!/usr/bin/python

# find list of maven dependencies and check the result against existing eclipse cqs
# (1) create list of dependencies (in org.eclipse.scout.rt root directory): mvn dependency:list 2>&1 | tee dependencies.txt
# (2) run this script: python processDependencyList.py dependencies.txt | cut -d' ' -f2- | sort -u

import sys

cq_approved = {
'org.apache.commons:commons-exec:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=6553',
'commons-codec:commons-codec:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=7615',
'com.google.guava:guava:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=6547',
'org.apache.commons:commons-lang3:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10282',
'org.objenesis:objenesis:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11298',
'xerces:xercesImpl:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=5201',
'org.slf4j:jcl-over-slf4j:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11278',
'org.mozilla:rhino:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10067',
'javax.xml.bind:jaxb-api:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11294',
'javax.xml.soap:javax.xml.soap-api:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11295',
'javax.xml.ws:jaxws-api:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11293',
'javax.jws:jsr181-api:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11296',
'javax.jms:jms-api:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=4516',
'com.yahoo.platform.yui:yuicompressor:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10071',
'com.asual.lesscss:lesscss-engine:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10076;https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10203',
'org.hamcrest:hamcrest-core:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=7200',
'org.mockito:mockito-core:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11290',
'org.jboss:jandex:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=9956',
'org.apache.xmlgraphics:batik-util:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=8325',
'org.apache.xmlgraphics:batik-swing:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=8323',
'org.apache.xmlgraphics:batik-parser:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=8321',
'org.apache.xmlgraphics:batik-awt-util:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=8320',
'org.apache.xmlgraphics:batik-svg-dom:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=8319',
'org.apache.xmlgraphics:batik-dom:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=8318',
'org.apache.xmlgraphics:batik-bridge:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=8317',
'org.apache.xmlgraphics:batik-ext:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=6132',
'log4j:log4j:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11285',
'org.apache.commons:commons-math3:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11288',
'junit:junit:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11286',
'javax.servlet:javax.servlet-api:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=8922',
'javax.annotation:javax.annotation-api:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11292',
'javax.activation:activation:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=4497',
'commons-io:commons-io:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11276',
'commons-fileupload:commons-fileupload:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10065',
'ch.qos.logback:logback-core:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=4499',
'com.unquietcode.tools.jcodemodel:codemodel:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11297',
'com.sun.mail:javax.mail:jar': 'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11275',
'ch.qos.logback:logback-classic:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10341',
'org.slf4j:jul-to-slf4j:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10342',
'org.slf4j:slf4j-jdk14:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10315',
'org.slf4j:slf4j-log4j12:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10315',
'org.slf4j:slf4j-api:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10315',
'xml-apis:xml-apis-ext:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=7082',
'xalan:xalan:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11279',
'org.quartz-scheduler:quartz:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10651',
'org.ow2.asm:asm-commons:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11277',
'org.ow2.asm:asm-tree:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11277',
'org.ow2.asm:asm:jar':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11277'
}

cq_approved_version = {
'org.apache.commons:commons-exec:jar:1.1':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=6553',
'commons-codec:commons-codec:jar:1.6':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=7615',
'com.google.guava:guava:jar:12.0':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=6547',
'org.apache.commons:commons-lang3:jar:3.1':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10282',
'org.objenesis:objenesis:jar:2.2':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11298',
'xerces:xercesImpl:jar:2.9.0':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=5201',
'org.slf4j:jcl-over-slf4j:jar:1.7.12':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11278',
'org.mozilla:rhino:jar:1.7R4':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10067',
'javax.xml.bind:jaxb-api:jar:2.2.9':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11294',
'javax.xml.soap:javax.xml.soap-api:jar:1.3.5':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11295',
'javax.xml.ws:jaxws-api:jar:2.2.10':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11293',
'javax.jws:jsr181-api:jar:1.0-MR1':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11296',
'javax.jms:jms-api:jar:1.1':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=4516',
'com.yahoo.platform.yui:yuicompressor:jar:2.4.8':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10071',
'com.asual.lesscss:lesscss-engine:jar:1.5.1':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10076;https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10203',
'org.hamcrest:hamcrest-core:jar:1.3':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=7200',
'org.mockito:mockito-core:jar:1.10.19':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11290',
'org.jboss:jandex:jar:1.2.2.Final':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=9956',
'org.apache.xmlgraphics:batik-util:jar:1.7':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=8325',
'org.apache.xmlgraphics:batik-swing:jar:1.7':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=8323',
'org.apache.xmlgraphics:batik-parser:jar:1.7':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=8321',
'org.apache.xmlgraphics:batik-awt-util:jar:1.7':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=8320',
'org.apache.xmlgraphics:batik-svg-dom:jar:1.7':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=8319',
'org.apache.xmlgraphics:batik-dom:jar:1.7':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=8318',
'org.apache.xmlgraphics:batik-bridge:jar:1.7':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=8317',
'org.apache.xmlgraphics:batik-ext:jar:1.7':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=6132',
'log4j:log4j:jar:1.2.17':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11285',
'org.apache.commons:commons-math3:jar:3.5':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11288',
'junit:junit:jar:4.12':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11286',
'javax.servlet:javax.servlet-api:jar:3.1.0':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=8922',
'javax.annotation:javax.annotation-api:jar:1.2':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11292',
'javax.activation:activation:jar:1.1.1':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=4497',
'commons-io:commons-io:jar:2.2':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11276',
'commons-fileupload:commons-fileupload:jar:1.3.1':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10065',
'ch.qos.logback:logback-core:jar:0.9.19':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=4499',
'com.unquietcode.tools.jcodemodel:codemodel:jar:1.0.3':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11297',
'com.sun.mail:javax.mail:jar:1.5.5': 'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11275',
'ch.qos.logback:logback-classic:jar:1.1.3':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10341',
'org.slf4j:jul-to-slf4j:jar:1.7.12':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10342',
'org.slf4j:slf4j-jdk14:jar:1.7.12':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10315',
'org.slf4j:slf4j-log4j12:jar:1.7.12':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10315',
'org.slf4j:slf4j-api:jar:1.7.12':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10315',
'xml-apis:xml-apis-ext:jar:1.3.04':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=7082',
'xalan:xalan:jar:2.7.0':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11279',
'org.quartz-scheduler:quartz:jar:2.2.2':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=10651',
'org.ow2.asm:asm-commons:jar:5.0.1':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11277',
'org.ow2.asm:asm-tree:jar:5.0.1':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11277',
'org.ow2.asm:asm:jar:5.0.1':'https://dev.eclipse.org/ipzilla/show_bug.cgi?id=11277'
}

def printf(format, *args):
	sys.stdout.write(format % args)

with open(sys.argv[1]) as f:
	for line in f.readlines():
		# process line
		items = line.split()
		
		if(items[0] == '[INFO]'):
			lastToken = items[-1]
			
			# skip over org.eclipse.* stuff  
			if(lastToken.startswith('org.eclipse.jetty') or lastToken.startswith('org.eclipse.scout')):
				continue
			
			if(lastToken.endswith(':compile') or lastToken.endswith(':test') or lastToken.endswith(':provided')):
				dependency = lastToken.split(':')
				jar = ':'.join(dependency[:-2])
				jar_version = ':'.join(dependency[:-1])
				status = 'CQ_MISSING'
				cq_link = ''
				
				# special case for batik
				if(jar_version.startswith('org.apache.xmlgraphics:batik-') and jar_version.endswith(':jar:1.7')):
					status = 'CQ_BATIK'
					cq_link = '<no link for this one>'
				
				if(jar in cq_approved):
					status = 'CQ_BAD_VERSION'
					cq_link = cq_approved[jar]
				
				if(jar_version in cq_approved_version):
					status = 'CQ_OK'
					cq_link = cq_approved_version[jar_version]
				
				printf('%s %s %s %s\n', dependency[-1], status, jar_version, cq_link)
