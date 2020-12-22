## Gradle Configuration

Apply the plugin in your root `build.gradle`:

!!! code "Gradle Buildscript config"
    ```groovy
    buildscript {
      repositories {
        maven { // versions >=1.4.0
          url 'https://maven.pkg.github.com/illjut/semrel'
          credentials {
            username System.getenv("REGISTRY_USER") ?: "none"
            password System.getenv("GITHUB_TOKEN") ?: "none"
          }
        }
        jcenter()
        maven { // versions <1.4.0
          url 'https://dl.bintray.com/illjut/maven'
        }
      }
    }

    apply plugin: 'de.illjut.gradle.semrel'
    ```

You can access the generated version by using `project.version` in your build scripts.


## Semantic-Release Configuration

`semrel` extends the [Semantic-Release][semrel] configuration file `.releaserc.yml`:

!!! example ".releaserc.yml example"
    ```yaml
    # standard semantic-release configuration
    branch: master
    plugins:
      - '@semantic-release/commit-analyzer'
      - '@semantic-release/release-notes-generator'
      - '@semantic-release/github'
    ci: false

    # gradle semrel configuration
    gradle:
      semantic_release:
        version: 17
      node:
        version: 12.19.0
        detect: false # try to detect system node, if found skip download evaluation.
        download: true # download a node dist and use it, if it was not detected
        distUrl: https://nodejs.org/dist # dist download root url (default)
        packages: # extra packages needed in semantic-release plugins
          - '@semantic-release/commit-analyzer'
          - '@semantic-release/release-notes-generator'
          - '@semantic-release/github'
      config:
        registry: https://registry.npmjs.org/
      env:
    ```
### Properties

| key                       | value      | description |
| ---                       | ---        | ---         |
| `gradle.node.detect`  | `boolean`  | Try to autodetect an existing NodeJS installation. If detection is successful, skip NodeJS download and setup. Otherwise use `download` config |
| `gradle.node.download`    | `boolean`  | Download and setup a local NodeJS dist |
| `gradle.node.distUrl`     | `string`   | Base distribution url to retrieve NodeJS binaries |
| `gradle.node.packages`    | `string[]` | Extra packages supplied to `npx` when running `semantic-release`. This should contain the plugins used your `semantic-release` `plugin` config |


[semrel]: https://github.com/semantic-release/semantic-release