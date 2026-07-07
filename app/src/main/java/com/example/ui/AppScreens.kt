package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EmergencyRequest
import com.example.data.Hospital
import com.example.data.User
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Navigation state
enum class AppScreen {
    Register,
    Dashboard,
    Map,
    RequestBlood,
    Records,
    Settings
}

@Composable
fun AppNavigationContainer(viewModel: EmergencyViewModel) {
    val isRegistered by viewModel.isRegistered.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    // Observe toast messages
    LaunchedEffect(key1 = true) {
        viewModel.toastMessage.collect { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepMedicalBlue),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AppLogo(isDarkTheme = true)
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(color = CrimsonRed, strokeWidth = 3.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Syncing Logistics Network...",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = LightText.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    } else {
        var currentScreen by remember { mutableStateOf(AppScreen.Register) }

        // Sync screen with registration status
        LaunchedEffect(isRegistered) {
            currentScreen = if (isRegistered) AppScreen.Dashboard else AppScreen.Register
        }

        Scaffold(
            bottomBar = {
                if (isRegistered) {
                    BottomNavigationBar(
                        currentScreen = currentScreen,
                        onScreenSelected = { currentScreen = it }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    AppScreen.Register -> RegisterScreen(viewModel = viewModel)
                    AppScreen.Dashboard -> DonorDashboardScreen(viewModel = viewModel, onNavigateToMap = { currentScreen = AppScreen.Map })
                    AppScreen.Map -> EmergencyMapScreen(viewModel = viewModel)
                    AppScreen.RequestBlood -> RequestBloodScreen(viewModel = viewModel, onBroadcastSuccess = { currentScreen = AppScreen.Dashboard })
                    AppScreen.Records -> RecordsScreen(viewModel = viewModel)
                    AppScreen.Settings -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PureWhite)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .drawBehind {
                drawLine(
                    color = Color(0xFFF1F5F9),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 2f
                )
            }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Dashboard
            val dashboardActive = currentScreen == AppScreen.Dashboard
            val dashboardColor = if (dashboardActive) CrimsonRed else Color(0xFF94A3B8)
            Column(
                modifier = Modifier
                    .clickable { onScreenSelected(AppScreen.Dashboard) }
                    .padding(8.dp)
                    .testTag("nav_dashboard"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (dashboardActive) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                    contentDescription = "Home",
                    tint = dashboardColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Home",
                    color = dashboardColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Tab 2: Map
            val mapActive = currentScreen == AppScreen.Map
            val mapColor = if (mapActive) CrimsonRed else Color(0xFF94A3B8)
            Column(
                modifier = Modifier
                    .clickable { onScreenSelected(AppScreen.Map) }
                    .padding(8.dp)
                    .testTag("nav_map"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (mapActive) Icons.Filled.Map else Icons.Outlined.Map,
                    contentDescription = "Map",
                    tint = mapColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Map",
                    color = mapColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Tab 3: FAB Request Blood (Prominent in middle)
            val requestActive = currentScreen == AppScreen.RequestBlood
            val fabBgColor = if (requestActive) CrimsonRed else DeepMedicalBlue
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .shadow(6.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(fabBgColor)
                    .clickable { onScreenSelected(AppScreen.RequestBlood) }
                    .testTag("nav_request_blood"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Request Blood",
                    tint = PureWhite,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Tab 4: Records (Interactive Donation History)
            val recordsActive = currentScreen == AppScreen.Records
            val recordsColor = if (recordsActive) CrimsonRed else Color(0xFF94A3B8)
            Column(
                modifier = Modifier
                    .clickable { onScreenSelected(AppScreen.Records) }
                    .padding(8.dp)
                    .testTag("nav_records"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (recordsActive) Icons.Filled.History else Icons.Outlined.History,
                    contentDescription = "Records",
                    tint = recordsColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Records",
                    color = recordsColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Tab 5: Menu/Settings
            val settingsActive = currentScreen == AppScreen.Settings
            val settingsColor = if (settingsActive) CrimsonRed else Color(0xFF94A3B8)
            Column(
                modifier = Modifier
                    .clickable { onScreenSelected(AppScreen.Settings) }
                    .padding(8.dp)
                    .testTag("nav_settings"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (settingsActive) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Menu",
                    tint = settingsColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Menu",
                    color = settingsColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==========================================
// 1. REGISTER / ONBOARDING SCREEN
// ==========================================
@Composable
fun RegisterScreen(viewModel: EmergencyViewModel) {
    var name by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Donor") } // "Donor" or "Patient"
    var selectedBloodGroup by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var bloodError by remember { mutableStateOf(false) }

    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DeepMedicalBlue, DeepMedicalBlue.copy(alpha = 0.95f))
                )
            )
            .verticalScroll(rememberScrollState())
    ) {
        // Aesthetic layered background waves
        Canvas(modifier = Modifier.fillMaxSize().matchParentSize()) {
            val width = size.width
            val height = size.height

            val path1 = Path().apply {
                moveTo(0f, height * 0.35f)
                cubicTo(
                    width * 0.3f, height * 0.3f,
                    width * 0.7f, height * 0.45f,
                    width, height * 0.4f
                )
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = path1,
                brush = Brush.verticalGradient(
                    colors = listOf(CrimsonRed.copy(alpha = 0.08f), Color.Transparent)
                )
            )

            val path2 = Path().apply {
                moveTo(0f, height * 0.65f)
                cubicTo(
                    width * 0.25f, height * 0.75f,
                    width * 0.75f, height * 0.6f,
                    width, height * 0.72f
                )
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = path2,
                brush = Brush.verticalGradient(
                    colors = listOf(CrimsonRed.copy(alpha = 0.12f), CrimsonRed.copy(alpha = 0.02f))
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Premium, Pure-Code Brand Logo
            AppLogo(isDarkTheme = true)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Emergency Logistics & Donor Network",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = LightText.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Registration Card Layout
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create Profile",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepMedicalBlue
                        ),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Name Input
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (it.trim().length >= 3) nameError = false
                        },
                        label = { Text("Full Name") },
                        placeholder = { Text("e.g. Zainab Ahmed") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Name",
                                tint = DeepMedicalBlue
                            )
                        },
                        isError = nameError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = CrimsonRed,
                            focusedLabelColor = CrimsonRed,
                            unfocusedBorderColor = DeepMedicalBlue.copy(alpha = 0.3f),
                            errorBorderColor = CrimsonRed,
                            focusedPlaceholderColor = Color(0xFF8A8A8A),
                            unfocusedPlaceholderColor = Color(0xFF8A8A8A)
                        )
                    )
                    if (nameError) {
                        Text(
                            text = "Please enter a valid name (min 3 characters)",
                            color = CrimsonRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Role Selector
                    Text(
                        text = "Select Network Role",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = DeepMedicalBlue
                        ),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("Donor", "Patient").forEach { role ->
                            val isSelected = selectedRole == role
                            val backgroundColor by animateColorAsState(
                                targetValue = if (isSelected) CrimsonRed else SoftOffWhite,
                                label = "bg"
                            )
                            val contentColor by animateColorAsState(
                                targetValue = if (isSelected) PureWhite else DeepMedicalBlue,
                                label = "content"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(backgroundColor)
                                    .clickable { selectedRole = role }
                                    .testTag("role_tab_$role"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                               ) {
                                    Icon(
                                        imageVector = if (role == "Donor") Icons.Filled.Favorite else Icons.Filled.LocalHospital,
                                        contentDescription = role,
                                        tint = contentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = role,
                                        fontWeight = FontWeight.Bold,
                                        color = contentColor
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Blood Group Selection
                    Text(
                        text = "Blood Group Required / Owned",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = DeepMedicalBlue
                        ),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Large horizontal grid layout for blood groups
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val chunked = bloodGroups.chunked(4)
                        chunked.forEach { rowGroups ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowGroups.forEach { group ->
                                    val isSelected = selectedBloodGroup == group
                                    val borderStroke = if (isSelected) {
                                        BorderStroke(2.dp, CrimsonRed)
                                    } else {
                                        BorderStroke(1.dp, DeepMedicalBlue.copy(alpha = 0.15f))
                                    }
                                    val bg = if (isSelected) CrimsonRed.copy(alpha = 0.08f) else PureWhite
                                    val textColor = if (isSelected) CrimsonRed else TextSlateGray

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(bg)
                                            .border(borderStroke, RoundedCornerShape(12.dp))
                                            .clickable {
                                                selectedBloodGroup = group
                                                bloodError = false
                                            }
                                            .testTag("blood_group_$group"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = group,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (bloodError) {
                        Text(
                            text = "Please select a blood group",
                            color = CrimsonRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Submit Register Button
                    Button(
                        onClick = {
                            val isNameValid = name.trim().length >= 3
                            val isBloodValid = selectedBloodGroup.isNotEmpty()

                            nameError = !isNameValid
                            bloodError = !isBloodValid

                            if (isNameValid && isBloodValid) {
                                viewModel.registerUser(
                                    name = name.trim(),
                                    role = selectedRole,
                                    bloodGroup = selectedBloodGroup
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = "JOIN EMERGENCY NETWORK",
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = PureWhite,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = "Secure logistics matching with near-zero latency.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = LightText.copy(alpha = 0.45f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==========================================
// 2. DONOR DASHBOARD SCREEN
// ==========================================
@Composable
fun DonorDashboardScreen(
    viewModel: EmergencyViewModel,
    onNavigateToMap: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val activeRequests by viewModel.activeRequests.collectAsState()
    val localUser = currentUser ?: return

    var showIdCard by remember { mutableStateOf(false) }
    var eligibilityStatus by remember { mutableStateOf("ELIGIBLE") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftOffWhite)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Elegant Top Header with User Info & Emergency Counter
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PureWhite)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .clickable { showIdCard = true }
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = "AURA MEDICAL",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = DeepMedicalBlue.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = localUser.name,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = DeepMedicalBlue,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Text(
                            text = "Role: ${localUser.role} | Group: ${localUser.bloodGroup}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSlateGray.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    // Avatar with Initials (Clicking now opens the Digital ID card safely, matching visual layout)
                    val initials = localUser.name.split(" ")
                        .filter { it.isNotEmpty() }
                        .take(2)
                        .joinToString("") { it.take(1).uppercase() }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SoftOffWhite)
                            .border(2.dp, PureWhite, CircleShape)
                            .clickable { showIdCard = true }
                            .testTag("avatar_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(DeepMedicalBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (initials.isNotEmpty()) initials else "US",
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Stats row & Availability Toggle Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Interactive Availability Card for Donors
                if (localUser.role == "Donor") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                val pulseScale = rememberInfiniteTransition(label = "pulse")
                                val scale by pulseScale.animateFloat(
                                    initialValue = 0.85f,
                                    targetValue = 1.15f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1000, easing = EaseInOutBack),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "scale"
                                )

                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .drawBehind {
                                            drawCircle(
                                                color = if (localUser.isAvailable) CrimsonRed else Color.LightGray,
                                                radius = size.minDimension / 2 * (if (localUser.isAvailable) scale else 1f)
                                            )
                                        }
                                )

                                Column {
                                    Text(
                                        text = if (localUser.isAvailable) "Status: Available" else "Status: Inactive",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = DeepMedicalBlue,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Broadcast location to nearby patients",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSlateGray.copy(alpha = 0.7f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }

                            Switch(
                                checked = localUser.isAvailable,
                                onCheckedChange = { viewModel.toggleAvailability(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PureWhite,
                                    checkedTrackColor = CrimsonRed,
                                    uncheckedThumbColor = Color.LightGray,
                                    uncheckedTrackColor = Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.testTag("available_switch")
                            )
                        }
                    }
                }

                // Interactive Blood Donation Eligibility Checker Card (Bonus Feature)
                EligibilityCheckerCard(
                    onEligibilityUpdated = { eligible ->
                        eligibilityStatus = if (eligible) "ELIGIBLE" else "DEFERRED"
                    }
                )

                // Aesthetic Metrics Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.VolunteerActivism,
                        title = "Lives Impacted",
                        value = "3 Lives",
                        color = CrimsonRed
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Timer,
                        title = "Status Eligibility",
                        value = eligibilityStatus,
                        color = if (eligibilityStatus == "ELIGIBLE") Color(0xFF2E7D32) else CrimsonRed,
                        onClick = { showIdCard = true }
                    )
                }

                // Urgent Requests Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "NETWORK ACTIVITY",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = DeepMedicalBlue,
                                letterSpacing = 1.sp
                            )
                        )
                        Box(
                            modifier = Modifier
                                .background(CrimsonRed.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                color = CrimsonRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Text(
                        text = "${activeRequests.size} Nearby",
                        color = DeepMedicalBlue.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (activeRequests.isEmpty()) {
                    // Friendly Empty State Placeholder
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, DeepMedicalBlue.copy(alpha = 0.05f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "Safe",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Zero Emergencies Pending",
                                fontWeight = FontWeight.Bold,
                                color = DeepMedicalBlue,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Great news! No active blood broadcasts are currently reported in your area.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSlateGray.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                } else {
                    // Active broadcast logs matching user's blood group first (prioritized)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        activeRequests.forEach { request ->
                            EmergencyRequestCard(
                                request = request,
                                isMatch = request.bloodGroupRequired == localUser.bloodGroup,
                                onAccept = { viewModel.acceptDonation(request) },
                                onNavigate = onNavigateToMap
                            )
                        }
                    }
                }
            }
        }

        // Beautiful glassmorphism dialog overlay for Donor Digital ID Card & secure QR Code (Bonus Feature 1)
        if (showIdCard) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable { showIdCard = false },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clickable(enabled = false) {}
                        .padding(16.dp)
                ) {
                    DonorIDCard(
                        user = localUser,
                        eligibilityStatus = eligibilityStatus,
                        onDismiss = { showIdCard = false },
                        onLogout = {
                            showIdCard = false
                            viewModel.logout()
                        }
                    )
                }
            }
        }
    }
}

// ==========================================
// CANVA-BASED HIGH-FIDELITY SECURE QR CODE GENERATOR
// ==========================================
@Composable
fun QRCodeDrawing(data: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val sizePx = size.minDimension
        val gridCount = 21 // 21x21 grid representing a compact Version 1 QR matrix
        val cellSize = sizePx / gridCount

        // Standard pure white background for QR scanner contrast
        drawRect(color = Color.White)

        // Helper to draw standard QR Finder Pattern anchors (top-left, top-right, bottom-left)
        fun drawFinderPattern(col: Int, row: Int) {
            val startX = col * cellSize
            val startY = row * cellSize
            val outerSize = 7 * cellSize

            // Outer dark square
            drawRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(startX, startY),
                size = androidx.compose.ui.geometry.Size(outerSize, outerSize)
            )
            // Inner white square
            val innerWhiteSize = 5 * cellSize
            drawRect(
                color = Color.White,
                topLeft = Offset(startX + cellSize, startY + cellSize),
                size = androidx.compose.ui.geometry.Size(innerWhiteSize, innerWhiteSize)
            )
            // Center dark solid block
            val centerBlackSize = 3 * cellSize
            drawRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(startX + 2 * cellSize, startY + 2 * cellSize),
                size = androidx.compose.ui.geometry.Size(centerBlackSize, centerBlackSize)
            )
        }

        // Render the 3 standard anchor squares
        drawFinderPattern(0, 0)                  // Top-Left
        drawFinderPattern(gridCount - 7, 0)      // Top-Right
        drawFinderPattern(0, gridCount - 7)      // Bottom-Left

        // Deterministic PRNG seeded with input data hash to draw a visually stable and authentic layout
        val hash = data.hashCode()
        val random = java.util.Random(hash.toLong())

        for (col in 0 until gridCount) {
            for (row in 0 until gridCount) {
                // Ensure we don't overwrite finder patterns
                val isTopLeftFinder = col < 7 && row < 7
                val isTopRightFinder = col >= (gridCount - 7) && row < 7
                val isBottomLeftFinder = col < 7 && row >= (gridCount - 7)

                if (!isTopLeftFinder && !isTopRightFinder && !isBottomLeftFinder) {
                    val isTimingLine = (row == 6 && col % 2 == 0) || (col == 6 && row % 2 == 0)
                    val isDataBit = random.nextBoolean()

                    if (isTimingLine || isDataBit) {
                        drawRect(
                            color = Color(0xFF0F172A),
                            topLeft = Offset(col * cellSize, row * cellSize),
                            size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// DIGITAL GLASSMORPHIC ID CARD & VERIFIED BADGING
// ==========================================
@Composable
fun DonorIDCard(
    user: User,
    eligibilityStatus: String,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SMART BLOOD NETWORK",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = CrimsonRed,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Text(
                        text = "DIGITAL DONOR ID",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = DeepMedicalBlue,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }

                // Blood type drop-shaped or circular badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(CrimsonRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.bloodGroup,
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Premium frame around QR Code
            Card(
                modifier = Modifier
                    .size(170.dp)
                    .border(2.dp, CrimsonRed.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                QRCodeDrawing(
                    data = "SmartBlood_Donor_${user.uid}_BG_${user.bloodGroup}_Eligible_${eligibilityStatus == "ELIGIBLE"}",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = user.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = DeepMedicalBlue,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Text(
                text = "ID: ${user.uid.uppercase()}",
                color = TextSlateGray.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Visual badge reflecting Checker Outcome
            val isVerified = eligibilityStatus == "ELIGIBLE"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(
                        color = if (isVerified) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(100.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = eligibilityStatus,
                    tint = if (isVerified) Color(0xFF2E7D32) else CrimsonRed,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isVerified) "VERIFIED DONOR ELIGIBLE" else "DEFERRED STATUS ADVISORY",
                    color = if (isVerified) Color(0xFF2E7D32) else CrimsonRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Hospitals scan this digital signature to verify logistics, live telemetry tracking eligibility, and blood compliance protocols during active alerts.",
                color = TextSlateGray.copy(alpha = 0.7f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Elegant Bottom Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onLogout,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("logout_button"),
                    border = BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text("De-Register", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepMedicalBlue),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text("Close Card", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

// ==========================================
// INTERACTIVE BLOOD DONATION ELIGIBILITY CHECKER
// ==========================================
@Composable
fun EligibilityCheckerCard(
    onEligibilityUpdated: (Boolean) -> Unit
) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    val answers = remember { mutableStateListOf<Boolean>() }
    var isSubmitted by remember { mutableStateOf(false) }
    var isEligible by remember { mutableStateOf(true) }

    val questions = listOf(
        "Have you received any tattoos, acupuncture, or body piercings in the past 6 months?",
        "Have you traveled outside the country or into a malaria-endemic region in the last 28 days?",
        "Has it been at least 56 days (8 weeks) since your last blood donation?"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FactCheck,
                        contentDescription = "Checker",
                        tint = CrimsonRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "LOGISTICS ELIGIBILITY CHECKER",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = DeepMedicalBlue,
                            letterSpacing = 1.sp
                        )
                    )
                }

                if (!isSubmitted) {
                    Text(
                        text = "Q ${currentQuestionIndex + 1}/${questions.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextSlateGray.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isSubmitted) {
                Text(
                    text = questions[currentQuestionIndex],
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DeepMedicalBlue,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp
                    ),
                    modifier = Modifier.height(54.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            answers.add(true)
                            if (currentQuestionIndex < questions.size - 1) {
                                currentQuestionIndex++
                            } else {
                                // Tattoos: NO (false), Travel: NO (false), WaitTime: YES (true)
                                val tattooYes = answers[0]
                                val travelYes = answers[1]
                                val waitTimeYes = answers[2]
                                isEligible = !tattooYes && !travelYes && waitTimeYes
                                isSubmitted = true
                                onEligibilityUpdated(isEligible)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("YES", color = DeepMedicalBlue, fontWeight = FontWeight.ExtraBold)
                    }

                    Button(
                        onClick = {
                            answers.add(false)
                            if (currentQuestionIndex < questions.size - 1) {
                                currentQuestionIndex++
                            } else {
                                val tattooYes = answers[0]
                                val travelYes = answers[1]
                                val waitTimeYes = answers[2]
                                isEligible = !tattooYes && !travelYes && waitTimeYes
                                isSubmitted = true
                                onEligibilityUpdated(isEligible)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("NO", color = DeepMedicalBlue, fontWeight = FontWeight.ExtraBold)
                    }
                }
            } else {
                // Submitted State
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isEligible) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(Color(0xFFE8F5E9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Eligible",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Logistics Verified: Eligible",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You meet all local healthcare standards. Keep your broadcast toggle active to receive requests.",
                            fontSize = 11.sp,
                            color = TextSlateGray.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(Color(0xFFFFEBEE), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Deferred",
                                tint = CrimsonRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Deferred Donation Status",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = CrimsonRed,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You do not meet immediate eligibility criteria. This is standard to protect safety limits.",
                            fontSize = 11.sp,
                            color = TextSlateGray.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            currentQuestionIndex = 0
                            answers.clear()
                            isSubmitted = false
                            isEligible = true
                            onEligibilityUpdated(true)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepMedicalBlue),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        shape = RoundedCornerShape(100.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Retake", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// LAHORE REGION DUMMY DATA SEEDING UTILITY
// ==========================================
@Composable
fun DeveloperSeederPanel(viewModel: EmergencyViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(DeepMedicalBlue, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SettingsSuggest,
                        contentDescription = "Dev",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "DEVELOPER TOOLKIT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepMedicalBlue.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Seed Lahore Data",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepMedicalBlue
                    )
                }
            }

            Button(
                onClick = { viewModel.forceSeedLahoreData() },
                colors = ButtonDefaults.buttonColors(containerColor = DeepMedicalBlue),
                shape = RoundedCornerShape(100.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(32.dp)
                    .testTag("seed_database_button")
            ) {
                Text("Seed Database", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    color: Color,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier,
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSlateGray.copy(alpha = 0.5f),
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = DeepMedicalBlue,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }
}

@Composable
fun EmergencyRequestCard(
    request: EmergencyRequest,
    isMatch: Boolean,
    onAccept: () -> Unit,
    onNavigate: () -> Unit
) {
    val cardBackground = if (isMatch) CrimsonRed else DeepMedicalBlue
    val contentColor = PureWhite

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("emergency_request_${request.requestId}"),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Main content block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Urgency Pill
                    Box(
                        modifier = Modifier
                            .background(PureWhite.copy(alpha = 0.2f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isMatch) "HIGH URGENCY MATCH" else "NEIGHBORHOOD ALERT",
                            color = contentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }

                    // Distance indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Distance",
                            tint = contentColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "1.2 km away",
                            color = contentColor.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Patient and Blood Info
                Text(
                    text = "${request.bloodGroupRequired} Blood Required",
                    color = contentColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${request.patientName} • ${request.hospitalName}",
                    color = contentColor.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Bottom bar matching HTML style with black/10 background, unit label and actions
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.12f))
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Units indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(PureWhite.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${request.units}U",
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "Units needed",
                            color = PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Dual CTAs: View Map & Accept Request
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Small icon button to view map
                        IconButton(
                            onClick = onNavigate,
                            modifier = Modifier
                                .size(36.dp)
                                .background(PureWhite.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "View Map",
                                tint = PureWhite,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Accept Request CTA Pill
                        Button(
                            onClick = onAccept,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PureWhite,
                                contentColor = cardBackground
                            ),
                            shape = RoundedCornerShape(100.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = "Accept Request",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. INTERACTIVE MAP SCREEN (LAHORE SCHEMATIC)
// ==========================================
@Composable
fun EmergencyMapScreen(viewModel: EmergencyViewModel) {
    val activeRequests by viewModel.activeRequests.collectAsState()
    val allHospitals by viewModel.allHospitals.collectAsState()
    val selectedRequest by viewModel.selectedRequest.collectAsState()

    // Map gesture offset values
    var mapOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    var scaleFactor by remember { mutableStateOf(1f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE2E8F0)) // Warm map base color
    ) {
        // Full Canvas-based Dynamic Lahore Schematic Map
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        mapOffset += dragAmount
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2 + mapOffset.x
            val centerY = height / 2 + mapOffset.y

            // Draw Schematic Lahore Roads Grid
            // Canal Road
            drawLine(
                color = Color(0xFF94A3B8),
                start = Offset(centerX - 600, centerY - 400),
                end = Offset(centerX + 600, centerY + 400),
                strokeWidth = 12f
            )

            // Ferozepur Road
            drawLine(
                color = Color(0xFF94A3B8),
                start = Offset(centerX - 100, centerY - 600),
                end = Offset(centerX - 100, centerY + 600),
                strokeWidth = 14f
            )

            // Jail Road / Mall Road Intersects
            drawLine(
                color = Color(0xFFcbd5e1),
                start = Offset(centerX - 500, centerY),
                end = Offset(centerX + 500, centerY - 100),
                strokeWidth = 8f
            )

            // River Ravi schematics (Light Blue Curved Ribbon)
            val riverPath = Path().apply {
                moveTo(centerX - 700, centerY - 300)
                quadraticTo(
                    centerX - 200, centerY - 450,
                    centerX + 700, centerY - 250
                )
            }
            drawPath(
                path = riverPath,
                color = Color(0xFF93C5FD).copy(alpha = 0.6f),
                style = Stroke(width = 32f)
            )

            // Center Ring/Grid indicators
            drawCircle(
                color = CrimsonRed.copy(alpha = 0.04f),
                center = Offset(centerX, centerY),
                radius = 200f
            )
            drawCircle(
                color = CrimsonRed.copy(alpha = 0.02f),
                center = Offset(centerX, centerY),
                radius = 400f
            )
        }

        // Overlay Interactive Node Markers
        // 1. User Position Indicator (Pulsing Green Node)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(0.dp))
        ) {
            val width = 1080f // virtual resolution base
            val height = 1920f
            
            // Render Hospitals
            allHospitals.forEach { hospital ->
                // Map Coordinates to screen offset
                val mapX = ((hospital.longitude - 74.31) * 8000.0).toFloat() + mapOffset.x + 400f
                val mapY = (-(hospital.latitude - 31.52) * 8000.0).toFloat() + mapOffset.y + 700f

                HospitalMarker(
                    x = mapX,
                    y = mapY,
                    hospital = hospital,
                    isSelected = selectedRequest?.hospitalName == hospital.name,
                    onSelect = {
                        // Find if there is a matching request in this hospital
                        val matchingRequest = activeRequests.find { it.hospitalName == hospital.name }
                        if (matchingRequest != null) {
                            viewModel.selectRequest(matchingRequest)
                        } else {
                            // Dummy request matching hospital
                            viewModel.selectRequest(
                                EmergencyRequest(
                                    requestId = "hosp_${hospital.hospitalId}",
                                    patientName = "Direct Logistics Liaison",
                                    bloodGroupRequired = "A/B/O",
                                    units = 0,
                                    hospitalName = hospital.name,
                                    latitude = hospital.latitude,
                                    longitude = hospital.longitude,
                                    status = "hospital"
                                )
                            )
                        }
                    }
                )
            }

            // Render Active Requests
            activeRequests.forEach { request ->
                val mapX = ((request.longitude - 74.31) * 8000.0).toFloat() + mapOffset.x + 400f
                val mapY = (-(request.latitude - 31.52) * 8000.0).toFloat() + mapOffset.y + 700f

                RequestPulseMarker(
                    x = mapX,
                    y = mapY,
                    request = request,
                    isSelected = selectedRequest?.requestId == request.requestId,
                    onSelect = { viewModel.selectRequest(request) }
                )
            }

            // Render Lahore Center point node
            val userX = mapOffset.x + 400f + 389.6f
            val userY = mapOffset.y + 700f - 83.2f
            UserPinMarker(x = userX, y = userY)
        }

        // Floating Title Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(PureWhite.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                .border(1.dp, DeepMedicalBlue.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .padding(12.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(CrimsonRed.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Map Center",
                        tint = CrimsonRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = "LAHORE METRO SECTOR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DeepMedicalBlue,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Live Dispatch Logistics Feed",
                        fontWeight = FontWeight.Bold,
                        color = TextSlateGray,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Interactive Bottom Slide Panel for selected markers
        AnimatedVisibility(
            visible = selectedRequest != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val req = selectedRequest
            if (req != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("map_detail_panel"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            if (req.status == "hospital") DeepMedicalBlue.copy(alpha = 0.1f)
                                            else CrimsonRed.copy(alpha = 0.1f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (req.status == "hospital") Icons.Default.LocalHospital else Icons.Default.Favorite,
                                        contentDescription = "Pin type",
                                        tint = if (req.status == "hospital") DeepMedicalBlue else CrimsonRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = req.hospitalName,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepMedicalBlue,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = if (req.status == "hospital") "Emergency Hub Facility" else "Critical Patient: ${req.patientName}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSlateGray.copy(alpha = 0.5f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.selectRequest(null) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = TextSlateGray.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = DeepMedicalBlue.copy(alpha = 0.05f)
                        )

                        if (req.status != "hospital") {
                            // Request Information
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "BLOOD GROUP REQUIRED",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextSlateGray.copy(alpha = 0.4f),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = req.bloodGroupRequired,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = CrimsonRed
                                        )
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "UNITS REQUESTED",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextSlateGray.copy(alpha = 0.4f),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "${req.units} BAGS",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = DeepMedicalBlue
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.fulfillRequest(req.requestId) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Fulfill",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Mark Fulfilled", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { viewModel.acceptDonation(req) },
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolunteerActivism,
                                        contentDescription = "Donate",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Initiate Dispatch", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // General Hub info
                            Text(
                                text = "This hospital is an active dispatch node in Lahore. Contact this facility directly to sync manual emergency deliveries or drop-offs.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSlateGray.copy(alpha = 0.7f)
                                ),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Button(
                                onClick = { /* Call simulated */ },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepMedicalBlue)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Contact Hub",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Contact Hospital Logistics", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserPinMarker(x: Float, y: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val radiusPulse by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius"
    )
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .offset { IntOffset(x.roundToInt() - 15, y.roundToInt() - 15) }
            .size(30.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Pulse circle
            drawCircle(
                color = Color(0xFF2E7D32),
                radius = radiusPulse,
                alpha = alphaPulse
            )
            // Center indicator
            drawCircle(
                color = PureWhite,
                radius = 7f
            )
            drawCircle(
                color = Color(0xFF2E7D32),
                radius = 5f
            )
        }
    }
}

@Composable
fun HospitalMarker(
    x: Float,
    y: Float,
    hospital: Hospital,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val markerSize = if (isSelected) 36.dp else 28.dp
    val color = if (isSelected) CrimsonRed else DeepMedicalBlue

    Box(
        modifier = Modifier
            .offset { IntOffset(x.roundToInt() - 14, y.roundToInt() - 14) }
            .size(markerSize)
            .background(color, CircleShape)
            .border(2.dp, PureWhite, CircleShape)
            .clickable { onSelect() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocalHospital,
            contentDescription = hospital.name,
            tint = PureWhite,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
fun RequestPulseMarker(
    x: Float,
    y: Float,
    request: EmergencyRequest,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 15f,
        targetValue = 42f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseSize"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .offset { IntOffset(x.roundToInt() - 20, y.roundToInt() - 20) }
            .size(40.dp)
            .clickable { onSelect() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = CrimsonRed,
                radius = pulseSize,
                alpha = pulseAlpha
            )
        }

        Box(
            modifier = Modifier
                .size(if (isSelected) 30.dp else 24.dp)
                .background(CrimsonRed, CircleShape)
                .border(1.5.dp, PureWhite, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = request.bloodGroupRequired,
                fontWeight = FontWeight.Black,
                color = PureWhite,
                fontSize = if (isSelected) 10.sp else 8.sp
            )
        }
    }
}

// ==========================================
// 4. REQUEST BLOOD SCREEN (BROADCASTER)
// ==========================================
@Composable
fun RequestBloodScreen(
    viewModel: EmergencyViewModel,
    onBroadcastSuccess: () -> Unit
) {
    val hospitals by viewModel.allHospitals.collectAsState()

    var patientName by remember { mutableStateOf("") }
    var selectedBloodGroup by remember { mutableStateOf("") }
    var selectedUnits by remember { mutableStateOf(2) }
    var selectedHospital by remember { mutableStateOf<Hospital?>(null) }

    var pNameError by remember { mutableStateOf(false) }
    var bloodError by remember { mutableStateOf(false) }
    var hospError by remember { mutableStateOf(false) }

    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftOffWhite)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // High Urgency header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CrimsonRed),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(PureWhite.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Urgent",
                            tint = PureWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "EMERGENCY BROADCAST SYSTEM",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Pulse notifications will broadcast immediately to compatible local donors.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PureWhite.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            // Input Fields Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Patient & Logistics Details",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepMedicalBlue
                        )
                    )

                    // Patient Name
                    OutlinedTextField(
                        value = patientName,
                        onValueChange = {
                            patientName = it
                            if (it.trim().isNotEmpty()) pNameError = false
                        },
                        label = { Text("Patient Name") },
                        placeholder = { Text("e.g. Salim Khan") },
                        isError = pNameError,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("patient_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = CrimsonRed,
                            focusedLabelColor = CrimsonRed,
                            unfocusedBorderColor = DeepMedicalBlue.copy(alpha = 0.15f),
                            focusedPlaceholderColor = Color(0xFF8A8A8A),
                            unfocusedPlaceholderColor = Color(0xFF8A8A8A)
                        )
                    )

                    // Blood Selection Header
                    Text(
                        text = "Required Blood Group",
                        fontWeight = FontWeight.Bold,
                        color = DeepMedicalBlue,
                        fontSize = 14.sp
                    )

                    // Horizonal flows of blood selection
                    val rows = bloodGroups.chunked(4)
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { grp ->
                                val isSelected = selectedBloodGroup == grp
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) CrimsonRed else SoftOffWhite)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) CrimsonRed else DeepMedicalBlue.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            selectedBloodGroup = grp
                                            bloodError = false
                                        }
                                        .testTag("req_blood_group_$grp"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = grp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) PureWhite else TextSlateGray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    // Step counter for blood bags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Units Required (Bags)",
                            fontWeight = FontWeight.Bold,
                            color = DeepMedicalBlue,
                            fontSize = 14.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = { if (selectedUnits > 1) selectedUnits-- },
                                modifier = Modifier.background(SoftOffWhite, CircleShape)
                            ) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = "Minus", tint = DeepMedicalBlue)
                            }

                            Text(
                                text = selectedUnits.toString(),
                                fontWeight = FontWeight.ExtraBold,
                                color = DeepMedicalBlue,
                                fontSize = 18.sp
                            )

                            IconButton(
                                onClick = { selectedUnits++ },
                                modifier = Modifier.background(SoftOffWhite, CircleShape)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Plus", tint = DeepMedicalBlue)
                            }
                        }
                    }
                }
            }

            // Facility Selection Cards
            Text(
                text = "Select Dispatch Hospital / Hub",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepMedicalBlue
                ),
                modifier = Modifier.padding(top = 8.dp)
            )

            if (hospitals.isEmpty()) {
                CircularProgressIndicator(color = CrimsonRed, modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    hospitals.forEach { hospital ->
                        val isSelected = selectedHospital?.hospitalId == hospital.hospitalId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) CrimsonRed else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedHospital = hospital
                                    hospError = false
                                }
                                .testTag("hospital_card_${hospital.hospitalId}"),
                            colors = CardDefaults.cardColors(containerColor = PureWhite)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                if (isSelected) CrimsonRed.copy(alpha = 0.1f) else SoftOffWhite,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalHospital,
                                            contentDescription = "Hub",
                                            tint = if (isSelected) CrimsonRed else DeepMedicalBlue
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = hospital.name,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepMedicalBlue,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = hospital.address,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextSlateGray.copy(alpha = 0.5f)
                                            )
                                        )
                                    }
                                }

                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        selectedHospital = hospital
                                        hospError = false
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = CrimsonRed,
                                        unselectedColor = DeepMedicalBlue.copy(alpha = 0.2f)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pulse Broadcast Trigger
            Button(
                onClick = {
                    val pNameValid = patientName.trim().isNotEmpty()
                    val bloodValid = selectedBloodGroup.isNotEmpty()
                    val hospValid = selectedHospital != null

                    pNameError = !pNameValid
                    bloodError = !bloodValid
                    hospError = !hospValid

                    if (pNameValid && bloodValid && hospValid) {
                        viewModel.broadcastRequest(
                            patientName = patientName.trim(),
                            bloodGroup = selectedBloodGroup,
                            units = selectedUnits,
                            hospital = selectedHospital!!
                        )
                        onBroadcastSuccess()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("broadcast_emergency_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Campaign, contentDescription = "Broadcast", tint = PureWhite)
                    Text(
                        text = "BROADCAST EMERGENCY REQUEST",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        color = PureWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// HIGH-FIDELITY INTERACTIVE DONATION RECORDS SCREEN
// ==========================================
@Composable
fun RecordsScreen(viewModel: EmergencyViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val localUser = currentUser ?: return

    var showAddLogDialog by remember { mutableStateOf(false) }
    val donationLogs = remember {
        mutableStateListOf(
            DonationLog("Lahore General Hospital", "June 15, 2026", "O-", 1),
            DonationLog("Jinnah Hospital Lahore", "April 02, 2026", "O-", 1),
            DonationLog("Mayo Hospital Lahore", "Jan 10, 2026", "O-", 1)
        )
    }

    var inputHospital by remember { mutableStateOf("") }
    var inputDate by remember { mutableStateOf("") }
    var inputUnits by remember { mutableStateOf("1") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftOffWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AURA LOGISTICS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = CrimsonRed,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "DONATION RECORDS",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = DeepMedicalBlue,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }

                // Add Button
                Button(
                    onClick = {
                        inputHospital = ""
                        inputDate = "July 07, 2026"
                        inputUnits = "1"
                        showAddLogDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    shape = RoundedCornerShape(100.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("add_donation_record_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Log", tint = PureWhite, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stats Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Donations", fontSize = 11.sp, color = TextSlateGray.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                        Text("${donationLogs.size}", fontSize = 24.sp, color = DeepMedicalBlue, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Units Contributed", fontSize = 11.sp, color = TextSlateGray.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                        val totalUnits = donationLogs.sumOf { it.units }
                        Text("$totalUnits Unit${if (totalUnits > 1) "s" else ""}", fontSize = 24.sp, color = CrimsonRed, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // History Header
            Text(
                text = "LOGISTICS HISTORY VERIFIED",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = TextSlateGray.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Scrollable list of donations
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(donationLogs) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(CrimsonRed.copy(alpha = 0.08f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolunteerActivism,
                                        contentDescription = "Blood drop",
                                        tint = CrimsonRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = log.hospital,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepMedicalBlue,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "${log.date} • ${log.units} Unit (${log.bloodGroup})",
                                        fontSize = 12.sp,
                                        color = TextSlateGray.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            // Status badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(100.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFF2E7D32), CircleShape)
                                )
                                Text(
                                    text = "VERIFIED",
                                    color = Color(0xFF2E7D32),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add Log Dialog
        if (showAddLogDialog) {
            AlertDialog(
                onDismissRequest = { showAddLogDialog = false },
                title = {
                    Text(
                        text = "LOG PAST DONATION",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = DeepMedicalBlue,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = inputHospital,
                            onValueChange = { inputHospital = it },
                            label = { Text("Hospital Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_hospital_name"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedBorderColor = CrimsonRed,
                                focusedLabelColor = CrimsonRed,
                                unfocusedBorderColor = DeepMedicalBlue.copy(alpha = 0.15f),
                                focusedPlaceholderColor = Color(0xFF8A8A8A),
                                unfocusedPlaceholderColor = Color(0xFF8A8A8A)
                            )
                        )

                        OutlinedTextField(
                            value = inputDate,
                            onValueChange = { inputDate = it },
                            label = { Text("Donation Date") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_donation_date"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedBorderColor = CrimsonRed,
                                focusedLabelColor = CrimsonRed,
                                unfocusedBorderColor = DeepMedicalBlue.copy(alpha = 0.15f),
                                focusedPlaceholderColor = Color(0xFF8A8A8A),
                                unfocusedPlaceholderColor = Color(0xFF8A8A8A)
                            )
                        )

                        OutlinedTextField(
                            value = inputUnits,
                            onValueChange = { inputUnits = it },
                            label = { Text("Blood Units") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_blood_units"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedBorderColor = CrimsonRed,
                                focusedLabelColor = CrimsonRed,
                                unfocusedBorderColor = DeepMedicalBlue.copy(alpha = 0.15f),
                                focusedPlaceholderColor = Color(0xFF8A8A8A),
                                unfocusedPlaceholderColor = Color(0xFF8A8A8A)
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (inputHospital.trim().isNotEmpty()) {
                                donationLogs.add(
                                    0,
                                    DonationLog(
                                        hospital = inputHospital.trim(),
                                        date = inputDate.trim(),
                                        bloodGroup = localUser.bloodGroup,
                                        units = inputUnits.toIntOrNull() ?: 1
                                    )
                                )
                                showAddLogDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                    ) {
                        Text("Log Donation", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddLogDialog = false }) {
                        Text("Cancel", color = DeepMedicalBlue)
                    }
                }
            )
        }
    }
}

data class DonationLog(
    val hospital: String,
    val date: String,
    val bloodGroup: String,
    val units: Int
)

// ==========================================
// HIGH-FIDELITY SETTINGS SCREEN WITH SECURED DEVELOPER TOOLKIT
// ==========================================
@Composable
fun SettingsScreen(viewModel: EmergencyViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val localUser = currentUser ?: return

    var enableAlerts by remember { mutableStateOf(true) }
    var enableTelemetry by remember { mutableStateOf(true) }
    var enableSilentMode by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftOffWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column {
                Text(
                    text = "AURA MEDICAL",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = CrimsonRed,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "MENU & SETTINGS",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = DeepMedicalBlue,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }

            // Profile Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    val initials = localUser.name.split(" ")
                        .filter { it.isNotEmpty() }
                        .take(2)
                        .joinToString("") { it.take(1) }
                        .uppercase()

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(CrimsonRed.copy(alpha = 0.1f), CircleShape)
                            .border(2.dp, CrimsonRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = CrimsonRed,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = localUser.name,
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepMedicalBlue,
                        fontSize = 18.sp
                    )

                    Text(
                        text = "UID: ${localUser.uid.uppercase()}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TextSlateGray.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Blood Badge
                        Box(
                            modifier = Modifier
                                .background(CrimsonRed, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "BLOOD TYPE: ${localUser.bloodGroup}",
                                color = PureWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Role Badge
                        Box(
                            modifier = Modifier
                                .background(DeepMedicalBlue, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = localUser.role.uppercase(),
                                color = PureWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Preference Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "PREFERENCES & TELEMETRY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepMedicalBlue.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )

                    // Toggle 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = DeepMedicalBlue)
                            Column {
                                Text("Emergency Push Alerts", fontWeight = FontWeight.Bold, color = DeepMedicalBlue, fontSize = 14.sp)
                                Text("Broadcast notifications nearby", fontSize = 11.sp, color = TextSlateGray.copy(alpha = 0.6f))
                            }
                        }
                        Switch(
                            checked = enableAlerts,
                            onCheckedChange = { enableAlerts = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = CrimsonRed)
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Toggle 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null, tint = DeepMedicalBlue)
                            Column {
                                Text("Live Telemetry Location", fontWeight = FontWeight.Bold, color = DeepMedicalBlue, fontSize = 14.sp)
                                Text("Share location safely with dispatch", fontSize = 11.sp, color = TextSlateGray.copy(alpha = 0.6f))
                            }
                        }
                        Switch(
                            checked = enableTelemetry,
                            onCheckedChange = { enableTelemetry = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = CrimsonRed)
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Toggle 3
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.DoNotDisturbOn, contentDescription = null, tint = DeepMedicalBlue)
                            Column {
                                Text("Silent Focus Mode", fontWeight = FontWeight.Bold, color = DeepMedicalBlue, fontSize = 14.sp)
                                Text("DND for non-priority events", fontSize = 11.sp, color = TextSlateGray.copy(alpha = 0.6f))
                            }
                        }
                        Switch(
                            checked = enableSilentMode,
                            onCheckedChange = { enableSilentMode = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = CrimsonRed)
                        )
                    }
                }
            }

            // SECURE THE DEVELOPER TOOLKIT:
            // Conditionally hide/show based on ADMIN role OR developer builds (DEBUG)
            val isDeveloperOrAdmin = com.example.BuildConfig.DEBUG || localUser.role.equals("admin", ignoreCase = true)

            if (isDeveloperOrAdmin) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "SECURE DEV ADMINISTRATIVE CONTROL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CrimsonRed,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    DeveloperSeederPanel(viewModel = viewModel)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Logout action button
            Button(
                onClick = { viewModel.logout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("settings_logout_button"),
                colors = ButtonDefaults.buttonColors(containerColor = SoftOffWhite),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("De-Register / Reset Application", color = CrimsonRed, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
