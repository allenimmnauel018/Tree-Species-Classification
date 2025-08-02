# Tree Species Classifier Android App 🌳

This project is an end-to-end mobile AI system for classifying tree species from images. It includes:

* A **TensorFlow training pipeline** (in Colab)
* A **converted TensorFlow Lite model with metadata**
* An **Android app** built using Kotlin + Jetpack Compose + TFLite

---

## 📄 Features

* Image classification using custom-trained TFLite model
* Supports **camera** and **gallery** image input
* Offline **label fetching** from model metadata
* **Confidence thresholding**
* Well-designed Android UI using **Jetpack Compose**

---

## 🎓 TensorFlow Training Pipeline (Google Colab)

Colab steps included:

1. Mount Google Drive & load dataset
2. Analyze dataset (class count, outliers, duplicates)
3. Preprocess and augment images
4. Train 3 different models:

   * `EfficientNetB0`
   * Basic CNN
   * Final optimized CNN (BatchNorm, Dropout, GlobalAvgPool)
5. Save best model as `.keras`
6. Convert to TFLite with metadata using `tflite-support`
7. Output: `model_with_metadata.tflite`, `labels.txt`

---

## 📷 Android App (Kotlin)

### Project Highlights

* Model loaded with TFLite `Interpreter`
* Metadata read via `MetadataExtractor`
* Capture image or select from gallery
* Threshold filtering (<60% confidence shows 'Unsure')

### File Structure

```
app/
 ├── src/
 │   └── main/
 │       ├── java/com/example/treeclassifier/
 │       │   ├── MainActivity.kt
 │       │   └── TreeClassifierViewModel.kt
 │       ├── assets/
 │       │   ├── model_with_metadata.tflite
 │       │   └── labels.txt
```

---

## ⚡ How to Run the Android App

1. Clone this repo and open in Android Studio
2. Place `model_with_metadata.tflite` & `labels.txt` in `assets/`
3. Connect Android device or emulator
4. Run the app and test by:

   * Capturing image
   * Picking image from gallery

---

## 🚀 Future Improvements

* Add model auto-download via Firebase
* Show top-3 predictions with confidences
* Support landscape and night mode UI

---

## 🌟 Acknowledgements

* TensorFlow Lite team
* Google Colab
* Android Jetpack

---

## 🔗 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

If you found this useful, feel free to ⭐ star the repo and contribute!

---

*Built by Allen Immanuel (2025)*
