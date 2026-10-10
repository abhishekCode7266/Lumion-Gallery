package com.example.data.engine

import com.example.data.model.MediaItem

enum class AiCommandAction {
    REMOVE_BLUR_PHOTO,
    MAKE_BACKGROUND_WHITE,
    BLUR_BACKGROUND,
    APPLY_WHITE_HAZE,
    FIND_BLURRY_SCENES,
    ENHANCE_MOVIE_SCENE,
    REPLACE_SCENE_IN_MOVIE,
    REMOVE_SCENE_FROM_MOVIE,
    EXTRACT_VIDEO_FRAME,
    EXPORT_MEDIA,
    UNKNOWN
}

data class AiAssistantResponse(
    val userPrompt: String,
    val action: AiCommandAction,
    val title: String,
    val description: String,
    val requiresConfirmation: Boolean = false,
    val isDestructive: Boolean = false,
    val targetMedia: MediaItem? = null
)

object AiAssistantEngine {

    fun parseCommand(prompt: String, activeMedia: MediaItem?): AiAssistantResponse {
        val lower = prompt.trim().lowercase()

        return when {
            lower.contains("remove blur") || lower.contains("deblur") || lower.contains("unblur") || lower.contains("sharpen") || lower.contains("clearer photo") -> {
                AiAssistantResponse(
                    userPrompt = prompt,
                    action = AiCommandAction.REMOVE_BLUR_PHOTO,
                    title = "AI Blur Removal & Clarification",
                    description = "Detected motion/defocus blur. Ready to apply unsharp mask detail recovery, edge sharpening, and noise suppression.",
                    requiresConfirmation = false,
                    targetMedia = activeMedia
                )
            }
            lower.contains("white background") || lower.contains("background white") -> {
                AiAssistantResponse(
                    userPrompt = prompt,
                    action = AiCommandAction.MAKE_BACKGROUND_WHITE,
                    title = "Pure White Studio Background",
                    description = "Segmenting main subject and replacing background with clean white studio backdrop.",
                    requiresConfirmation = false,
                    targetMedia = activeMedia
                )
            }
            lower.contains("blur only the background") || lower.contains("blur background") || lower.contains("portrait blur") -> {
                AiAssistantResponse(
                    userPrompt = prompt,
                    action = AiCommandAction.BLUR_BACKGROUND,
                    title = "AI Background Blur (Depth of Field)",
                    description = "Extracting subject with feathered mask while applying smooth cinematic depth-of-field background blur.",
                    requiresConfirmation = false,
                    targetMedia = activeMedia
                )
            }
            lower.contains("white haze") || lower.contains("white blur") || lower.contains("soft glow") -> {
                AiAssistantResponse(
                    userPrompt = prompt,
                    action = AiCommandAction.APPLY_WHITE_HAZE,
                    title = "White Haze & Soft Luminescence",
                    description = "Applying dreamy white-tinted soft diffusion blur with adjustable opacity.",
                    requiresConfirmation = false,
                    targetMedia = activeMedia
                )
            }
            lower.contains("find blurry scene") || lower.contains("detect blur") || lower.contains("which scene is blurry") -> {
                AiAssistantResponse(
                    userPrompt = prompt,
                    action = AiCommandAction.FIND_BLURRY_SCENES,
                    title = "Scan & Detect Blurry Scenes",
                    description = "Analyzing video timeline and measuring frame Laplacian blur variance to highlight blurry scenes.",
                    requiresConfirmation = false,
                    targetMedia = activeMedia
                )
            }
            lower.contains("enhance scene") || lower.contains("clearer video") || lower.contains("enhance movie") || lower.contains("deblur video") -> {
                AiAssistantResponse(
                    userPrompt = prompt,
                    action = AiCommandAction.ENHANCE_MOVIE_SCENE,
                    title = "AI Movie Scene Restoration",
                    description = "Applying motion deblurring, temporal stabilization, and low-light recovery to selected video scene.",
                    requiresConfirmation = false,
                    targetMedia = activeMedia
                )
            }
            lower.contains("replace") && (lower.contains("scene") || lower.contains("version")) -> {
                AiAssistantResponse(
                    userPrompt = prompt,
                    action = AiCommandAction.REPLACE_SCENE_IN_MOVIE,
                    title = "Replace Scene in Movie",
                    description = "Replacing blurry scene with the enhanced version while preserving surrounding scenes and audio tracks.",
                    requiresConfirmation = true,
                    targetMedia = activeMedia
                )
            }
            lower.contains("remove") && lower.contains("scene") -> {
                AiAssistantResponse(
                    userPrompt = prompt,
                    action = AiCommandAction.REMOVE_SCENE_FROM_MOVIE,
                    title = "Delete Selected Scene",
                    description = "Removing this scene segment from the timeline and joining surrounding footage seamlessly.",
                    requiresConfirmation = true,
                    isDestructive = true,
                    targetMedia = activeMedia
                )
            }
            lower.contains("extract frame") || lower.contains("save frame") -> {
                AiAssistantResponse(
                    userPrompt = prompt,
                    action = AiCommandAction.EXTRACT_VIDEO_FRAME,
                    title = "Extract High-Res Video Frame",
                    description = "Extracting current playhead frame and saving to your photo gallery.",
                    requiresConfirmation = false,
                    targetMedia = activeMedia
                )
            }
            lower.contains("export") -> {
                AiAssistantResponse(
                    userPrompt = prompt,
                    action = AiCommandAction.EXPORT_MEDIA,
                    title = "Export Media Project",
                    description = "Ready to encode and export in high quality (1080p Full HD).",
                    requiresConfirmation = false,
                    targetMedia = activeMedia
                )
            }
            else -> {
                AiAssistantResponse(
                    userPrompt = prompt,
                    action = AiCommandAction.UNKNOWN,
                    title = "Smart Editing Assistance",
                    description = "I can help remove photo blur, add white/background blur, detect blurry movie scenes, enhance video clips, or replace scenes. Try asking: \"Remove blur from this photo\" or \"Find blurry scenes in this movie\".",
                    requiresConfirmation = false,
                    targetMedia = activeMedia
                )
            }
        }
    }
}
