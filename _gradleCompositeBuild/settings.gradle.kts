// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

// Composite Build for all SDK Code Sample projects (excluding those under /product_specific/ to reduce dependencies)

rootProject.name = "SDK Code Samples"

includeBuild("../comparing_string_references_inspection")
includeBuild("../conditional_operator_intention")
includeBuild("../facet_basics")
includeBuild("../framework_basics")
includeBuild("../max_opened_projects")
includeBuild("../module")
includeBuild("../project_model")
includeBuild("../project_view_pane")
includeBuild("../project_wizard")
includeBuild("../run_configuration")
includeBuild("../simple_language_plugin")
includeBuild("../tree_structure_provider")

//includeBuild("../live_templates")   //OK
//includeBuild("../tool_window")      //OK
//includeBuild("../kotlin_demo")      //OK
//includeBuild("../editor_basics")    // OK
//includeBuild("../action_basics")    //OK
//includeBuild("../psi_demo")         //OK
