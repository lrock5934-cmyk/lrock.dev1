package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.ui.theme.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

fun getSafeAuth(context: android.content.Context): FirebaseAuth {
  try {
    if (FirebaseApp.getApps(context).isEmpty()) {
      val options = com.google.firebase.FirebaseOptions.Builder()
        .setApiKey("AIzaSyCTlO36mqxI7z-aAWrEIDBMmflWq-6PKEg")
        .setApplicationId("1:453694868322:android:f2b48a6537320689b63a3b")
        .setProjectId("timeless-13c21")
        .setStorageBucket("timeless-13c21.firebasestorage.app")
        .build()
      FirebaseApp.initializeApp(context, options)
    }
  } catch (e: Exception) {
    try {
      FirebaseApp.initializeApp(context)
    } catch (ignored: Exception) {}
  }
  return FirebaseAuth.getInstance()
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    FirebaseApp.initializeApp(this)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          var currentUser by remember { mutableStateOf<com.google.firebase.auth.FirebaseUser?>(null) }

          LaunchedEffect(Unit) {
            try {
              currentUser = getSafeAuth(this@MainActivity).currentUser
            } catch (e: Exception) {
              currentUser = null
            }
          }

          if (currentUser == null) {
            AuthScreen(onAuthSuccess = { 
              try {
                currentUser = getSafeAuth(this@MainActivity).currentUser
              } catch (e: Exception) {
                currentUser = null
              }
            })
          } else {
            MainScreen(onLogout = {
              try {
                getSafeAuth(this@MainActivity).signOut()
              } catch (e: Exception) {}
              currentUser = null
            })
          }
        }
      }
    }
  }
}

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
  val context = LocalContext.current
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var isSignUp by remember { mutableStateOf(false) }
  var isLoading by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(DeepNavy)
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(DeepNavy),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(id = R.drawable.lrock_logo),
            contentDescription = "LRock Logo",
            modifier = Modifier
              .size(72.dp)
              .clip(CircleShape)
          )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = if (isSignUp) "Create LRock.dev Account" else "Sign In to LRock.dev",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Web & App Development in Accra, Ghana",
          style = MaterialTheme.typography.bodySmall,
          color = TextMuted,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Email Address") },
          leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text("Password") },
          leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
          visualTransformation = PasswordVisualTransformation(),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = {
            if (email.isBlank() || password.isBlank()) {
              Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
              return@Button
            }
            isLoading = true
            val auth = getSafeAuth(context)
            if (isSignUp) {
              auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                  if (task.isSuccessful) {
                    isLoading = false
                    Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                    onAuthSuccess()
                  } else {
                    auth.signInWithEmailAndPassword(email, password)
                      .addOnCompleteListener { loginTask ->
                        isLoading = false
                        Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                        onAuthSuccess()
                      }
                  }
                }
            } else {
              auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                  isLoading = false
                  Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                  onAuthSuccess()
                }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TealDark)
        ) {
          if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
          } else {
            Text(
              text = if (isSignUp) "Sign Up" else "Sign In",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
          onClick = { isSignUp = !isSignUp },
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = if (isSignUp) "Already have an account? Sign In" else "Need an account? Sign Up",
            color = TealDark
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
          onClick = {
            getSafeAuth(context).signInAnonymously()
              .addOnCompleteListener {
                Toast.makeText(context, "Entered as Guest", Toast.LENGTH_SHORT).show()
                onAuthSuccess()
              }
          },
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Continue as Guest", color = TextMuted)
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onLogout: () -> Unit) {
  val context = LocalContext.current
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  // Contact form state
  var name by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var message by remember { mutableStateOf("") }
  var showSuccessDialog by remember { mutableStateOf(false) }

  // Estimator state
  var selectedServiceType by remember { mutableStateOf("Custom Websites") }
  var timelineWeeks by remember { mutableStateOf(4f) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(DeepNavy),
              contentAlignment = Alignment.Center
            ) {
              Image(
                painter = painterResource(id = R.drawable.lrock_logo),
                contentDescription = "LRock Logo",
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "LRock.dev",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
              )
              Text(
                text = "Accra, Ghana 🇬🇭",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
              )
            }
          }
        },
        actions = {
          IconButton(onClick = onLogout) {
            Icon(Icons.Default.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
          }
          FilledTonalButton(
            onClick = {
              coroutineScope.launch {
                listState.animateScrollToItem(4)
              }
            },
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = TealAccent.copy(alpha = 0.15f),
              contentColor = TealDark
            ),
            modifier = Modifier.padding(end = 12.dp).testTag("header_quote_button")
          ) {
            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Get a Quote", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      Surface(
        color = DeepNavy,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            TextButton(onClick = { coroutineScope.launch { listState.animateScrollToItem(1) } }) {
              Text("Services", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            }
            TextButton(onClick = { coroutineScope.launch { listState.animateScrollToItem(2) } }) {
              Text("How it works", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            }
            TextButton(onClick = { coroutineScope.launch { listState.animateScrollToItem(4) } }) {
              Text("Contact", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            }
            TextButton(onClick = { coroutineScope.launch { listState.animateScrollToItem(4) } }) {
              Text("Get a Quote", color = TealAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
          Divider(color = Color.White.copy(alpha = 0.15f), thickness = 1.dp)
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "© 2026 LRock.dev · Accra, Ghana",
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
          )
        }
      }
    }
  ) { innerPadding ->
    LazyColumn(
      state = listState,
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentPadding = PaddingValues(bottom = 32.dp)
    ) {
      item {
        HeroSection(
          onGetStartedClick = { coroutineScope.launch { listState.animateScrollToItem(4) } },
          onExploreClick = { coroutineScope.launch { listState.animateScrollToItem(1) } }
        )
      }
      item {
        ServicesSection()
      }
      item {
        HowItWorksSection()
      }
      item {
        EstimatorSection(
          selectedService = selectedServiceType,
          onServiceSelected = { selectedServiceType = it },
          timelineWeeks = timelineWeeks,
          onTimelineChanged = { timelineWeeks = it },
          onRequestCustomQuote = {
            message = "Hi LRock, I'm interested in a $selectedServiceType project with an estimated timeline of ${timelineWeeks.toInt()} weeks."
            coroutineScope.launch { listState.animateScrollToItem(4) }
          }
        )
      }
      item {
        ContactSection(
          name = name,
          onNameChanged = { name = it },
          phone = phone,
          onPhoneChanged = { phone = it },
          message = message,
          onMessageChanged = { message = it },
          onSubmit = {
            if (name.isBlank() || phone.isBlank() || message.isBlank()) {
              Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
              showSuccessDialog = true
            }
          },
          onCallClick = {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0536759904"))
            context.startActivity(intent)
          },
          onEmailClick = {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:lrock.dev5933@gmail.com?subject=Project Inquiry&body=Hi LRock.dev,"))
            context.startActivity(intent)
          },
          onWhatsappClick = {
            val url = "https://wa.me/233536759904?text=Hi%20LRock.dev,%20I'd%20like%20to%20discuss%20a%20project."
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
          }
        )
      }
    }
  }

  if (showSuccessDialog) {
    AlertDialog(
      onDismissRequest = { showSuccessDialog = false },
      icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TealAccent, modifier = Modifier.size(48.dp)) },
      title = { Text("Message Sent Successfully!", textAlign = TextAlign.Center) },
      text = {
        Text(
          "Thank you, $name! LRock.dev has received your message and will reach out via phone/WhatsApp ($phone) or email shortly.",
          textAlign = TextAlign.Center
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showSuccessDialog = false
            name = ""
            phone = ""
            message = ""
          },
          colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
        ) {
          Text("Done", color = Color.White)
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = {
            showSuccessDialog = false
            val encodedMsg = Uri.encode(message)
            val url = "https://wa.me/233536759904?text=$encodedMsg"
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
          }
        ) {
          Text("Open WhatsApp", color = TealDark)
        }
      }
    )
  }
}

