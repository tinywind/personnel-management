# personnel-management

Requires JDK 17 and Maven 3. Run `mvn verify` to compile the Jakarta persistence model, run the three real H2/HTTP contracts, and build the executable WAR at `target/pm-0.1.war`.

Run `java -jar target/pm-0.1.war --server.port=8080` and open `/hello`. The existing `PersonRepository` and immutable creation timestamp remain in place. The development H2 web console is disabled.

Tests use a separate in-memory H2 database, bind HTTP to loopback on an ephemeral port, verify repository create/update/delete and the rendered Thymeleaf view, and check the console returns HTTP 404. They do not open the default file database.