# Vakit ve Dua: Namaz Vakitleri & İslami Rehber

**Vakit ve Dua**, Müslümanların günlük ibadetlerini kolaylaştırmak amacıyla geliştirilmiş modern, sade, hızlı ve çevrimdışı çalışabilen kapsamlı bir Android uygulamasıdır. **Kotlin** ve **Jetpack Compose** kullanılarak modern Android mimari standartlarına uygun olarak geliştirilmiştir.

---

## 🌟 Öne Çıkan Özellikler

### 🕌 Namaz Vakitleri & Gökyüzü Görünümü
- **Çevrimdışı Hesaplama:** Diyanet İşleri Başkanlığı uyumlu astronomik namaz vakti hesaplama motoru (İmsak, Güneş, Öğle, İkindi, Akşam, Yatsı).
- **81 İl ve 973 İlçe Desteği:** Türkiye'nin tüm il ve ilçeleri ile dünya genelindeki şehirler için internetsiz vakit hesaplama.
- **Konum Tespiti:** GPS ve konum servisleri ile en yakın il/ilçeyi otomatik algılama.
- **Geri Sayım & Dinamik Kart:** Bir sonraki vakte kalan süreyi canlı gösteren sayaç ve günün vaktine göre renk değiştiren dinamik gökyüzü kartı.
- **24 Saatlik Kerahat Kadranı:** Güneş doğuşu, istiva (öğle öncesi) ve güneş batışı kerahat vakitlerini görsel dairesel kadran üzerinde takip edebilme.

### 🧭 Gelişmiş Kıble Pusulası
- **Gerçek Zamanlı Pusula:** Sensör filtrelemeli yüksek hassasiyetli Kıble yönü göstergesi.
- **Titreşimli Geribildirim:** Kıble açısına tam hizalandığında titreşim uyarısı.
- **Güneş Konumu ile Yön Tayini:** Sensörlerin yetersiz kaldığı durumlarda Güneş'in açısına göre alternatif kıble yönü bulma rehberi.
- **Mesafe ve Detaylar:** Kâbe'ye olan kuş uçuşu mesafe, pusula derecesi ve yön göstergeleri.

### 📖 Kur'an-ı Kerim & Sesli Dinleme
- **Tanzil Projesi Metinleri:** 114 surenin tamamını içeren eksiksiz Arapça metin ve Türkçe mealler.
- **Sesli Dinleme:** Abdüssamed (Basit) kıraatı ile sureleri çevrim içi akış üzerinden dinleyebilme.
- **Yer İmi & Arama:** Son kalınan ayeti/sureyi tek dokunuşla kaydetme ve hızlı sure arama.
- **Paylaşım & Özelleştirme:** Ayetleri kopyalama, paylaşma ve ayarlanabilir Arapça/Türkçe yazı boyutu.

### 📿 Zikirmatik
- **İnteraktif Zikir Sayacı:** Halka animasyonlu geniş basma alanı ve hedef tamamlanma halkası.
- **Hazır & Özel Zikirler:** Subhanallah, Elhamdülillah, Allahuekber gibi hazır zikirler veya kendi özel zikirlerinizi ekleme/düzenleme.
- **Hedef ve Titreşim:** 33, 99, 100, 1000 ve özel hedef seçenekleri, her dokunuşta ve hedefte titreşimli geribildirim.

### 📊 Namaz Takibi & Kaza Sayacı
- **Haftalık Takip:** Son 7 günün 5 vakit namazını eda durumuna göre işaretleme ve istatistik takibi.
- **Kaza Sayacı:** Sabah, Öğle, İkindi, Akşam, Yatsı, Vitir ve Oruç borçları için kolay kaza sayacı artırma/azaltma ve hızlı kurulum sihirbazı.

### 🤲 Dualar, Esmâü'l-Hüsnâ & İbadet Rehberi
- **Dualar & Sureler:** Namaz duaları, günlük dualar, Arapça okunuş, Türkçe meal ve arama özelliği.
- **Esmâü'l-Hüsnâ:** Allah'ın 99 ismi, anlamları ve zikir erdemleri.
- **Görsel Rehber:** Adım adım görseller ve açıklamalarla Abdest ve Namaz kılınışı rehberleri.

### 💌 Cuma & Bayram Mesajları (Görsel Kart Oluşturucu)
- **Hazır Tebrik Mesajları:** Cuma, Kandil, Ramazan ve Bayram günleri için hazır yazılı mesajlar.
- **Görsel Paylaşım Kartı (Card Generator):** Mesajları özelleştirilebilir İslami desenli arka planlar, renk gradyanları ve şık tipografi ile resim formatında sosyal medyada ve WhatsApp'ta paylaşabilme.

### 📅 Takvim & İmsakiye
- **30 Günlük İmsakiye:** Seçilen şehir için 1 aylık namaz vakitleri tablosu.
- **Dini Günler & Geri Sayım:** Ramazan, Kadir Gecesi, Kurban Bayramı ve Kandiller gibi tüm dini günlerin tarihi ve geri sayım sayacı.

### 📲 Ana Ekran Widget'ı & Bildirimler
- **Ana Ekran Widget'ları:** Küçük ve büyük boyutta mevcut vakti, kalan süreyi ve günlük vakit çizelgesini gösteren uygulama widget'ları.
- **Vakit Bildirimleri & Ezan Sesi:** Ezan sesi uyarı seçeneği, vakit öncesi hatırlatıcılar ve kerahat vakti bildirimleri.

---

## 🛠️ Mimari ve Teknolojik Yapı

| Bileşen | Teknoloji / Kütüphane |
| :--- | :--- |
| **Dil** | Kotlin (100%) |
| **Arayüz (UI)** | Jetpack Compose, Material 3, Compose Navigation |
| **Mimari** | Clean Architecture ilkeleri, StateFlow, ViewModel |
| **Proje Yapısı** | Gradle Kotlin DSL (`build.gradle.kts`), Version Catalog (`libs.versions.toml`) |
| **Tasarım / Tema** | Material You dinamik temalama, Koyu/Açık Tema, Edge-to-Edge |
| **Bileşenler** | App Widgets (AppWidgetProvider), Android Notification System |
| **SDK Sürümleri** | `minSdk = 26` (Android 8.0), `targetSdk = 36` (Android 15+) |

---

## 🚀 Projeyi Derleme ve Çalıştırma

1. Projeyi klonlayın:
   ```bash
   git clone https://github.com/stitchilyas/vakitvedua.git
   ```
2. Android Studio ile projeyi açın.
3. Gradle senkronizasyonunun tamamlanmasını bekleyin.
4. `app` modülünü seçerek simülatör veya gerçek cihazda çalıştırın:
   ```bash
   ./gradlew app:assembleDebug
   ```

---

## 📄 Lisans

Bu proje kişisel/açık kaynak geliştirme projesidir. Tüm hakları saklıdır.
