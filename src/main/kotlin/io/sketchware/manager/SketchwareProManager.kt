package io.sketchware.manager

import io.sketchware.interfaces.Manager
import io.sketchware.manager.collections.CollectionsManager
import io.sketchware.manager.customs.SketchwareProCustomManager
import io.sketchware.manager.projects.SketchwareProProjectsManager
import java.io.File

class SketchwareProManager(
    sketchwareFolder: File
) : Manager<SketchwareProProjectsManager> {
    /**
     * @param folderPath - path to sketchware folder.
     */
    constructor(folderPath: String) : this(File(folderPath))

    /**
     * Sketchware projects manager for this instance.
     * Responsible for sketchware projects.
     */
    override val projectsManager = SketchwareProProjectsManager(sketchwareFolder)

    /**
     * Sketchware collections manager.
     * Responsible for sketchware collections.
     */
    override val collectionsManager = CollectionsManager(File(sketchwareFolder, "collections"))

    /**
     * Sketchware Pro Customs Manager. Responsible for sketchware pro customs (components, blocks, etc)
     */
    val customsManager = SketchwareProCustomManager(
        File(sketchwareFolder, "data/system"),
        File(sketchwareFolder, "data/settings.json"), File(sketchwareFolder, "resources/block/Menu Block")
    )

}