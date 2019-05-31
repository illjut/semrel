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
    def cacheCompleteMarker = project.file("${semrelDir}/.cacheComplete")
    def workDir = project.file("${semrelDir}/")
    def nodeCache = project.file("${semrelDir}/node_modules")
    def packageJson = project.file("${semrelDir}/package.json")

    def nodeVersion = "10.15.3"
    def semanticReleaseVersion = "15"

    def grgit = Grgit.open(dir: project.rootProject.projectDir)
    def gitDescribe = grgit.describe(longDescr: false, tags: true)
    def currentBranch = grgit.branch.current()
    def gitStatus = grgit.status()

    grgit.close()

    def config = new SemanticReleaseConfig(project.rootProject.file(".releaserc.yml"))
    def node = new NodeExec(config.distUrl, project.logger)

    project.configure(project) {

      // create semrel build directory
      project.file(semrelDir).mkdirs();

      def versionPattern = /^SEMREL:\s+\[(.*)\]\s+\[(.*)\]\s+\[(.*)\]/
      packageJson.text = groovy.json.JsonOutput.toJson(
        [ 
          name: project.name ?: "unknown",
          branch: currentBranch.name, // set to current branch to retrieve version
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

      if (config.autoDetectNode) {
        project.logger.info "trying to autodetect a nodejs installation"

        if(node.isNodeAvailable()) {
          project.logger.info "node is available on PATH"
        } else {
          project.logger.info "node is not available on PATH"
          if (config.downloadNode && !completeMarker.exists()) { // download node
            node.setupNode(nodeVersion, project.file(semrelDir))
            completeMarker.createNewFile()
          }
        }
      } else {
        project.logger.info "skipped nodejs autodetection"
        if (config.downloadNode && !completeMarker.exists()) { // download node
          node.setupNode(nodeVersion, project.file(semrelDir))
          completeMarker.createNewFile()
        }
      }

      if (!cacheCompleteMarker.exists()) { // prepare cache for faster executions
        project.logger.info "preparing npm cache for faster executions."
        node.executeNpm(['i', '-D', "semantic-release@v${semanticReleaseVersion}".toString(), "@semantic-release/exec"], workDir)
        cacheCompleteMarker.createNewFile()
      }

      def extraPackages = [ ]

      // invoke npx to run semantic release
      def result = node.executeNpx(
        ['--no-install', 'semantic-release', '--prepare', '--dry-run'],
        extraPackages,
        workDir
      )

      // retrieve information from semantic-release stdout
      def versionFound = false
      def branch = null;
      def lastVersion = null;
      def snapshot = false;

      for (String line : result.log) { // iterate through log and try to find version marker
        def matcher = (line =~ versionPattern)
        if(matcher.find()) {
          branch = matcher.group(1);
          lastVersion = matcher.group(2);
          project.version = matcher.group(3)
          versionFound = true
          break;
        }
      }

      if (!gitStatus.isClean()) { // local repo has uncommitted changes
        project.logger.info "The local git repository is not clean. There are uncommitted changes."

        if (project.logger.isEnabled(org.gradle.api.logging.LogLevel.INFO)) { // print uncommitted files
          gitStatus.staged.getAllChanges().each {
            project.logger.info "staged change  : {}", it
          }

          gitStatus.unstaged.getAllChanges().each {
            project.logger.info "unstaged change: {}", it
          }
        }
        snapshot = true;
      } else if (currentBranch.name != config.branch) { // we are currently not on the release branch
        project.logger.debug "currently not on release branch"
        snapshot = true;
      } else { // we are currently on the release branch
        project.logger.info "currently on release branch {}", config.branch
        snapshot = false;
      }

      if (snapshot || !versionFound) { // append snapshot tag to version
        if (gitDescribe == null) {
          project.version = "${currentBranch.name}-SNAPSHOT"
        } else {
          project.version = "${gitDescribe}-${currentBranch.name}-SNAPSHOT"
        }
      }

      if(!versionFound) { // use current branch to create temporary version, if neccessary
        project.logger.info "Could not retrieve version via semantic-release. If this is unexpected see the semantic-release log for details."
        project.logger.info "Assuming this is not a release branch."

        // remove invalid characters
        project.version = project.version.replace('/', '-')
      }

      project.logger.quiet "Inferred version: ${project.version}"
      project.ext.isSnapshot = snapshot
    }

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