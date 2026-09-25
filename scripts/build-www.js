// Web dosyalarını Capacitor'ın paketleyeceği www klasörüne kopyalar.
const fs = require('fs');
const files = ['index.html', 'manifest.webmanifest', 'icon.svg', 'icon-192.png', 'icon-512.png'];
fs.rmSync('www', { recursive: true, force: true });
fs.mkdirSync('www');
for (const f of files) fs.copyFileSync(f, 'www/' + f);
console.log('www hazır:', files.join(', '));
