# Gradle semantic-release

Execute [Semantic Release][semrel] inside your Gradle build using `npx`. Also allows you to download a nodejs dist for your build on demand.

## How does it work

The plugin executes `semantic-release` in `dry-run` mode on Gradle project configuration phase. It extracts the detected version and writes it to `project.version`.



[semrel]: https://github.com/semantic-release/semantic-release