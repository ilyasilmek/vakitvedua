#!/bin/bash
# Play Store için yükleme anahtarını oluşturur ve GitHub Actions sırlarına ekler.
# Şifreyi sen yazarsın; betik onu hiçbir dosyaya kaydetmez.
# Kullanım: bash scripts/imza-anahtari.sh
set -euo pipefail
REPO=ilyasilmek/vakitvedua
DIR="$HOME/vakitvedua-imza"
KS="$DIR/upload.jks"
ALIAS=upload

command -v gh >/dev/null || { echo "GitHub CLI (gh) gerekli: https://cli.github.com"; exit 1; }
command -v keytool >/dev/null || { echo "keytool bulunamadı; bir Java (JDK) kurulu olmalı."; exit 1; }
gh auth status >/dev/null 2>&1 || { echo "Önce 'gh auth login' ile GitHub'a giriş yap."; exit 1; }

read -rsp "Anahtar şifresi (en az 6 karakter): " KS_PASS; echo
[ ${#KS_PASS} -ge 6 ] || { echo "Şifre en az 6 karakter olmalı."; exit 1; }
export KS_PASS

if [ -e "$KS" ]; then
  echo "$KS zaten var, yeni anahtar oluşturulmadı. Girdiğin şifreyle doğrulanıyor..."
  keytool -list -keystore "$KS" -storepass:env KS_PASS >/dev/null || { echo "Şifre bu anahtarla eşleşmiyor."; exit 1; }
else
  read -rsp "Şifre tekrar: " P2; echo
  [ "$KS_PASS" = "$P2" ] || { echo "Şifreler eşleşmedi."; exit 1; }
  mkdir -p "$DIR"; chmod 700 "$DIR"
  keytool -genkeypair -keystore "$KS" -alias "$ALIAS" -keyalg RSA -keysize 2048 -validity 10000 \
    -storepass:env KS_PASS -keypass:env KS_PASS -dname "CN=Vakit ve Dua, C=TR"
  chmod 600 "$KS"
  echo "Anahtar oluşturuldu: $KS"
fi

echo "GitHub sırları ekleniyor..."
base64 -i "$KS" | gh secret set ANDROID_KEYSTORE_BASE64 -R "$REPO"
printf %s "$KS_PASS" | gh secret set ANDROID_KEYSTORE_PASSWORD -R "$REPO"
gh secret set ANDROID_KEY_ALIAS -R "$REPO" -b "$ALIAS"

echo
echo "Yükleme anahtarının SHA-256 parmak izi (Play Console'da görünecek olanla aynı olmalı):"
keytool -list -v -keystore "$KS" -alias "$ALIAS" -storepass:env KS_PASS | grep -E "SHA256" || true
echo
echo "ÖNEMLİ: $KS dosyasını ve şifresini kaybetme."
echo "Bir kopyasını şifre yöneticine ya da başka bir diske yedekle. Kaybedersen Play Console'dan yeni yükleme anahtarı istemen gerekir."
