package com.example.braillie

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions
import com.google.firebase.ml.vision.objects.FirebaseVisionObject
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import kotlinx.android.synthetic.main.fragment_image_recognition.*
import kotlinx.android.synthetic.main.fragment_text_rcognition.*
import java.util.*


class ImageRecognitionFragment : Fragment() {

    private lateinit var tts: TextToSpeech

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_recognition, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        speak()

        image_button.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            val bundle = data?.extras
            val image = (bundle?.get("data") as Bitmap)
            image_image.setImageBitmap(image)
            recogniseObjects(image)
        }
    }

    private fun recogniseObjects(bitmap: Bitmap) {
        image_button.isEnabled = false
        image_text.text = ""

        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val options = FirebaseVisionOnDeviceImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.7f)
            .build()
        val labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler(options)

        labeler.processImage(image).addOnSuccessListener { labels ->
            parseRecogniseLabels(labels)
            image_button.isEnabled = true
        }.addOnFailureListener { ex: Exception ->
            Toast.makeText(requireActivity(), "Error occurred: ${ex.message}", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun parseRecogniseLabels(labels: List<FirebaseVisionImageLabel>) {
        for (label in labels) {
            val text = label.text
            image_text.append(text + "\n")
        }
    }

    private fun speak() {
        tts = TextToSpeech(requireActivity()) {
            tts.language = Locale.UK
            tts.setSpeechRate(0.75F)
            val text = image_text.text.toString()
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, "")
        }

    }

    override fun onStop() {
        super.onStop()
        tts.stop()
        tts.shutdown()
    }

}