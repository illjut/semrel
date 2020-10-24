# Configuration

The plugin is configured using the semantic-release `.releaserc.yml` inside your repository.

``` yaml
branch: master
plugins:
  - '@semantic-release/commit-analyzer'
  - '@semantic-release/release-notes-generator'
  - '@semantic-release/github'
ci: false

gradle: # gradle semrel configuration
  semantic_release:
    version: 17
  node:
    version: 10.16.3
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
