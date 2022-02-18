# Tugas Besar 1 IF2211 Strategi Algoritma
Dibuat oleh:
1. 13520051 &nbsp; Flavia Beatrix Leoni A. S.
2. 13520057 &nbsp; Marcellus Michael Herman K
3. 13520087 &nbsp; Dimas Shidqi Parikesit 

## Deskripsi
Merupakan implementasi bot mobil dalam permainan Overdrive dengan menggunakan strategi greedy.
Overdrive adalah sebuah game yang mempertandingkan 2 bot mobil dalam sebuah ajang balapan. Setiap pemain akan memiliki sebuah bot mobil dan masing-masing bot akan saling bertanding untuk memenangkan pertandingan.

## Algoritma Greedy yang Diimplementasikan
Strategi greedy yang kamu implementasikan adalah strategi yang berusaha untuk memaksimalkan kecepatan dan memilih jalur yang paling menguntungkan dalam arti mengandung obstacle seminimal mungkin atau memiliki powerups untuk diambil yang lebih menguntungkan. Pemilihan jalur ini dilakukan dengan metode pembobotan tiap jalur yang ada. Jika mobil telah berada pada jalur yang aman, mobil akan menggunakan powerups yang dimiliki dengan urutan prioritas yaitu EMP, Boost, Tweet, dan Oil.

## Requirement
* Java (minimal Java 8), dapat diunduh di https://www.oracle.com/java/technologies/downloads/#java8
* IntelIiJ IDEA, dapat diunduh di https://www.jetbrains.com/idea/
* NodeJS, dapat diunduh di https://nodejs.org/en/download/
* Starter-pack Overdrive, dapat diunduh di https://github.com/EntelectChallenge/2020-Overdrive/releases/tag/2020.3.4

## Cara Menjalankan
1. Pastikan semua requirement telah terinstall dengan baik
2. Build bot dengan menggunakan fitur build pada IntelliJ IDEA atau menuliskan command berikut pada terminal
```
mvn clean install
```
3. Pada folder target, akan muncul file bernama `...-jar-with-dependencies.jar`. Pindahkan file ini ke dalam folder starter-pack yang telah diunduh sebelumnya
4. Pastikan konfigurasi program yang terdapat pada file `game-runner-config.json` sudah benar
5. Jalankan permainan dengan menjalankan file `run.bat` atau menuliskan command berikut pada terminal
```
java -Dfile.encoding=UTF-8 -jar ./game-runner-jar-with-dependencies.jar
```
6. (Optional) Hasil permainan dapat dilihat dengan menggunakan salah satu visualizer berikut <br>
https://github.com/GoosenA/OverdriveGUI <br>
https://github.com/Affuta/overdrive-round-runner <br>
https://entelect-replay.raezor.co.za/