@Composable
fun HeroSection(onGetStartedClick: () -> Unit, onExploreClick: () -> Unit) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(
        Brush.verticalGradient(
          colors = listOf(DeepNavy, SlateNavy)
        )
      )
      .padding(horizontal = 24.dp, vertical = 48.dp)
  ) {
    Column(
      horizontalAlignment = Alignment.Start,
      modifier = Modifier.fillMaxWidth()
    ) {
      Surface(
        color = TealAccent.copy(alpha = 0.15f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, TealAccent.copy(alpha = 0.3f))
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.LocationOn, contentDescription = null, tint = TealAccent, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Accra, Ghana · Available Worldwide",
            color = TealAccent,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "Crafting High-Performance Websites & Mobile Apps",
        style = MaterialTheme.typography.headlineLarge.copy(
          fontWeight = FontWeight.ExtraBold,
          lineHeight = 40.sp
        ),
        color = Color.White
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "We build lightning-fast, scalable, and user-centric digital products that elevate your brand and accelerate business growth.",
        style = MaterialTheme.typography.bodyLarge,
        color = Color.White.copy(alpha = 0.8f),
        lineHeight = 24.sp
      )

      Spacer(modifier = Modifier.height(32.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Button(
          onClick = onGetStartedClick,
          colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
          modifier = Modifier
            .weight(1f)
            .height(50.dp)
            .testTag("hero_start_project_button")
        ) {
          Text("Start a Project", color = DeepNavy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Spacer(modifier = Modifier.width(8.dp))
          Icon(Icons.Default.ArrowForward, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(18.dp))
        }

        OutlinedButton(
          onClick = onExploreClick,
          border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
          modifier = Modifier
            .weight(1f)
            .height(50.dp)
            .testTag("hero_explore_services_button")
        ) {
          Text("Explore Services", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        MetricItem(value = "100%", label = "On-Time Delivery")
        MetricItem(value = "Lightning", label = "Fast Performance")
        MetricItem(value = "24/7", label = "Dedicated Support")
      }
    }
  }
}

@Composable
fun MetricItem(value: String, label: String) {
  Column {
    Text(
      text = value,
      color = TealAccent,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = label,
      color = Color.White.copy(alpha = 0.7f),
      style = MaterialTheme.typography.bodySmall
    )
  }
}

@Composable
fun ServicesSection() {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 20.dp, vertical = 40.dp)
  ) {
    Text(
      text = "OUR EXPERTISE",
      color = TealDark,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.5.sp
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "Services",
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "Tailored digital solutions engineered for performance, security, and exceptional user experience.",
      style = MaterialTheme.typography.bodyMedium,
      color = TextMuted
    )

    Spacer(modifier = Modifier.height(24.dp))

    val services = listOf(
      ServiceItem(
        title = "Custom Websites",
        desc = "High-performance web apps, landing pages, and e-commerce platforms built with modern tech stacks.",
        icon = Icons.Outlined.Language
      ),
      ServiceItem(
        title = "Mobile Apps",
        desc = "Native & cross-platform Android & iOS apps with slick UIs and robust backend architecture.",
        icon = Icons.Outlined.PhoneAndroid
      ),
      ServiceItem(
        title = "UI/UX Design",
        desc = "User-centric design systems, intuitive wireframes, and gorgeous interactive prototypes.",
        icon = Icons.Outlined.DesignServices
      ),
      ServiceItem(
        title = "Ongoing Support",
        desc = "Dedicated maintenance, security updates, feature scaling, and lightning-fast performance tuning.",
        icon = Icons.Outlined.SupportAgent
      )
    )

    services.forEachIndexed { index, service ->
      FadeInBox(delayMillis = index * 80) {
        Column {
          ServiceCard(service)
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }
}

@Composable
fun FadeInBox(delayMillis: Int = 0, content: @Composable () -> Unit) {
  var visible by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    kotlinx.coroutines.delay(delayMillis.toLong())
    visible = true
  }
  AnimatedVisibility(
    visible = visible,
    enter = fadeIn(animationSpec = tween(400)) + slideInVertically(
      animationSpec = tween(400),
      initialOffsetY = { it / 3 }
    )
  ) {
    content()
  }
}

data class ServiceItem(val title: String, val desc: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun ServiceCard(service: ServiceItem) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    shape = RoundedCornerShape(16.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(52.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(TealAccent.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = service.icon,
          contentDescription = null,
          tint = TealDark,
          modifier = Modifier.size(28.dp)
        )
      }
      Spacer(modifier = Modifier.width(16.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = service.title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = service.desc,
          style = MaterialTheme.typography.bodyMedium,
          color = TextMuted,
          lineHeight = 20.sp
        )
      }
    }
  }
}

@Composable
fun HowItWorksSection() {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface)
      .padding(horizontal = 20.dp, vertical = 40.dp)
  ) {
    Text(
      text = "OUR PROCESS",
      color = TealDark,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.5.sp
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "How It Works",
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "A simple, transparent 4-step workflow from initial idea to successful production launch.",
      style = MaterialTheme.typography.bodyMedium,
      color = TextMuted
    )

    Spacer(modifier = Modifier.height(24.dp))

    val steps = listOf(
      ProcessStep("01", "Discovery", "Understanding your vision, target audience, technical requirements, and project goals."),
      ProcessStep("02", "Design", "Crafting intuitive wireframes, visual mockups, and interactive user flows."),
      ProcessStep("03", "Development", "Writing clean, scalable code with rigorous testing and regular milestone updates."),
      ProcessStep("04", "Launch & Support", "Deploying your product to production with ongoing care, optimization, and scaling.")
    )

    steps.forEachIndexed { index, step ->
      FadeInBox(delayMillis = index * 80) {
        Column {
          ProcessCard(step)
          Spacer(modifier = Modifier.height(12.dp))
        }
      }
    }
  }
}

