package `fun`.kotlingang.sketchware.objects.project.content

import `fun`.kotlingang.sketchware.interfaces.Identifiable
import `fun`.kotlingang.sketchware.objects.SWConst

enum class ActivityTheme(override val id: Int) : Identifiable {
    /**
     * Says that activity don't have any special theme.
     */
    NONE(SWConst.Theme.THEME_NONE),

    /**
     * Says that activity has default theme.
     */
    DEFAULT(SWConst.Theme.THEME_DEFAULT),

    /**
     * Says that activity has no action bar.
     */
    NO_ACTIONBAR(SWConst.Theme.THEME_NO_ACTIONBAR),

    /**
     * Says that activity has fullscreen theme.
     */
    FULL_SCREEN(SWConst.Theme.THEME_FULL_SCREEN)
}