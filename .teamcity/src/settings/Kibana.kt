package settings

import builds.*
import builds.default.*
import builds.oss.*
import builds.test.*
import jetbrains.buildServer.configs.kotlin.v2019_2.*
import templates.DefaultTemplate
import DefaultRoot
import kibanaAgent

object Kibana : Project ({
  params {
    param("teamcity.ui.settings.readOnly", "true")
  }

  vcsRoot(DefaultRoot)
  template(DefaultTemplate)

  defaultTemplate = DefaultTemplate

//        triggers {
//            vcs {
//                perCheckinTriggering = true
//            }
//        }
//    }


  features {
    val sizes = listOf("2", "4", "8", "16")
    for (size in sizes) {
      kibanaAgent(size)
    }

    feature {
      id = "kibana-brianseeders"
      type = "CloudProfile"
      param("agentPushPreset", "")
      param("profileId", "kibana-brianseeders")
      param("profileServerUrl", "")
      param("name", "kibana-brianseeders")
      param("total-work-time", "")
      param("credentialsType", "key")
      param("description", "")
      param("next-hour", "")
      param("cloud-code", "google")
      param("terminate-after-build", "true")
      param("terminate-idle-time", "30")
      param("enabled", "true")
      param("secure:accessKey", "credentialsJSON:447fdd4d-7129-46b7-9822-2e57658c7422")
    }
  }

  buildType(Lint)

  val ossCiGroups = (1..12).map { OssCiGroup(it) }
  val defaultCiGroups = (1..10).map { DefaultCiGroup(it) }

  subProject {
    id("Test")
    name = "Test"

    subProject {
      id("Jest")
      name = "Jest"

      buildType(Jest)
      buildType(XPackJest)
      buildType(JestIntegration)
    }

    buildType(ApiIntegration)
    buildType(AllTests)
  }

  subProject {
    id("OSS")
    name = "OSS Distro"

    buildType(OssBuild)

    subProject {
      id("OSS_Functional")
      name = "Functional"

      buildType {
        id("CIGroups_Composite")
        name = "CI Groups"
        type = BuildTypeSettings.Type.COMPOSITE

        dependencies {
          for (ciGroup in ossCiGroups) {
            snapshot(ciGroup) {
              reuseBuilds = ReuseBuilds.SUCCESSFUL
              onDependencyCancel = FailureAction.CANCEL
              onDependencyFailure = FailureAction.CANCEL
              synchronizeRevisions = true
            }
          }
        }
      }

      buildType(OssVisualRegression)

      subProject {
        id("CIGroups")
        name = "CI Groups"

        for (ciGroup in ossCiGroups) buildType(ciGroup)
      }
    }
  }

  subProject {
    id("Default")
    name = "Default Distro"

    buildType(DefaultBuild)

    subProject {
      id("Default_Functional")
      name = "Functional"

      buildType {
        id("Default_CIGroups_Composite")
        name = "CI Groups"
        type = BuildTypeSettings.Type.COMPOSITE

        dependencies {
          for (ciGroup in defaultCiGroups) {
            snapshot(ciGroup) {
              reuseBuilds = ReuseBuilds.SUCCESSFUL
              onDependencyCancel = FailureAction.CANCEL
              onDependencyFailure = FailureAction.CANCEL
              synchronizeRevisions = true
            }
          }
        }
      }

      buildType(DefaultVisualRegression)

      subProject {
        id("Default_CIGroups")
        name = "CI Groups"

        for (ciGroup in defaultCiGroups) buildType(ciGroup)
      }
    }
  }

  // This job is temporary
  buildType {
    id("Kitchen_Sink")
    name = "Kitchen Sink"
    type = BuildTypeSettings.Type.COMPOSITE

    dependencies {
      val builds = listOf(
        AllTests,
        OssVisualRegression,
        DefaultVisualRegression,
        Lint,
        ossCiGroups[0],
        defaultCiGroups[0]
      )

      for (build in builds) {
        snapshot(build) {
          reuseBuilds = ReuseBuilds.SUCCESSFUL
          onDependencyCancel = FailureAction.CANCEL
          onDependencyFailure = FailureAction.CANCEL
          synchronizeRevisions = true
        }
      }
    }
  }
})
