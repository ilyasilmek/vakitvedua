# Vakit ve Dua

Diyanet yöntemiyle namaz vakitleri, kerahat kadranı, kıble pusulası, sureler ve dualar, zikirmatik ve dini günler takvimi. 81 il ve 973 ilçe.

Tek sayfalık bir PWA'dır; derleme gerektirmez. GitHub Pages'te yayınlandığında telefona "Ana ekrana ekle" ile kendi ikonuyla kurulur ve internetsiz çalışır.

## Android

GitHub Actions her `main` gönderiminde iki dosya üretir: test için debug APK (`vakitvedua-debug-apk`) ve Play Store için AAB (`vakitvedua-release-aab`). Uygulama açılışta bir kez uygulama açılışı reklamı, ana sayfadaki "Destek ol & Reklam izle" düğmesiyle de ödüllü reklam gösterir (Google AdMob).

AAB'nin imzalı çıkması için depoya şu sırlar eklenmelidir: `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`. Sırlar yoksa AAB imzasız üretilir.

## Android

- `assets/ezan.mp3`: Bildirimde çalacak ezan kaydı. Dosya yoksa ezan kanalı telefonun varsayılan bildirim sesini kullanır. Telif hakkı sende olan ya da kullanım izni bulunan bir kayıt koy.
- `android-res/`: Derleme sırasında Android projesine kopyalanan widget, bildirim ikonu ve Java kodu.
- Gizlilik politikası: `gizlilik.html` (Play Console'da bu sayfanın GitHub Pages adresini ver).
- `app-ads.txt`: AdMob doğrulaması için alan adının köküne konmalıdır (bkz. aşağıdaki not).
