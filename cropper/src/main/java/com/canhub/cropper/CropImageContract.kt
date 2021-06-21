package com.canhub.cropper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.canhub.cropper.CropImage.getActivityResult

class CropImageContract :
    ActivityResultContract<CropImageContractOptions, CropImageView.CropResult>() {

    override fun createIntent(context: Context, input: CropImageContractOptions): Intent {
        input.options.validate()
        return Intent().apply {
            val bundle = Bundle()
            bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE, input.uri)
            bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS, input.options)
            putExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE, bundle)
        }
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): CropImageView.CropResult {
        val result = getActivityResult(intent)
        return if (result == null || resultCode == Activity.RESULT_CANCELED) {
            CropImage.CancelledResult
        } else {
            result
        }
    }
}
