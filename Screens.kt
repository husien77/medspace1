package com.medspace.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medspace.app.data.AdminViewModel
import com.medspace.app.data.AuthViewModel
import com.medspace.app.data.Profile
import com.medspace.app.data.statusLabel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val OWNER_NAME = "الحسين الغرسي"
private const val APP_TAGLINE = "منصتك الأكاديمية لتنظيم كل ما يتعلق بدراستك."

// ---------------------------------------------------------------- شاشة البداية

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF3730A3), Color(0xFF6D28D9)))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text("MEDSPACE", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
            Spacer(Modifier.height(10.dp))
            Text(APP_TAGLINE, color = Color(0xFFE0E7FF), fontSize = 15.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(28.dp))
            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
        }
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Designed & Owned by", color = Color(0xFFC7D2FE), fontSize = 12.sp)
            Text(OWNER_NAME, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ConfigMissingScreen() {
    CenterMessage(
        title = "إعداد غير مكتمل",
        message = "لم يتم وضع بيانات Supabase في ملف Config.kt. اتبع خطوات ملف README ثم أعد بناء التطبيق."
    )
}

@Composable
private fun CenterMessage(title: String, message: String, content: @Composable () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        content()
    }
}

// ---------------------------------------------------------------- تسجيل الدخول / إنشاء حساب

@Composable
fun AuthScreen(vm: AuthViewModel) {
    val busy by vm.busy.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    var register by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    val ltr = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("MEDSPACE", color = MaterialTheme.colorScheme.primary, fontSize = 34.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(6.dp))
        Text(APP_TAGLINE, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (register) "طلب انضمام جديد" else "تسجيل الدخول",
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                if (register) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم الكامل") },
                        singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth())
                }
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("البريد الإلكتروني") },
                    singleLine = true, enabled = !busy, textStyle = ltr,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("كلمة المرور") },
                    singleLine = true, enabled = !busy, textStyle = ltr,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth())
                if (register) {
                    OutlinedTextField(value = confirm, onValueChange = { confirm = it }, label = { Text("تأكيد كلمة المرور") },
                        singleLine = true, enabled = !busy, textStyle = ltr,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth())
                }

                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }

                Button(
                    onClick = {
                        if (register) vm.signUp(name, email, password, confirm) else vm.signIn(email, password)
                    },
                    enabled = !busy,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (busy) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
                    else Text(if (register) "إرسال الطلب" else "دخول", fontWeight = FontWeight.Medium)
                }

                TextButton(
                    onClick = { vm.clearError(); register = !register },
                    enabled = !busy,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(if (register) "لديّ حساب بالفعل" else "ليس لديّ حساب — طلب انضمام")
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        if (register) {
            Text("بعد إرسال الطلب يتم قبولك من قبل المدير ثم يفتح لك المحتوى.",
                style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ---------------------------------------------------------------- حالات الحساب والأخطاء

@Composable
fun BlockedScreen(profile: Profile, busy: Boolean, onRefresh: () -> Unit, onLogout: () -> Unit) {
    val (title, message) = when (profile.status) {
        "rejected" -> "تم رفض طلب الانضمام" to "إن كان هذا خطأ تواصل مع المدير."
        "suspended" -> "الحساب موقوف" to "تواصل مع المدير لمعرفة السبب."
        else -> "طلبك قيد المراجعة" to "سيتم فتح المحتوى فور موافقة المدير. اضغط تحديث للتحقق."
    }
    CenterMessage(title, "أهلًا ${profile.fullName}\n$message") {
        Button(onClick = onRefresh, enabled = !busy, shape = RoundedCornerShape(14.dp)) {
            if (busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
            else Text("تحديث الحالة")
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onLogout) { Text("تسجيل الخروج") }
    }
}

@Composable
fun ErrorScreen(message: String, busy: Boolean, onRetry: () -> Unit, onLogout: () -> Unit) {
    CenterMessage("تعذّر المتابعة", message) {
        Button(onClick = onRetry, enabled = !busy, shape = RoundedCornerShape(14.dp)) {
            if (busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
            else Text("إعادة المحاولة")
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onLogout) { Text("تسجيل الخروج") }
    }
}

// ---------------------------------------------------------------- الرئيسية

@Composable
fun HomeScreen(profile: Profile, onAdmin: () -> Unit, onAbout: () -> Unit, onLogout: () -> Unit) {
    val today = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale("ar")))
    }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))),
                    RoundedCornerShape(24.dp)
                )
                .padding(22.dp)
        ) {
            Column {
                Text("MEDSPACE", color = Color(0xFFC7D2FE), fontSize = 13.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(6.dp))
                Text("أهلًا، ${profile.fullName}", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(today, color = Color(0xFFE0E7FF), fontSize = 14.sp)
            }
        }

        if (profile.isAdmin) {
            ActionCard("طلبات الانضمام وإدارة الطلاب", "قبول أو رفض أو إيقاف الحسابات", onAdmin)
        }
        ActionCard("عن التطبيق", "معلومات المنصة والمصمم", onAbout)

        Text(
            "المحتوى الأكاديمي (المواد والمحاضرات) يُضاف في الجزء التالي من المرحلة الأولى.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(onClick = onLogout, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Text("تسجيل الخروج")
        }
    }
}

