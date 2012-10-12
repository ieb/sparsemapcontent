#!/bin/sh
curl http://www.ettrema.com/maven2/com/ettrema/milton-api/1.8.1.4/milton-api-1.8.1.4.jar -o /tmp/milton-api-1.8.1.4.jar
curl http://www.ettrema.com/maven2/com/ettrema/milton-api/1.8.1.4/milton-api-1.8.1.4.pom -o  /tmp/milton-api-1.8.1.4.pom
curl http://www.ettrema.com/maven2/com/ettrema/milton-servlet/1.8.1.4/milton-servlet-1.8.1.4.jar -o /tmp/milton-servlet-1.8.1.4.jar
curl http://www.ettrema.com/maven2/com/ettrema/milton-servlet/1.8.1.4/milton-servlet-1.8.1.4.pom -o /tmp/milton-servlet-1.8.1.4.pom
curl http://www.ettrema.com/maven2/com/ettrema/milton/1.8.1.4/milton-1.8.1.4.pom -o /tmp/milton-1.8.1.4.pom
mvn --quiet install:install-file -DgroupId=com.ettrema -DartifactId=milton-api -Dpackaging=pom -Dversion=1.8.1.4 -Dfile=/tmp/milton-api-1.8.1.4.pom
mvn --quiet install:install-file -DgroupId=com.ettrema -DartifactId=milton-api -Dpackaging=jar -Dversion=1.8.1.4 -Dfile=/tmp/milton-api-1.8.1.4.jar
mvn --quiet install:install-file -DgroupId=com.ettrema -DartifactId=milton-servlet -Dpackaging=pom -Dversion=1.8.1.4 -Dfile=/tmp/milton-servlet-1.8.1.4.pom
mvn --quiet install:install-file -DgroupId=com.ettrema -DartifactId=milton-servlet -Dpackaging=jar -Dversion=1.8.1.4 -Dfile=/tmp/milton-servlet-1.8.1.4.jar
mvn --quiet install:install-file -DgroupId=com.ettrema -DartifactId=milton -Dpackaging=pom -Dversion=1.8.1.4 -Dfile=/tmp/milton-1.8.1.4.pom

