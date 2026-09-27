# Play Console adım adım

Bu liste, Vakit ve Dua'yı Play Store'a çıkarmak için yapılacakları sırasıyla verir. Metinler ve görseller `magaza.md` dosyasında ve `ekran/` klasöründe.

## 0. Önce bilinmesi gereken

**Kapalı test zorunluluğu.** 13 Kasım 2023'ten sonra açılan kişisel geliştirici hesaplarında uygulama doğrudan yayına alınamaz. Önce en az **12 test kullanıcısının 14 gün boyunca kesintisiz** katıldığı bir kapalı test gerekir. Google 2026'dan beri test kullanıcılarının uygulamayı gerçekten kullanıp kullanmadığına da bakıyor. Bu yüzden takvim en az iki hafta sürer. Kaynak: [Play Console Yardım](https://support.google.com/googleplay/android-developer/answer/14151465?hl=tr)

Kuruluş (şirket) hesaplarında bu zorunluluk yoktur, ama kuruluş hesabı için D-U-N-S numarası gerekir.

## 1. Geliştirici hesabı (senin yapman gereken)

1. https://play.google.com/console adresinden hesap aç. Tek seferlik kayıt ücreti 25 ABD dolarıdır.
2. Kimlik doğrulamasını tamamla (kimlik belgesi ve telefon). Birkaç gün sürebilir.
3. Hesap türü: Kişisel.

## 2. İmza anahtarı (senin yapman gereken, bir kez)

Terminalde, deponun klasöründe:

```bash
bash scripts/imza-anahtari.sh
```

Betik şifreyi sorar, `~/vakitvedua-imza/upload.jks` dosyasını oluşturur ve GitHub sırlarını ekler. Ardından GitHub Actions'ın bir sonraki derlemesi imzalı AAB üretir.

**Anahtar dosyasını ve şifresini mutlaka yedekle.**

## 3. Uygulamayı oluştur

Play Console > Tüm uygulamalar > Uygulama oluştur:

| Alan | Değer |
|---|---|
| Uygulama adı | Vakit ve Dua: Namaz Vakitleri |
| Varsayılan dil | Türkçe – tr-TR |
| Uygulama mı, oyun mu | Uygulama |
| Ücretsiz mi, ücretli mi | Ücretsiz |

## 4. Uygulama içeriği (Politika > Uygulama içeriği)

| Form | Yanıt |
|---|---|
| Gizlilik politikası | https://ilyasilmek.github.io/vakitvedua/gizlilik.html |
| Uygulama erişimi | Tüm işlevler özel erişim olmadan kullanılabilir |
| Reklamlar | Evet, uygulamam reklam içeriyor |
| İçerik derecelendirmesi | Kategori: "Diğer tüm uygulama türleri". Şiddet, cinsellik, küfür, uyuşturucu, kumar sorularına **Hayır**. Kullanıcılar arası etkileşim ya da içerik paylaşımı: **Hayır** (paylaşım yalnızca telefonun paylaşım menüsüyle, uygulama içinde değil). Konum paylaşımı: **Hayır**. |
| Hedef kitle | 18 ve üzeri. "Uygulama çocukların ilgisini çekebilir mi": Hayır |
| Haber uygulaması | Hayır |
| COVID-19 izleme | Hayır |
| Veri güvenliği | Aşağıdaki 5. bölüm |
| Devlet uygulaması | Hayır |
| Finansal özellikler | Uygulamam finansal özellik sunmuyor |
| Sağlık | Uygulamam sağlık özelliği sunmuyor |
| Reklam kimliği | Evet, reklam kimliği kullanılıyor. Amaç: Reklam veya pazarlama |

İzinler hakkında soru gelirse:
- **Tam zamanlı alarm (SCHEDULE_EXACT_ALARM):** "Namaz vakti bildirimlerinin kullanıcının seçtiği vakitlerde tam zamanında gelmesi için." Bu izin Android 14'te kullanıcının kendisi tarafından açılır; ayrıca beyan gerektiren USE_EXACT_ALARM kullanılmıyor.
- **Konum:** Yalnızca ön planda, kullanıcı "Konumumu bul"a bastığında ya da seyahat modunda, en yakın ilçeyi bulmak için. Arka planda konum yok.
- **Ön plan hizmeti:** Uygulamada tür belirten ön plan hizmeti yok (listelenen tek hizmet AndroidX WorkManager'ın standart hizmeti). Form çıkmaz.

## 5. Veri güvenliği formu

**Veri toplama ve paylaşma:** Evet (AdMob nedeniyle).
**Aktarım sırasında şifreleme:** Evet.
**Kullanıcılar verilerinin silinmesini isteyebilir mi:** Hayır. Uygulama hesap açtırmaz ve sunucuda veri tutmaz; kayıtların hepsi telefondadır ve uygulama silinince gider. Hesap oluşturma olmadığı için silme bağlantısı zorunlu değildir.

Toplanan veri türleri (hepsi AdMob SDK'sından):

| Veri türü | Toplanıyor | Paylaşılıyor | Amaç |
|---|---|---|---|
| Konum > Yaklaşık konum (IP'den) | Evet | Evet | Reklam veya pazarlama |
| Uygulama etkinliği > Uygulama etkileşimleri | Evet | Evet | Reklam veya pazarlama, Analiz |
| Uygulama bilgileri ve performansı > Kilitlenme günlükleri, Teşhis | Evet | Evet | Analiz |
| Cihaz veya diğer kimlikler | Evet | Evet | Reklam veya pazarlama, Dolandırıcılığı önleme |

Her satır için: işleme **zorunlu** (kullanıcı kapatamaz), veriler **geçici değil**.

Uygulamanın kendisi hiçbir veri göndermez. Kesin konum, en yakın ilçeyi bulmak için yalnızca telefonda kullanılır ve cihazdan çıkmaz; Google'ın tanımına göre bu "toplama" sayılmaz, formda işaretlenmez.

AdMob'un güncel listesi: https://developers.google.com/admob/android/privacy/play-data-disclosure

## 6. Ana mağaza girişi

`magaza.md` dosyasındaki kısa ve tam açıklamayı yapıştır. Görseller:

| Alan | Dosya |
|---|---|
| Uygulama simgesi (512×512) | `icon-512.png` |
| Öne çıkan grafik (1024×500) | `store/tanitim-gorseli.png` |
| Telefon ekran görüntüleri (en az 2, en çok 8) | `store/ekran/1-vakitler.png` … `8-ayarlar.png` |

Kategori: Yaşam tarzı. İletişim e-postası: ilyasilmk@gmail.com.

## 7. İlk sürüm: kapalı test

1. **Sürüm dosyası:** Deponun GitHub sayfasında `v1.54.01` etiketi açıldığında GitHub Actions imzalı AAB'yi bir sürüme ekler. Terminalden:
   ```bash
   git tag v1.54.01 && git push origin v1.54.01
   ```
   Dosya: GitHub > Releases > "Vakit ve Dua 1.54.01" > `vakitvedua-1.54.01.aab`.
2. Play Console > Test etme > Kapalı test > Kanal oluştur (ör. "Test").
3. **Test kullanıcıları:** En az 12 kişinin Google hesabı e-postasını bir e-posta listesine ekle. Birkaç yedek kişi eklemek iyi olur; biri ayrılırsa 14 günlük süre başa dönebilir.
4. **Uygulama imzalama:** İlk yüklemede Play App Signing'i kabul et. Yüklediğin anahtar "yükleme anahtarı" olur; asıl imza anahtarını Google saklar.
5. AAB'yi yükle, sürüm notlarını `magaza.md`'den yapıştır, incelemeye gönder.
6. İnceleme onaylanınca test bağlantısını test kullanıcılarına gönder. Her biri bağlantıdan katılıp uygulamayı Play Store'dan kurmalı ve birkaç gün kullanmalı.

## 8. Üretim erişimi

14 gün dolunca Kontrol Paneli'nde "Üretim erişimine başvur" açılır. Sorular testin nasıl geçtiğini sorar; kısa ve dürüst yanıtlar ver, örneğin:

- Test kullanıcılarını nasıl buldun: Aile, arkadaşlar ve cami çevresinden gönüllüler.
- Hangi geri bildirimleri aldın ve ne değiştirdin: Testte gelen gerçek geri bildirimleri yaz.
- Uygulamanın hedef kitlesi: Türkiye'de namaz vakitlerini, kıbleyi ve duaları takip etmek isteyen Müslümanlar.

Onaydan sonra aynı AAB'yi üretim kanalına tanıt.

## 9. Yayından sonra

- **AdMob:** AdMob > Uygulamalar > Vakit ve Dua > Uygulama ayarları > "Mağazaya bağla" ile Play Store girişine bağla.
- **app-ads.txt:** Play Console'daki geliştirici web sitesi alanına yazdığın alan adının kök dizininde `app-ads.txt` bulunmalı (örneğin `https://ilyasilmek.github.io/app-ads.txt`). AdMob bu dosyayı birkaç gün içinde tarar.
- **Her yeni sürüm:** `package.json`'daki sürümü yükselt, `v<sürüm>` etiketini gönder, oluşan AAB'yi yükle.
