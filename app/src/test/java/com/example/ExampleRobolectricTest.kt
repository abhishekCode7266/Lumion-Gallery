package com.example

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.example.data.engine.AiAssistantEngine
import com.example.data.engine.AiCommandAction
import com.example.data.engine.AiImageProcessingEngine
import com.example.data.engine.MovieSceneEngine
import com.example.data.engine.SceneEnhancementSettings
import com.example.data.model.MediaItem
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Lumina Gallery", appName)
  }

  @Test
  fun `media item properties and formatting`() {
    val photo = MediaItem(
      uri = "content://media/1",
      name = "IMG_Test.jpg",
      mimeType = "image/jpeg",
      sizeBytes = 2_500_000L,
      width = 1920,
      height = 1080
    )
    assertFalse(photo.isVideo)
    assertEquals("2.4 MB", photo.formattedSize)

    val video = MediaItem(
      uri = "content://media/2",
      name = "VID_Test.mp4",
      mimeType = "video/mp4",
      sizeBytes = 15_000_000L,
      durationMs = 75_000L
    )
    assertTrue(video.isVideo)
    assertEquals("1:15", video.formattedDuration)
  }

  @Test
  fun `ai image deblur algorithm processes bitmap`() = runBlocking {
    val input = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    val result = AiImageProcessingEngine.removeBlurAndClarify(
      source = input,
      deblurStrength = 1.0f,
      sharpenStrength = 1.0f,
      denoiseStrength = 0.5f
    )
    assertNotNull(result)
    assertEquals(100, result.width)
    assertEquals(100, result.height)
  }

  @Test
  fun `movie scene engine detects scenes and enhances blurry scene`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val engine = MovieSceneEngine(context)

    val video = MediaItem(
      uri = "content://media/10",
      name = "Movie_Clip.mp4",
      mimeType = "video/mp4",
      durationMs = 20000L
    )

    val scenes = engine.detectScenes(video)
    assertTrue(scenes.isNotEmpty())

    val blurryScene = scenes.firstOrNull { it.isBlurry } ?: scenes.first()
    val enhanced = engine.enhanceScene(blurryScene, SceneEnhancementSettings(deblurStrength = 1.5f))
    assertTrue(enhanced.isEnhanced)
    assertFalse(enhanced.isBlurry)
  }

  @Test
  fun `ai assistant parses natural language editing commands`() {
    val photoResp = AiAssistantEngine.parseCommand("remove blur from this photo", null)
    assertEquals(AiCommandAction.REMOVE_BLUR_PHOTO, photoResp.action)

    val bgResp = AiAssistantEngine.parseCommand("make the background white", null)
    assertEquals(AiCommandAction.MAKE_BACKGROUND_WHITE, bgResp.action)

    val sceneResp = AiAssistantEngine.parseCommand("find blurry scenes in movie", null)
    assertEquals(AiCommandAction.FIND_BLURRY_SCENES, sceneResp.action)
  }
}