@Composable
private fun ActionCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ---------------------------------------------------------------- عن التطبيق

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        TextButton(onClick = onBack) { Text("رجوع") }
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("MEDSPACE", color = MaterialTheme.colorScheme.primary, fontSize = 36.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            Text(APP_TAGLINE, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(36.dp))
            Text("Designed & Owned by", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(OWNER_NAME, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("المصمم وصاحب التطبيق والمدير الرئيسي", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(36.dp))
            Text("منصة خاصة بدفعة واحدة من طلاب الطب البشري", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("الإصدار 0.1.0", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ---------------------------------------------------------------- إدارة الطلاب

@Composable
fun AdminRequestsScreen(onBack: () -> Unit, onExpired: () -> Unit, vm: AdminViewModel = viewModel()) {
    val users by vm.items.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val expired by vm.expired.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var confirm by remember { mutableStateOf<Triple<Profile, String, String>?>(null) }

    LaunchedEffect(Unit) { vm.load() }
    LaunchedEffect(expired) { if (expired) onExpired() }

    val shown = if (tab == 0) users.filter { it.status == "pending" } else users

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("رجوع") }
            Text("إدارة الطلاب", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        TabRow(selectedTabIndex = tab, containerColor = Color.Transparent) {
            Tab(selected = tab == 0, onClick = { tab = 0 },
                text = { Text("قيد المراجعة (${users.count { it.status == "pending" }})") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("الجميع (${users.size})") })
        }
        Spacer(Modifier.height(10.dp))

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = { vm.load() }) { Text("إعادة المحاولة") }
        }
        if (loading) {
            Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(26.dp), strokeWidth = 2.5.dp)
            }
        }
        if (!loading && error == null && shown.isEmpty()) {
            Text(if (tab == 0) "لا توجد طلبات قيد المراجعة." else "لا يوجد مستخدمون بعد.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(shown, key = { it.id }) { p ->
                UserCard(p, enabled = !loading) { newStatus, label -> confirm = Triple(p, newStatus, label) }
            }
        }
    }

    confirm?.let { (p, newStatus, label) ->
        AlertDialog(
            onDismissRequest = { confirm = null },
            title = { Text("تأكيد العملية") },
            text = { Text("هل تريد $label الحساب: ${p.fullName}؟") },
            confirmButton = {
                TextButton(onClick = { vm.setStatus(p.id, newStatus); confirm = null }) { Text("تأكيد") }
            },
            dismissButton = { TextButton(onClick = { confirm = null }) { Text("إلغاء") } }
        )
    }
}

@Composable
private fun UserCard(p: Profile, enabled: Boolean, onAction: (String, String) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(p.fullName.ifBlank { "بدون اسم" }, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(end = 8.dp))
                Text(if (p.isAdmin) "المدير" else statusLabel(p.status),
                    color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            }
            Text(p.email, style = MaterialTheme.typography.bodySmall.copy(textDirection = TextDirection.Ltr),
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (!p.isAdmin) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (p.status) {
                        "pending" -> {
                            Button(onClick = { onAction("approved", "قبول") }, enabled = enabled,
                                shape = RoundedCornerShape(12.dp)) { Text("قبول") }
                            OutlinedButton(onClick = { onAction("rejected", "رفض") }, enabled = enabled,
                                shape = RoundedCornerShape(12.dp)) { Text("رفض") }
                        }
                        "approved" -> OutlinedButton(onClick = { onAction("suspended", "إيقاف") }, enabled = enabled,
                            shape = RoundedCornerShape(12.dp)) { Text("إيقاف") }
                        else -> Button(onClick = { onAction("approved", "إعادة تفعيل") }, enabled = enabled,
                            shape = RoundedCornerShape(12.dp)) { Text("إعادة تفعيل") }
                    }
                }
            }
        }
    }
}