data class ProcessStep(val number: String, val title: String, val desc: String)

@Composable
fun ProcessCard(step: ProcessStep) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
    shape = RoundedCornerShape(12.dp),
    border = BorderStroke(1.dp, BorderColor)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(DeepNavy),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = step.number,
          color = TealAccent,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp
        )
      }
      Spacer(modifier = Modifier.width(16.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = step.title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = step.desc,
          style = MaterialTheme.typography.bodySmall,
          color = TextMuted,
          lineHeight = 18.sp
        )
      }
    }
  }
}

@Composable
fun EstimatorSection(
  selectedService: String,
  onServiceSelected: (String) -> Unit,
  timelineWeeks: Float,
  onTimelineChanged: (Float) -> Unit,
  onRequestCustomQuote: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 20.dp, vertical = 40.dp)
  ) {
    Text(
      text = "QUICK ESTIMATOR",
      color = TealDark,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.5.sp
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "Estimate Your Project",
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "Select your service and desired timeline to get an instant scope preview.",
      style = MaterialTheme.typography.bodyMedium,
      color = TextMuted
    )

    Spacer(modifier = Modifier.height(20.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(text = "Select Service", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(10.dp))

        val services = listOf("Custom Websites", "Mobile Apps", "UI/UX Design", "Ongoing Support")
        val servicePrices = mapOf("Custom Websites" to "$1,200+", "Mobile Apps" to "$2,500+", "UI/UX Design" to "$800+", "Ongoing Support" to "$400/mo")

        services.forEach { service ->
          val isSelected = selectedService == service
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              .clickable { onServiceSelected(service) },
            color = if (isSelected) TealAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, if (isSelected) TealAccent else BorderColor)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                  selected = isSelected,
                  onClick = { onServiceSelected(service) },
                  colors = RadioButtonDefaults.colors(selectedColor = TealDark)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = service, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
              }
              Text(
                text = servicePrices[service] ?: "",
                fontWeight = FontWeight.Bold,
                color = TealDark,
                fontSize = 14.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider(color = BorderColor)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Estimated Timeline: ${timelineWeeks.toInt()} Weeks",
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        Slider(
          value = timelineWeeks,
          onValueChange = onTimelineChanged,
          valueRange = 1f..12f,
          steps = 11,
          colors = SliderDefaults.colors(
            thumbColor = TealDark,
            activeTrackColor = TealAccent
          )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = onRequestCustomQuote,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
        ) {
          Icon(Icons.Default.Send, contentDescription = null, tint = TealAccent, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Discuss This Estimate", color = Color.White, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun ContactSection(
  name: String,
  onNameChanged: (String) -> Unit,
  phone: String,
  onPhoneChanged: (String) -> Unit,
  message: String,
  onMessageChanged: (String) -> Unit,
  onSubmit: () -> Unit,
  onCallClick: () -> Unit,
  onEmailClick: () -> Unit,
  onWhatsappClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(DeepNavy)
      .padding(horizontal = 20.dp, vertical = 48.dp)
  ) {
    Text(
      text = "GET IN TOUCH",
      color = TealAccent,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.5.sp
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "Contact LRock.dev",
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
      color = Color.White
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = "Ready to transform your digital presence? Reach out today to discuss your next web or mobile app project.",
      style = MaterialTheme.typography.bodyMedium,
      color = Color.White.copy(alpha = 0.8f)
    )

    Spacer(modifier = Modifier.height(24.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Button(
        onClick = onCallClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(containerColor = SlateNavy),
        border = BorderStroke(1.dp, TealAccent.copy(alpha = 0.4f))
      ) {
        Icon(Icons.Default.Phone, contentDescription = null, tint = TealAccent, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Call", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
      }

      Button(
        onClick = onWhatsappClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
      ) {
        Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("WhatsApp", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
      }

      Button(
        onClick = onEmailClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(containerColor = SlateNavy),
        border = BorderStroke(1.dp, TealAccent.copy(alpha = 0.4f))
      ) {
        Icon(Icons.Default.Email, contentDescription = null, tint = TealAccent, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Email", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = SlateNavy),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Phone, contentDescription = null, tint = TealAccent, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text(text = "Call: 0536759904", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Email, contentDescription = null, tint = TealAccent, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text(text = "Email: lrock.dev5933@gmail.com", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("contact_form_card"),
      colors = CardDefaults.cardColors(containerColor = SurfaceLight),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "Send a Direct Inquiry",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = TextDark
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = name,
          onValueChange = onNameChanged,
          label = { Text("Your Name") },
          leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("contact_name_input"),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = phone,
          onValueChange = onPhoneChanged,
          label = { Text("Phone or WhatsApp") },
          leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("contact_phone_input"),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = message,
          onValueChange = onMessageChanged,
          label = { Text("Project Message") },
          leadingIcon = { Icon(Icons.Default.Message, contentDescription = null) },
          modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .testTag("contact_message_input"),
          maxLines = 5
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = onSubmit,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("contact_send_button"),
          colors = ButtonDefaults.buttonColors(containerColor = TealDark)
        ) {
          Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Send Message", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
      }
    }
  }
}
