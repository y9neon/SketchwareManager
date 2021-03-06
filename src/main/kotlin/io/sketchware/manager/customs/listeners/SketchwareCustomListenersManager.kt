package io.sketchware.manager.customs.listeners

import io.sketchware.interfaces.Editor
import io.sketchware.interfaces.FileExportable
import io.sketchware.interfaces.listeners.ActionFinishListener
import io.sketchware.models.customs.CustomEvent
import io.sketchware.models.customs.CustomListenerGroup
import io.sketchware.utils.delegates.lazyInit
import io.sketchware.utils.internal.*
import io.sketchware.utils.serializers.toSpecFields
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class SketchwareCustomListenersManager(
    private var eventsValue: String,
    private var listenersValue: String,
    private val eventsFile: File,
    private val listenersFile: File
) : CoroutineScope, Editor, FileExportable {
    override val coroutineContext = Dispatchers.IO

    companion object {
        suspend operator fun invoke(
            eventsFile: File,
            listenersFile: File
        ) = SketchwareCustomListenersManager(
            eventsFile.read().byteArrayToString(), listenersFile.read().byteArrayToString(),
            eventsFile, listenersFile
        )
    }

    private val activityListenerMap by lazy {
        mapOf(
            "name" to "", "s" to "true", "imports" to "", "code" to ""
        )
    }

    private val listenerProperty = lazyInit {
        val events = eventsValue.serialize<List<Map<String, String>>>()
        val listeners = listenersValue.serialize<List<Map<String, String>>>().plus(activityListenerMap)
        return@lazyInit listeners.map {
            CustomListenerGroup(
                it.getValue("name"), it.getValue("s").toBoolean(),
                it.getValue("imports"), it.getValue("code"), events.filter { eventMap ->
                    it.getValue("name") == eventMap.getValue("listener")
                }.map { map ->
                    CustomEvent(
                        map.getValue("headerSpec").toSpecFields(), map.getValue("icon").toInt(),
                        map.getValue("var"), map.getValue("description"), map.getValue("parameters"),
                        map.getValue("name"), map.getValue("code")
                    )
                })
        }
    }

    /**
     * @return list of listeners group.
     */
    val listeners by listenerProperty

    /**
     * Adds listener group.
     * @param group - group to save.
     */
    fun addListenerGroup(group: CustomListenerGroup) = saveLocally(
        listeners.toMutableList().apply { add(group) }
    )

    /**
     * Removes listener group by [name].
     */
    fun removeListenerGroup(name: String) = saveLocally(
        listeners.toMutableList().apply {
            removeIf { it.name == name }
        }
    )

    /**
     * Edits listener group.
     */
    fun editListenerGroup(
        name: String,
        editor: (CustomListenerGroup) -> Unit
    ) = saveLocally(listeners.toMutableList().apply {
        val index = indexOfFirst { it.name == name }
        set(index, get(index).apply(editor))
    })

    private fun saveLocally(list: List<CustomListenerGroup>) = synchronized(this) {
        listenersValue = list
            .filter { it.name != "" }
            .map {
                mapOf(
                    "name" to it.name,
                    "code" to it.code,
                    "s" to it.independent.toString(),
                    "imports" to it.customImport
                )
            }.deserialize()
        eventsValue = list.map { group ->
            group.events.map {
                mapOf(
                    "headerSpec" to it.spec.deserialize(),
                    "icon" to it.iconId.toString(),
                    "var" to it.id,
                    "description" to it.description,
                    "parameters" to it.parameters,
                    "name" to it.name,
                    "code" to it.code,
                    "listener" to group.name
                )
            }
        }.flatten().deserialize()
        listenerProperty.reset()
    }

    /**
     * Updates data in Editor async.
     */
    override fun fetch(callback: ActionFinishListener?) = launch {
        fetch()
        callback?.onFinish()
    }

    /**
     * Updates data in Editor.
     */
    override suspend fun fetch() {
        eventsValue = eventsFile.read().byteArrayToString()
        listenersValue = listenersFile.read().byteArrayToString()
        listenerProperty.reset()
    }

    /**
     * Saves data which was edited async.
     */
    override fun save(callback: ActionFinishListener?) = launch {
        save()
        callback?.onFinish()
    }

    /**
     * Saves data which was edited.
     */
    override suspend fun save() {
        eventsFile.write(eventsValue.toByteArray())
    }

    /**
     * Imports Sketchware Custom Listeners
     * @param file - custom listeners file destination.
     * @param listenerNameProvider - provider for name conflicts.
     * If provider won't be specified, exists listener in current scope will be deleted.
     * @param callback - will be invoked when action will be finished.
     */
    fun import(
        file: File,
        listenerNameProvider: ((String) -> String)? = null,
        callback: ActionFinishListener? = null
    ) = launch {
        import(file, listenerNameProvider)
        callback?.onFinish()
    }

    /**
     * Imports Sketchware Custom Listeners
     * @param file - custom listeners file destination.
     * @param listenerNameProvider - provider for name conflicts.
     * If provider won't be specified, exists listener in current scope will be deleted.
     */
    suspend fun import(file: File, listenerNameProvider: ((String) -> String)? = null) {
        val imports = file.read().byteArrayToString().serialize<List<CustomListenerGroup>>()
        val allListeners = listeners.toMutableList()
        imports.forEach { newGroup ->
            allListeners.find { it.name == newGroup.name }?.let { group ->
                if (group != newGroup) {
                    newGroup.name = listenerNameProvider?.invoke(group.name) ?: group.name.also {
                        allListeners.remove(group)
                    }
                }
            }
        }
        saveLocally(allListeners.plus(imports))
        listenerProperty.reset()
    }

    /**
     * Exports data into file.
     * @param destination - file to which will be written data.
     */
    override suspend fun export(destination: File) {
        destination.write(listeners.deserialize().toByteArray())
    }

    /**
     * Exports data into file.
     * @param destination - file to which will be written data.
     * @param callback - call back when export will be finished.
     */
    override fun export(destination: File, callback: ActionFinishListener) = launch {
        export(destination)
        callback.onFinish()
    }
}