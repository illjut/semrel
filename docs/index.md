# Gradle semantic-release plugin

Execute [Semantic Release][semrel] inside your Gradle build using `npx`. Also allows you to download a nodejs dist for your build on demand.

## How does it work

The plugin executes `semantic-release` in `dry-run` mode on Gradle project configuration phase. It extracts the detected version and writes it to `project.version`.
`project.ext.isSnapshot` can be used to easily determine if a Snapshot is being build.

## Usage

Configure semantic-release using a `.releaserc.yml` in your `rootProject`. See Configuration for more details.



[semrel]: https://github.com/semantic-release/semantic-release