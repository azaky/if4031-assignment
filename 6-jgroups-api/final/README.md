# Tugas 6 IF4031: Replicated Stack dan Set dengan Jgroups API

Anggota Kelompok:

- Willy – 13512070
- Ahmad Zaky – 13512076

## Skenario Tes

### Replicated Stack

- Create stack1, push `1` ke stack1
- Create stack2 (dalam channel yang sama), push `2` ke stack2
- Push `3` ke stack1
- Pop dari stack2 (seharusnya hasilnya = `3`)
- Pop dari stack1 (seharusnya hasilnya = `2`)
- Pop dari stack2 (seharusnya hasilnya = `1`)
- Pop dari stack1 (seharusnya error karena stack sekarang kosong)


### Replicated Set

- Create set1, add `1` ke set1
- Create set2 (dalam channel yang sama), add `2` ke set2
- Periksa apakah `2` ada di set1 (seharusnya true)
- Periksa apakah `3` ada di set1 (seharusnya false)
- Remove `2` dari set1
- Periksa apakah `2` ada di set1 (seharusnya false)