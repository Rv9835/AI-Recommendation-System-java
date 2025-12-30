### Summary

This PR contains quality and testing improvements to prepare the project for Review 1 & 2.

### What changed
- Tightened Checkstyle rules and added Javadoc to public classes.
- Refactored `MySQLConnection` to use short-lived connections.
- Added Flyway migrations and DB tests (H2 + Testcontainers).
- Added unit tests for services and an embedded Jetty end-to-end test.
- Configured JaCoCo, SpotBugs and PMD.

### Checklist
- [ ] Code compiles (`mvn -B verify`) locally
- [ ] Unit tests pass (`mvn -DskipITs=true test`)
- [ ] Integration tests pass (requires Docker)
- [ ] Javadoc generated for public APIs
- [ ] README updated with setup and test instructions
- [ ] No sensitive data in commits

### Notes for reviewers
Run the unit tests first. Integration tests run on PRs via CI and require Docker.
