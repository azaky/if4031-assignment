# Tugas 9 - MongoDB Twitter Client

Anggota Kelompok:
- 13512070 - Willy
- 13512076 - Ahmad Zaky

## Cara Menjalankan Program

Jalankan `java -jar mongodb-twitterclient.jar` yang ada pada folder `bin`. Pastikan bahwa file `config.properties` ada pada direktori yang sama.

Berikut adalah perintah yang dapat dilakukan:
- `register <username> <password>`: Meregister user baru
- `login <username> <password>`: Login dengan akun yang sudah ada
- `follow <username>`: Menjadi follower dari <username>
- `tweet <body>`: Membuat tweet dengan isi <body>
- `show tweet <username>`: Melihat tweet yang dibuat <username>
- `show timeline`: Melihat timeline sendiri (yang berisi tweet-tweet orang yang kita follow)

## File Konfigurasi

Terdapat file `config.properties` yang menyatakan konfigurasi server MongoDB. Berikut adalah isi file tersebut secara default.

    # MongoDB Servers, in the form host:port. Multiple servers can be put as comma-separated string.
    # If not present, localhost is assumed
    mongodb_servers=localhost

    # Database Name to use. If not present, willyzaky is assumed
    mongodb_name=willyzaky

Jika file tersebut tidak ada, maka kedua nilai tersebut akan dipakai secara default.

## Struktur Collections

Mirip seperti tabel pada Cassandra, terdapat 6 buah collections yang digunakan di sini.

1. Collection users

    {
        "username": <string>,
        "password": <string>
    }

2. Collection friends

    {
        "username": <string>,
        "friend": <string>,
        "since": <Date>
    }

3. Collection followers

    {
        "username": <string>,
        "follower": <string>,
        "since": <Date>
    }

4. Collection tweets

    {
        "tweet_id": <ObjectId>,
        "username": <string>,
        "body": <string>
    }

5. Collection userline

    {
        "username": <string>,
        "time": <Date>,
        "tweet_id": <ObjectId>
    }

6. Collection timeline

    {
        "username": <string>,
        "time": <Date>,
        "tweet_id": <ObjectId>
    }

## Query Database

Berikut adalah query yang digunakan untuk setiap aksi. Query dalam mongo shell.

1. Mendaftar user baru:

    db.users.insert({
        "username": <username>,
        "password": <password>
    })

2. Follow a friend:

`<followee>` adalah orang yang di-follow, dan `<username>` adalah yang mem-follow.

    var since = new Date
    db.friends.insert({
        "username": <username>,
        "friends": <followee>,
        "since": since
    })
    db.followers.insert({
        "username": <followee>,
        "follower": <username>,
        "since": since
    })

3. Tweet:

    var id = new ObjectId
    var now = new Date
    db.tweets.insert({
        "tweet_id": id,
        "username": <username>,
        "body": <body>
    })
    db.userline.insert({
        "username": <username>,
        "time": now,
        "tweet_id": id
    })
    db.followers.find({
        "username": <username>
    }).forEach(function(doc) {
        db.timeline.insert({
            "username": doc.username,
            "time": now,
            "tweet_id": id
        })
    })

4. Menampilkan tweet per user:

    db.userline.find({
        "username": <username>
    }).forEach(function(doc) {
        db.tweets.find({
            "tweet_id": doc.tweet_id
        }).forEach({
            print("@" + doc.username + ": " + doc.body);
        })
    })

5. Menampilkan timeline per user

    db.timeline.find({
        "username": <username>
    }).forEach(function(doc) {
        db.tweets.find({
            "tweet_id": doc.tweet_id
        }).forEach({
            print("@" + doc.username + ": " + doc.body);
        })
    })
