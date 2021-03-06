package io.sketchware.models.view

import io.sketchware.models.projects.ScaleType
import kotlinx.serialization.Serializable

@Serializable
data class Image(
    var rotate: Int = 0,
    var scaleType: ScaleType = ScaleType.CENTER,
    var resName: String = "default_image"
)