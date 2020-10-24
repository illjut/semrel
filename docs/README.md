# Gradle semantic-release plugin

Execute [Semantic Release][semrel] inside your Gradle build using `npx`. Also allows you to download a nodejs dist for your build on demand.

## How does it work

The plugin executes `semantic-release` in `dry-run` mode on Gradle project configuration phase. It extracts the detected version and writes it to `project.version`.
`project.ext.isSnapshot` can be used to easily determine if a Snapshot is being build.

## Usage

Configure semantic-release using a `.releaserc.yml` in your `rootProject`

### .releaserc.yml

| key                       | value      | description |
| ---                       | ---        | ---         |
| `gradle.node.detect`  | `boolean`  | Try to autodetect an existing NodeJS installation. If detection is successful, skip NodeJS download and setup. Otherwise use `download` config |
| `gradle.node.download`    | `boolean`  | Download and setup a local NodeJS dist |
| `gradle.node.distUrl`     | `string`   | Base distribution url to retrieve NodeJS binaries |
| `gradle.node.packages`    | `string[]` | Extra packages supplied to `npx` when running `semantic-release`. This should contain the plugins used your `semantic-release` `plugin` config |

For configuration examples see [./.releaserc.yml](./.releaserc.yml)

#### Use local NodeJS

You can disable the download of a NodeJS distribution by configuring

```yml
gradle.node.download = false
```

in the project `.releaserc.yml`. Be sure you have a NodeJS on your OS `PATH`




[semrel]: https://github.com/semantic-release/semantic-release