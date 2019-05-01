package de.illjut.gradle.semrel

import org.gradle.api.*
import java.io.*
import com.moowork.gradle.node.task.*
import com.moowork.gradle.node.npm.*
import com.moowork.gradle.node.*

import org.ajoberstar.grgit.Grgit

class SemrelPlugin implements Plugin<Project> {
  static final GROUP_NAME = "Semantic Release"

  void apply(Project project) {
    def semrelDir = "${project.buildDir}/semrel"
    def completeMarker = project.file("${semrelDir}/.complete")
    def workDir = project.file("${semrelDir}/")
    def nodeCache = project.file("${semrelDir}/node_modules")
    def packageJson = project.file("${semrelDir}/package.json")

    def nodeVersion = "10.15.3"
    def semanticReleaseVersion = "15"

    def config = new SemanticReleaseConfig(project.rootProject.file(".releaserc.yml"))
    def node = new NodeExec(config.distUrl, project.logger)

    project.configure(project) {

      // create semrel build directory
      project.file(semrelDir).mkdirs();

      def versionPattern = /^SEMREL:\s+\[(.*)\]\s+\[(.*)\]\s+\[(.*)\]/
      packageJson.text = groovy.json.JsonOutput.toJson(
        [ 
          name: project.name ?: "unknown",
          branch: config.branch,
          release: [
            verifyConditions: "@semantic-release/exec",
            plugins: [
              "@semantic-release/commit-analyzer",
              ["@semantic-release/exec", [
                verifyReleaseCmd: 'echo SEMREL: [${options.branch}] [${lastRelease.version}] [${nextRelease.version}]'
              ]]
            ]
          ]
        ]
      )

      // acquire nodejs dist
      if (config.downloadNode && !completeMarker.exists()) {
        node.setupNode(nodeVersion, project.file(semrelDir))
        completeMarker.createNewFile();
      }

      // prepare cache for faster executions
      if (!project.file("$nodeCache/semantic-release").exists()) {
        project.logger.info "preparing npm cache for faster executions"
        node.executeNpm(['i', '-D', "semantic-release@v${semanticReleaseVersion}".toString(), "@semantic-release/exec"], workDir)
      }

      def extraPackages = [ ]

      // invoke npx to run semantic release
      def result = node.executeNpx(
        ['--no-install', 'semantic-release', '--prepare', '--dry-run'],
        extraPackages,
        workDir
      )

      // retrieve information from semantic-release stdout
      def lines = new BufferedReader(new InputStreamReader(result.stdErr))
      def line = null
      def versionFound = false
      def branch = null;
      def lastVersion = null;
      while ((line = lines.readLine()) != null) {
        def matcher = (line =~ versionPattern)
        if(matcher.find()) {
          branch = matcher.group(1);
          lastVersion = matcher.group(2);
          project.version = matcher.group(3)
          versionFound = true
          break;
        }
      }

      // use grgit to create temporary version, if neccessary
      if(!versionFound) {
        project.logger.info "Could not retrieve version via semantic-release. If this is unexpected see the semantic-release log for details."
        project.logger.info "Assuming this is not a release branch."
        def grgit = Grgit.open(dir: project.rootProject.projectDir)

        def gitDescribe = grgit.describe()

        if (gitDescribe == null) {
          project.version = "${grgit.branch.current().name}-SNAPSHOT"
        } else {
          def pattern = /^(.*)-\d+-\w+$/

          def matcher = (gitDescribe =~ pattern)
          if (matcher.hasGroup()) {
            project.version = "${matcher[0][1]}-${grgit.branch.current().name}-SNAPSHOT"
          } else {
            project.version = "${grgit.branch.current().name}-SNAPSHOT"
          }
        }

        // remove invalid characters
        project.version = project.version.replace('/', '-')

        grgit.close()
      }

      project.logger.quiet "Inferred version: ${project.version}"
    }

    project.afterEvaluate {
      project.tasks.register('release') {
        description "Runs semantic-relase on the root project"
        group GROUP_NAME

        doLast {
          def extraPackages = ["semantic-release@v${semanticReleaseVersion}".toString()]
          extraPackages.addAll config.packages

          def result = node.executeNpx([
              'semantic-release'
            ],
            extraPackages,
            project.rootProject.projectDir
          )

          project.logger.info "semantic-release exited with {}", result.exitCode
          if(result.exitCode != 0) {
            throw new GradleScriptException("Release failed.", new Exception("semantic-release did not exit successfully. See log above (use -i)."));
          }
        }
      }
    }
  }
}