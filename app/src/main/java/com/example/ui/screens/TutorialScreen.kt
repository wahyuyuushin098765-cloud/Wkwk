package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PANDUAN STRATEGI & TAKTIK",
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("tutorial_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = GoldPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF14170D))
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TutorialCard(
                title = "1. FORMASI MILITER NUSANTARA",
                content = "• Globus (o): Barisan lingkaran padat konsentris. Jika Jenderal diatur di posisi 'Tengah', slot tengah dikosongkan agar Jenderal terlindungi aman 360 derajat oleh prajurit sekutu.\n" +
                        "• Acies Simplex (-): Barisan tempur satu garis lurus memanjang. Tekan tombol '-' untuk beralih antara orientasi Horizontal dan Vertikal.\n" +
                        "• Acies Duplex (=): Barisan tempur dua lapis beriringan. Baris depan menahan benturan dan baris kedua siap menggantikan.\n" +
                        "• V-Shape (v): Formasi ujung panah taji. Tekan 'v' untuk memutar arah ujung panah ke Kanan, Bawah, Kiri, atau Atas."
            )

            TutorialCard(
                title = "2. SERANGAN BELAKANG & MATIKAN INSTAN (BACKSTAB)",
                content = "• Badan setiap bidak terbagi dua: setengah depan (berpanah) dan setengah belakang.\n" +
                        "• Bila seekor musuh ditebas secara bersamaan dari DEPAN dan BELAKANG (Flanking Clamp), musuh tersebut MATI SEKETIKA (Instant Kill)!\n" +
                        "• Pasukan penyerang otomatis melakukan manuver orbit mengitari musuh ke zona belakang jika slot depan sudah penuh."
            )

            TutorialCard(
                title = "3. TEKANAN DEPAN & MUNDUR TAKTIS",
                content = "• Ketika musuh ditekan oleh 2 atau lebih prajurit dari arah depan, musuh akan terdorong mundur untuk mempertahankan diri.\n" +
                        "• Saat musuh mundur, flanker belakang memiliki kesempatan emas untuk menghabisi target dari belakang."
            )

            TutorialCard(
                title = "4. MODE SERANG VS GOTO",
                content = "• Mode Serang: Pasukan otomatis mendeteksi musuh terdekat dan menyerang atau melakukan manuver flanking di sepanjang jalan.\n" +
                        "• Mode GoTo: Pasukan berbaris disiplin menuju titik tujuan tanpa berbelok, namun tetap menebas musuh yang menghalangi jalur lintasan mereka."
            )

            TutorialCard(
                title = "5. JENDERAL & KONVERSI PASUKAN",
                content = "• Jenderal memiliki HP tinggi (400 untuk pemain). Tombol 'Jendral' mengatur posisinya: Tengah, Kiri, Kanan, Atas, atau Bawah barisan.\n" +
                        "• Jika Jenderal wilayah musuh gugur, seluruh prajurit wilayah tersebut akan bertekuk lutut dan bergabung menjadi sekutu Anda!"
            )

            TutorialCard(
                title = "6. MEDAN PERANG & KONDISI WILAYAH",
                content = "• Air / Laut: Prajurit yang berenang di perairan akan bergerak lebih lambat (40% kecepatan normal).\n" +
                        "• Lumpur Coklat: Melambatkan laju gerak pasukan hingga 55%.\n" +
                        "• Api Kobaran: Membakar dan mengurangi HP prajurit yang melewatinya.\n" +
                        "• Air Suci / Tirta: Menyembuhkan dan memulihkan HP prajurit yang terluka."
            )

            TutorialCard(
                title = "7. MULTIPLAYER ONLINE & SINYAL TAKTIS",
                content = "• Buka menu Multiplayer untuk membuat Ruang Tempur atau bergabung dengan kode ruang 6 digit.\n" +
                        "• Gunakan panel Obrolan di pojok kanan untuk koordinasi live, serta tombol Panggilan Taktis Cepat untuk memberi komando instan kepada sekutu."
            )
        }
    }
}

@Composable
private fun TutorialCard(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF3A3A1E), RoundedCornerShape(4.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16190E))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                color = GoldPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                color = TextGold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp
            )
        }
    }
}
