package `fun`.kotlingang.sketchware.objects.project.view.widgets

import `fun`.kotlingang.sketchware.objects.project.view.properties.WidgetProperties

class ScrollViewWidget(
    view: WidgetProperties,
    _children: MutableList<BaseWidget>
) : ViewGroupWidget(view, _children)