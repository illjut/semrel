# Gradle semantic-release

Execute [Semantic Release][semrel] inside your Gradle build using `npx`. Also allows you to download a nodejs dist for your build on demand.

## How does it work

The plugin executes `semantic-release` in `dry-run` mode on Gradle project configuration phase. It extracts the detected version and writes it to `project.version`.

<details><summary>show flow diagram</summary>
<p>

![init diagram](./docs/initialization.png)

</p>
</details>

## Usage

Configure semantic-release using a `.releaserc.yml` in your `rootProject`

### .releaserc.yml

| key                       | value      | description |
| ---                       | ---        | ---         |
| `gradle.node.autoDetect`  | `boolean`  | Try to autodetect an existing NodeJS installation. If detection is successful, skip NodeJS download and setup. Otherwise use `download` config |
| `gradle.node.download`    | `boolean`  | Download and setup a local NodeJS dist |
| `gradle.node.distUrl`     | `string`   | Base distribution url to retrieve NodeJS binaries |
| `gradle.node.packages`    | `string[]` | Extra packages supplied to `npx` when running `semantic-release`. This should contain the plugins used your `semantic-release` `plugin` config |

For configuration examples see [./.releaserc.yml](./.releaserc.yml)

### Build Performance

Build Performance can be improved

#### Use local NodeJS

You can disable the download of a NodeJS distribution by configuring

```yml
gradle.node.download = false
```

in the project `.releaserc.yml`. Be sure you have a NodeJS on your OS `PATH`




[semrel]: https://github.com/semantic-release/semantic-release