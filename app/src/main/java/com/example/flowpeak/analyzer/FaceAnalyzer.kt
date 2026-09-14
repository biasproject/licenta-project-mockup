package com.example.flowpeak.analyzer

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

/**
 * Class FaceAnalyzer implementează interfața ImageAnalysis.Analyzer din CameraX.
 * Analizează fiecare cadru video primit de la cameră pentru a identifica dacă utilizatorul
 * mai privește dispozitivul. Analiza rulează local cu Google ML Kit.
 *
 * @param onAttentionChanged Callback trimis către UI când fața, ochii sau poziția capului se schimbă.
 */
class FaceAnalyzer(
    private val onAttentionChanged: (Boolean) -> Unit
) : ImageAnalysis.Analyzer {

    // Configurarea opțiunilor pentru ML Kit Face Detector
    private val options = FaceDetectorOptions.Builder()
        // PERFORMANCE_MODE_FAST prioritizează viteza pentru analiza cadru-cu-cadru în timp real
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        // CLASSIFICATION_MODE_ALL activează probabilitățile pentru ochii deschiși.
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    // Inițializarea clientului de detectare facială ML Kit
    private val detector = FaceDetection.getClient(options)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image

        if (mediaImage != null) {
            // Conversia imaginii din formatul nativ CameraX (ImageProxy) în formatul InputImage acceptat de ML Kit
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            // Procesarea asincronă a imaginii pe background thread (fără a bloca Main Thread)
            detector.process(image)
                .addOnSuccessListener { faces ->
                    val face = faces.firstOrNull()
                    if (face == null) {
                        onAttentionChanged(false)
                    } else {
                        val eyesOpen = (face.leftEyeOpenProbability ?: 1f) > 0.45f &&
                            (face.rightEyeOpenProbability ?: 1f) > 0.45f
                        // ML Kit nu poate măsura direcția privirii; o întoarcere pronunțată a capului
                        // este cel mai bun indicator local că utilizatorul nu mai privește ecranul.
                        val facingScreen = kotlin.math.abs(face.headEulerAngleY) < 25f &&
                            kotlin.math.abs(face.headEulerAngleX) < 25f
                        onAttentionChanged(eyesOpen && facingScreen)
                    }
                }
                .addOnFailureListener { exception ->
                    // Tratăm eventualele erori de analiză pentru a preveni crash-ul aplicației
                    exception.printStackTrace()
                }
                .addOnCompleteListener {
                    // CRUCIAL: Apelarea imageProxy.close() la finalizarea procesării cadrului
                    // (executat indiferent dacă analiza a fost cu succes sau a eșuat).
                    // Fără acest apel, CameraX nu va mai furniza cadre noi și streaming-ul se va bloca.
                    imageProxy.close()
                }
        } else {
            // Dacă imaginea este null, eliberăm imediat cadrul
            imageProxy.close()
        }
    }
}
