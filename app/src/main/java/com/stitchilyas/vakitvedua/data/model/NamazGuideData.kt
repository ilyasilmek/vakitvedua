package com.stitchilyas.vakitvedua.data.model

data class NamazSectionGuide(
    val sectionTitle: String,
    val rekatCount: String,
    val steps: List<Pair<String, String>> // Step Title -> Step Description
)

data class NamazDetail(
    val name: String,
    val totalRekat: String,
    val rekatSummary: String,
    val description: String,
    val sections: List<NamazSectionGuide>
)

object NamazGuideData {
    val PRAYERS = listOf(
        NamazDetail(
            name = "Sabah Namazı",
            totalRekat = "4 Rekât",
            rekatSummary = "2 Rekât Sünnet + 2 Rekât Farz",
            description = "Sabah namazı imsak vaktinin girmesiyle başlar, güneş doğana kadar kılınabilir. Önce 2 rekât sünnet, ardından 2 rekât farz kılınır.",
            sections = listOf(
                NamazSectionGuide(
                    sectionTitle = "2 Rekât Sünnetin Kılınışı",
                    rekatCount = "2 Rekât",
                    steps = listOf(
                        "Niyet" to "Niyet ettim Allah rızası için bugünkü sabah namazının iki rekât sünnetini kılmaya.",
                        "1. Rekât - İftitah Tekbiri & Kıyam" to "Eller kulak hizasına kaldırılarak 'Allâhu ekber' denir ve eller göbek altında (kadınlar göğüs üstünde) bağlanır.\n• Sübhâneke okunur.\n• Eûzü-Besmele çekilir.\n• Fâtiha suresi okunur.\n• Bir zamm-ı sure (örneğin Fil veya Kâfirûn suresi) okunur.",
                        "1. Rekât - Rükû & Secdeler" to "• 'Allâhu ekber' diyerek rükûya eğilinir. 3 defa 'Sübhâne rabbiye'l-azîm' denir.\n• 'Semiallâhu limen hamideh' denilerek doğrulunur ve 'Rabbenâ leke'l-hamd' denir.\n• 'Allâhu ekber' diyerek secdeye varılır. 3 defa 'Sübhâne rabbiye'l-a'lâ' denir.\n• 'Allâhu ekber' diyerek oturulur, ardından ikinci secdeye varılır ve 3 defa 'Sübhâne rabbiye'l-a'lâ' denir.\n• 'Allâhu ekber' diyerek 2. rekâta kalkılır.",
                        "2. Rekât - Kıyam & Kıraat" to "• Besmele çekilir.\n• Fâtiha suresi okunur.\n• Bir zamm-ı sure (örneğin İhlâs veya Felak suresi) okunur.\n• Rükû ve secdeler 1. rekâttaki gibi tamamlanır.",
                        "Son Oturuş (Ka'de-i Ahîre) & Selâm" to "İkinci secdeden sonra oturulur:\n• Ettehiyyâtü okunur.\n• Allâhümme Salli ve Allâhümme Bârik okunur.\n• Rabbenâ âtinâ ve Rabbenâğfirlî okunur.\n• Önce sağa 'Es-selâmü aleyküm ve rahmetullâh', sonra sola 'Es-selâmü aleyküm ve rahmetullâh' denilerek selâm verilir."
                    )
                ),
                NamazSectionGuide(
                    sectionTitle = "2 Rekât Farzın Kılınışı",
                    rekatCount = "2 Rekât",
                    steps = listOf(
                        "Kamet & Niyet" to "Erkekler farza başlamadan önce kamet getirir. 'Niyet ettim Allah rızası için bugünkü sabah namazının iki rekât farzını kılmaya.'",
                        "1. Rekât" to "• İftitah tekbiri alınır ('Allâhu ekber').\n• Sübhâneke, Eûzü-Besmele, Fâtiha ve bir zamm-ı sure okunur.\n• Rükû (3 defa 'Sübhâne rabbiye'l-azîm') ve iki secde (3'er defa 'Sübhâne rabbiye'l-a'lâ') yapılır.",
                        "2. Rekât" to "• Ayağa kalkılır, Besmele, Fâtiha ve zamm-ı sure okunur.\n• Rükû ve secdeler yapılır.",
                        "Son Oturuş & Selâm" to "• Oturulur: Ettehiyyâtü, Salli-Bârik ve Rabbenâ duaları okunur.\n• Sağa ve sola selâm verilerek namaz tamamlanır."
                    )
                )
            )
        ),
        NamazDetail(
            name = "Öğle Namazı",
            totalRekat = "10 Rekât",
            rekatSummary = "4 İlk Sünnet + 4 Farz + 2 Son Sünnet",
            description = "Öğle namazı toplam 10 rekâttır. Sırasıyla 4 rekât ilk sünnet, 4 rekât farz ve 2 rekât son sünnet kılınır.",
            sections = listOf(
                NamazSectionGuide(
                    sectionTitle = "4 Rekât İlk Sünnet",
                    rekatCount = "4 Rekât",
                    steps = listOf(
                        "Niyet & 1. Rekât" to "'Niyet ettim Allah rızası için bugünkü öğle namazının ilk sünnetini kılmaya.'\n• Tekbir, Sübhâneke, Eûzü-Besmele, Fâtiha ve zamm-ı sure okunur, rükû ve secdeler yapılır.",
                        "2. Rekât & İlk Oturuş" to "• 2. rekâta kalkılır, Besmele, Fâtiha ve zamm-ı sure okunur, rükû ve secdeler yapılır.\n• İlk oturuşta YALNIZCA Ettehiyyâtü okunur ve 3. rekâta kalkılır.",
                        "3. ve 4. Rekât" to "• 3. rekâtta Besmele, Fâtiha ve zamm-ı sure okunur, rükû ve secdeler yapılır.\n• 4. rekâtta Besmele, Fâtiha ve zamm-ı sure okunur, rükû ve secdeler yapılır.",
                        "Son Oturuş & Selâm" to "Oturulur: Ettehiyyâtü, Salli-Bârik ve Rabbenâ duaları okunur, sağa ve sola selâm verilir."
                    )
                ),
                NamazSectionGuide(
                    sectionTitle = "4 Rekât Farzın Kılınışı",
                    rekatCount = "4 Rekât",
                    steps = listOf(
                        "Niyet & İlk 2 Rekât" to "Kamet getirilir. 'Niyet ettim Allah rızası için bugünkü öğle namazının dört rekât farzını kılmaya.'\n• 1. ve 2. rekâtlar sünnetteki gibi Fâtiha + zamm-ı sure ile kılınır.\n• 2. rekât sonunda oturulup SADECE Ettehiyyâtü okunur ve 3. rekâta kalkılır.",
                        "ÖNEMLİ: 3. ve 4. Rekâtta Zamm-ı Sure Okunmaz" to "Farz namazların 3. ve 4. rekâtlarında YALNIZCA FÂTİHA okunur, zamm-ı sure okunmaz!\n• 3. rekât: Besmele ve Fâtiha okunup rükû ve secdelere gidilir.\n• 4. rekât: Besmele ve Fâtiha okunup rükû ve secdelere gidilir.",
                        "Son Oturuş & Selâm" to "Son oturuşta Ettehiyyâtü, Salli-Bârik, Rabbenâ okunarak selâm verilir."
                    )
                ),
                NamazSectionGuide(
                    sectionTitle = "2 Rekât Son Sünnet",
                    rekatCount = "2 Rekât",
                    steps = listOf(
                        "Kılınışı" to "Sabah namazının sünneti gibi 2 rekât olarak kılınır. Her iki rekâtta da Fâtiha ve zamm-ı sure okunur, son oturuşta selâm verilir."
                    )
                )
            )
        ),
        NamazDetail(
            name = "İkindi Namazı",
            totalRekat = "8 Rekât",
            rekatSummary = "4 Sünnet + 4 Farz",
            description = "İkindi namazı 4 rekât sünnet ve 4 rekât farz olmak üzere toplam 8 rekâttır. İkindi sünneti 'gayr-i müekkede' sünnettir.",
            sections = listOf(
                NamazSectionGuide(
                    sectionTitle = "4 Rekât Sünnetin Kılınışı",
                    rekatCount = "4 Rekât (Gayr-i Müekkede)",
                    steps = listOf(
                        "Niyet & İlk 2 Rekât" to "'Niyet ettim Allah rızası için ikindi namazının dört rekât sünnetini kılmaya.'\n• 1. ve 2. rekât Fâtiha ve zamm-ı sure ile kılınır.",
                        "ÖNEMLİ: İlk Oturuş Farkı" to "İkindi sünnetinin ilk oturuşunda Ettehiyyâtü'den sonra SALLİ ve BÂRİK duaları da okunur!",
                        "ÖNEMLİ: 3. Rekâta Kalkış Farkı" to "3. rekâta kalkıldığında kıyama durulunca doğrudan Besmele değil, önce SÜBHÂNEKE okunur, ardından Eûzü-Besmele, Fâtiha ve zamm-ı sure okunur.",
                        "4. Rekât ve Selâm" to "4. rekâtta Besmele, Fâtiha ve zamm-ı sure okunur. Rükû ve secdelerden sonra son oturuşta Ettehiyyâtü, Salli-Bârik ve Rabbenâ okunup selâm verilir."
                    )
                ),
                NamazSectionGuide(
                    sectionTitle = "4 Rekât Farzın Kılınışı",
                    rekatCount = "4 Rekât",
                    steps = listOf(
                        "Kılınışı" to "Tıpkı öğle namazının farzı gibi kılınır. İlk iki rekâtta Fâtiha + zamm-ı sure, ilk oturuşta sadece Ettehiyyâtü; 3. ve 4. rekâtlarda ise YALNIZCA FÂTİHA okunur."
                    )
                )
            )
        ),
        NamazDetail(
            name = "Akşam Namazı",
            totalRekat = "5 Rekât",
            rekatSummary = "3 Farz + 2 Sünnet",
            description = "Akşam namazı toplam 5 rekâttır. Diğer vakitlerin aksine ÖNCE 3 rekât farz, ardından 2 rekât sünnet kılınır.",
            sections = listOf(
                NamazSectionGuide(
                    sectionTitle = "3 Rekât Farzın Kılınışı",
                    rekatCount = "3 Rekât",
                    steps = listOf(
                        "Niyet & İlk 2 Rekât" to "Kamet getirilir. 'Niyet ettim Allah rızası için bugünkü akşam namazının üç rekât farzını kılmaya.'\n• 1. ve 2. rekât Fâtiha ve zamm-ı sure ile kılınır.\n• 2. rekât sonunda oturulup SADECE Ettehiyyâtü okunur ve 3. rekâta kalkılır.",
                        "3. Rekât & Selâm" to "• 3. rekâtta sadece Besmele ve FÂTİHA okunur (zamm-ı sure okunmaz).\n• Rükû ve secdeler yapılır.\n• Son oturuşta Ettehiyyâtü, Salli-Bârik ve Rabbenâ okunarak selâm verilir."
                    )
                ),
                NamazSectionGuide(
                    sectionTitle = "2 Rekât Sünnetin Kılınışı",
                    rekatCount = "2 Rekât",
                    steps = listOf(
                        "Kılınışı" to "Sabah namazının sünneti gibi kılınır. Her iki rekâtta Fâtiha ve zamm-ı sure okunur, oturuşta selâm verilir."
                    )
                )
            )
        ),
        NamazDetail(
            name = "Yatsı Namazı",
            totalRekat = "10 Rekât (Vitir ile 13)",
            rekatSummary = "4 İlk Sünnet + 4 Farz + 2 Son Sünnet (+ 3 Vitir)",
            description = "Yatsı namazı 4 ilk sünnet, 4 farz ve 2 son sünnet olmak üzere 10 rekâttır. Ardından 3 rekât Vitir vacip namazı kılınır.",
            sections = listOf(
                NamazSectionGuide(
                    sectionTitle = "4 Rekât İlk Sünnet",
                    rekatCount = "4 Rekât (Gayr-i Müekkede)",
                    steps = listOf(
                        "Kılınışı" to "Tıpkı ikindi namazının 4 rekât sünneti gibi kılınır (İlk oturuşta Salli-Bârik okunur, 3. rekâta kalkınca Sübhâneke ile başlanır)."
                    )
                ),
                NamazSectionGuide(
                    sectionTitle = "4 Rekât Farzın Kılınışı",
                    rekatCount = "4 Rekât",
                    steps = listOf(
                        "Kılınışı" to "Öğle ve ikindi namazlarının farzı gibi kılınır. 1. ve 2. rekâtta Fâtiha + zamm-ı sure, ilk oturuşta sadece Ettehiyyâtü; 3. ve 4. rekâtlarda yalnızca Fâtiha okunur."
                    )
                ),
                NamazSectionGuide(
                    sectionTitle = "2 Rekât Son Sünnet",
                    rekatCount = "2 Rekât",
                    steps = listOf(
                        "Kılınışı" to "Sabah namazının sünneti gibi 2 rekât olarak kılınır."
                    )
                )
            )
        ),
        NamazDetail(
            name = "Vitir Namazı",
            totalRekat = "3 Rekât Vacip",
            rekatSummary = "3 Rekât Vacip (Kunut Duaları ile)",
            description = "Vitir namazı yatsı namazından sonra kılınan 3 rekâtlık vacip bir namazdır. 3. rekâtında Kunut tekbiri alınır ve Kunut duaları okunur.",
            sections = listOf(
                NamazSectionGuide(
                    sectionTitle = "Vitir Namazının Kılınışı",
                    rekatCount = "3 Rekât",
                    steps = listOf(
                        "Niyet & İlk 2 Rekât" to "'Niyet ettim Allah rızası için bugünkü vitir namazını kılmaya.'\n• 1. rekât: Sübhâneke, Eûzü-Besmele, Fâtiha ve zamm-ı sure, rükû ve secdeler.\n• 2. rekât: Besmele, Fâtiha ve zamm-ı sure, rükû ve secdeler.\n• İlk oturuşta SADECE Ettehiyyâtü okunur ve 3. rekâta kalkılır.",
                        "ÖNEMLİ: 3. Rekât & Kunut Tekbiri" to "• 3. rekâta kalkılınca Besmele, Fâtiha ve bir zamm-ı sure (örneğin İhlâs suresi) okunur.\n• Rükûya gitmeden, ayaktayken eller kulak hizasına kaldırılarak 'Allâhu ekber' denir (Kunut Tekbiri) ve eller tekrar bağlanır!\n• Kunut duaları okunur: 'Allâhümme innâ neste'înüke...' ve 'Allâhümme iyyâke na'büdü...'\n• Kunut dualarını bilmeyenler 'Rabbenâ âtinâ...' duasını okuyabilir.",
                        "Rükû, Secdeler & Selâm" to "• 'Allâhu ekber' denilerek rükûya gidilir ve secdeler tamamlanır.\n• Son oturuşta Ettehiyyâtü, Salli-Bârik ve Rabbenâ okunarak selâm verilir."
                    )
                )
            )
        ),
        NamazDetail(
            name = "Cuma Namazı",
            totalRekat = "10 Rekât",
            rekatSummary = "4 İlk Sünnet + 2 Cemaatle Farz + 4 Son Sünnet",
            description = "Cuma namazı, Cuma günü öğle vaktinde cemaatle camide kılınır. Farzından önce hutbe irad edilir.",
            sections = listOf(
                NamazSectionGuide(
                    sectionTitle = "Cuma Namazının Aşamaları",
                    rekatCount = "10 Rekât",
                    steps = listOf(
                        "1. Dört Rekât İlk Sünnet" to "Öğle namazının ilk sünneti gibi kılınır.",
                        "2. Hutbe & Kamet" to "İmam minbere çıkarak Cuma hutbesini okur. Hutbe dinlendikten sonra müezzin kamet getirir.",
                        "3. İki Rekât Farz (İmamla Beraber)" to "İmama uyulur: 'Niyet ettim Allah rızası için Cuma namazının iki rekât farzını kılmaya, uydum hazır olan imama.'\n• İmam kıraati sesli yapar, cemaat Sübhâneke okuyup dinler.\n• Rükû, secdeler ve 2. rekât imamla tamamlanıp selâm verilir.",
                        "4. Dört Rekât Son Sünnet" to "Öğle namazının ilk sünneti gibi 4 rekât olarak kılınır."
                    )
                )
            )
        )
    )
}
