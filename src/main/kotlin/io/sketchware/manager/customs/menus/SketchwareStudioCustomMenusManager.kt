package io.sketchware.manager.customs.menus

import io.sketchware.interfaces.CustomMenusManager
import io.sketchware.interfaces.Editor
import io.sketchware.interfaces.listeners.ActionFinishListener
import io.sketchware.models.customs.CustomMenu
import io.sketchware.models.customs.SWStudioMenu
import io.sketchware.utils.delegates.lazyInit
import io.sketchware.utils.internal.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class SketchwareStudioCustomMenusManager(
    private var menusValue: String,
    private val menusFile: File
) : CustomMenusManager, Editor, CoroutineScope {

    companion object {
        suspend operator fun invoke(menusFile: File) =
            SketchwareStudioCustomMenusManager(menusFile.read().byteArrayToString(), menusFile)
    }

    private val menusProperty = lazyInit {
        menusValue.serialize<List<SWStudioMenu>>().map {
            CustomMenu(it.name, it.name, it.title, it.data)
        }
    }

    /**
     * @return list of custom menus.
     */
    override val menus by menusProperty

    /**
     * Adds custom menu.
     * @param menu menu to add
     */
    override fun addCustomMenu(menu: CustomMenu) = saveCustomMenus(
        menus.toMutableList().also { it.add(menu) }
    )

    /**
     * Removes custom menu by id.
     * @param id menu's string id.
     */
    override fun removeMenuById(id: String) = saveCustomMenus(
        menus.toMutableList().also { menus ->
            menus.removeIf { it.id == id }
        }
    )

    /**
     * Edits custom menu.
     * @param id menu string id.
     * @param builder Lambda with [CustomMenu] in context to edit already exists menu data.
     */
    override fun editMenu(id: String, builder: CustomMenu.() -> Unit) = editMenu(
        id, menus.toMutableList().first { it.id == id }.apply(builder)
    )

    /**
     * Edits custom menu.
     * @param id menu string id.
     * @param menu new menu data.
     */
    override fun editMenu(id: String, menu: CustomMenu) = saveCustomMenus(
        menus.toMutableList().apply {
            val oldMenu = first { it.id == id }
            set(indexOf(oldMenu), menu)
        }
    )

    /**
     * Imports custom menus from the [file].
     * @param file - file with data about menus.
     * @param conflictProvider - provider for conflict names.
     * If provider isn't specified or it is returning same value as specified
     * in [conflictProvider#conflicId],prime menu will be removed.
     */
    override suspend fun import(file: File, conflictProvider: ((conflictId: String) -> String)?) {
        val newMenus = file.read().byteArrayToString().serialize<List<CustomMenu>>()
        val allMenus = menus.toMutableList()
        newMenus.forEach { menu ->
            allMenus.find { it.id == menu.id }?.let {
                val currentId = it.id
                it.id = conflictProvider?.invoke(currentId) ?: currentId
                if (currentId == it.id)
                    allMenus.remove(it)
            }
        }
        menusProperty.reset()
        saveCustomMenus(allMenus.plus(newMenus))
    }

    /**
     * Imports custom menus from the [file].
     * @param file - file with data about menus.
     * @param conflictProvider - provider for conflict names.
     * If provider isn't specified or it is returning same value as specified
     * in [conflictProvider#conflicId],prime menu will be removed.
     * @param callback - will be called when import will be finished.
     */
    override fun import(
        file: File,
        conflictProvider: ((conflictId: String) -> String)?,
        callback: ActionFinishListener
    ) = launch {
        import(file, conflictProvider)
        callback.onFinish()
    }

    /**
     * Exports data into file.
     * @param destination - file to which will be written data.
     */
    override suspend fun export(destination: File) {
        destination.write(menus.deserialize().toByteArray())
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

    private fun saveCustomMenus(list: List<CustomMenu>) {
        menusValue = list.map {
            it.toSWStudioMenu()
        }.deserialize()
        menusProperty.reset()
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
        menusValue = menusFile.read().byteArrayToString()
        menusProperty.reset()
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
        menusFile.write(menusValue.toByteArray())
    }

    override val coroutineContext = Dispatchers.IO
}