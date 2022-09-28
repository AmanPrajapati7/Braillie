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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.android.synthetic.main.fragment_text_rcognition.*
import java.util.*

class TextRcognitionFragment : Fragment() {

    private lateinit var tts: TextToSpeech

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_text_rcognition, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        speak()

        text_button.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            val bundle = data?.extras
            val image = (bundle?.get("data") as Bitmap)
            text_image.setImageBitmap(image)
            recogniseText(image)
        }
    }

    private fun recogniseText(bitmap: Bitmap) {
        text_button.isEnabled = false
        text_text.text = ""

//        val image = FirebaseVisionImage.from(bitmap)
//        val detector = FirebaseVision.getInstance()

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image).addOnSuccessListener { texts ->
            parseRecognisedText(texts)
            text_button.isEnabled = true
        }.addOnFailureListener { ex: Exception ->
            Toast.makeText(requireActivity(), "Error occurred : ${ex.message}", Toast.LENGTH_LONG)
                .show()
            text_button.isEnabled = true
        }

    }

    private fun parseRecognisedText(texts: Text) {
        for(block in texts.textBlocks) {
            for(lines in block.lines)
                text_text.append(lines.text + "\n")
        }
//        text_text.text = texts.text
        speak()

    }

    private fun speak() {
        tts = TextToSpeech(requireActivity()) {
            tts.language = Locale.UK
            tts.setSpeechRate(0.75F)
            val text = text_text.text.toString()
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, "")
        }

    }

    override fun onStop() {
        super.onStop()
        tts.stop()
        tts.shutdown()
    }

}