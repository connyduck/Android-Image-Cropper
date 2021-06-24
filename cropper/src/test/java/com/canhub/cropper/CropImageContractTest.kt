package com.canhub.cropper

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.core.net.toUri
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CropImageContractTest {

    @Test
    fun testCancelledByUser() {

        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, Activity.RESULT_CANCELED, Intent())
            }
        }

        with(launchFragmentInContainer { ContractTestFragment(testRegistry) }) {
            onFragment { fragment ->
                fragment.cropImage(options())
                assert(fragment.cropResult == CropImage.CancelledResult)
            }
        }
    }

    @Test
    fun testCropSuccessWithEmptyOptions() {

        val result = CropImage.ActivityResult(
            originalUri = "content://original".toUri(),
            uriContent = "content://content".toUri(),
            error = null,
            cropPoints = floatArrayOf(),
            cropRect = Rect(1, 2, 3, 4),
            rotation = 45,
            wholeImageRect = Rect(10, 20, 0, 0),
            sampleSize = 0
        )

        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {

                val intent = Intent()
                intent.putExtra(CropImage.CROP_IMAGE_EXTRA_RESULT, result)

                dispatchResult(requestCode, CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE, intent)
            }
        }

        with(launchFragmentInContainer { ContractTestFragment(testRegistry) }) {
            onFragment { fragment ->
                fragment.cropImage(options())
                assert(fragment.cropResult == result)
            }
        }
    }

    @Test
    fun testCropWithAllOptions() {

        val options = options("file://testInput".toUri()) {
            setNoOutputImage(false)
            setInitialCropWindowRectangle(Rect(4, 5, 6, 7))
            setInitialRotation(13)
            setAllowRotation(true)
            setAllowFlipping(false)
            setAllowCounterRotation(true)
            setRotationDegrees(4)
            setFlipHorizontally(true)
            setFlipVertically(false)
            setCropMenuCropButtonTitle("Test Title")
            setCropMenuCropButtonIcon(R.drawable.ic_rotate_left_24)
        }

        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {}
        }

        with(launchFragmentInContainer { ContractTestFragment(testRegistry) }) {
            onFragment { fragment ->
                val cropImageIntent = fragment.cropImageIntent(options)

                val bundle = cropImageIntent.getBundleExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE)
                assertEquals("file://testInput".toUri(), bundle?.getParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE))
                assertEquals(options.options, bundle?.getParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS))
            }
        }
    }

    @Test
    fun testCropError() {

        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                val result = CropImage.ActivityResult(
                    originalUri = null,
                    uriContent = null,
                    error = Exception("Error!"),
                    cropPoints = floatArrayOf(),
                    cropRect = null,
                    rotation = 0,
                    wholeImageRect = null,
                    sampleSize = 0
                )
                val intent = Intent()
                intent.putExtra(CropImage.CROP_IMAGE_EXTRA_RESULT, result)

                dispatchResult(requestCode, CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE, intent)
            }
        }

        with(launchFragmentInContainer { ContractTestFragment(testRegistry) }) {
            onFragment { fragment ->
                fragment.cropImage(options())
                assertEquals(false, fragment.cropResult?.isSuccessful)
            }
        }
    }
}