package com.example.vresciecompose

import ProvideContext
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.vresciecompose.authentication.GoogleAuthentication
import com.example.vresciecompose.data.ConversationDao
import com.example.vresciecompose.data.ConversationEntity
import com.example.vresciecompose.data.MessageDao
import com.example.vresciecompose.data.MessageEntity
import com.example.vresciecompose.data.UserChatPrefs
import com.example.vresciecompose.data.UserChatPrefsDao
import com.example.vresciecompose.ui.theme.VrescieComposeTheme
import com.example.vresciecompose.view_models.ConfigurationProfileViewModel
import com.example.vresciecompose.view_models.ConversationViewModel
import com.example.vresciecompose.view_models.LoadingToAnonymousChatViewModel
import com.example.vresciecompose.view_models.LocationViewModel
import com.example.vresciecompose.view_models.LoginViewModel
import com.example.vresciecompose.view_models.MainViewModel
import com.example.vresciecompose.view_models.ProfileViewModel
import com.example.vresciecompose.view_models.RegistrationViewModel
import com.example.vresciecompose.view_models.SettingsViewModel
import com.example.vresciecompose.view_models.UserChatPrefsViewModel
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    // Room database
    companion object {
        lateinit var database: AppDatabase
    }

    private lateinit var userChatPrefsViewModel: UserChatPrefsViewModel
    private lateinit var registrationViewModel: RegistrationViewModel
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var configurationProfileViewModel: ConfigurationProfileViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var conversationViewModel: ConversationViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var loadingToAnonymousChatViewModel: LoadingToAnonymousChatViewModel
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(getSharedPreferences("MyPrefs", Context.MODE_PRIVATE))
    }

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var backDispatcher: OnBackPressedDispatcher



    val googleAuthClient by lazy {
        GoogleAuthentication(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }

            // Uzyskaj nowy token rejestracji FCM
            val token = task.result
            // Wywołaj saveTokenToFirebase
            MyFirebaseMessagingService().saveTokenToFirebase(token)
        })
        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance(),
        )

        // Inicjalizacja DataStore
        val dataStore = applicationContext.dataStore
        // Inicjalizacja SettingsRepository
        val settingsRepository = SettingsRepository(dataStore)
        // Utworzenie ViewModel przy użyciu ViewModelFactory
        settingsViewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(settingsRepository)
        )[SettingsViewModel::class.java]

        // Inicjalizacja bazy danych
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "my-database").build()

        // Inicjalizacja ViewModelu z fabryką
        val userChatPrefsDao = database.userChatPrefsDao()
        val viewModelFactory = UserChatPrefsViewModelFactory(userChatPrefsDao)
        userChatPrefsViewModel = ViewModelProvider(this, viewModelFactory).get(UserChatPrefsViewModel::class.java)
        // Inicjalizacja DAO message i conversaion
        val messageDao = database.messageDao()
        val conversationDao = database.conversationDao()
        // Utwórz fabrykę dla ConversationViewModel
        val conversationViewModelFactory = ConversationViewModelFactory(messageDao, conversationDao)
        conversationViewModel = ViewModelProvider(this, conversationViewModelFactory).get(ConversationViewModel::class.java)
        conversationViewModel.monitorNetworkConnection(this)

        backDispatcher = onBackPressedDispatcher

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                !viewModel.isReady.value
            }
        }

        val requestPermissionLauncher1 = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("NotificationPermission", "Permission granted!")
            } else {
                Log.d("NotificationPermission", "Permission denied.")
            }
        }

        fun askNotificationPermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        // Mamy uprawnienie, można wysyłać powiadomienia
                        Log.d("NotificationPermission", "Permission already granted.")
                    }
                    else -> {
                        // Poproś użytkownika o zgodę
                        requestPermissionLauncher1.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
        }
        askNotificationPermission()  // Prośba o zgodę na powiadomienia


        // Inicjalizacja ViewModel
        registrationViewModel = ViewModelProvider(this).get(RegistrationViewModel::class.java)
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        configurationProfileViewModel =
            ViewModelProvider(this).get(ConfigurationProfileViewModel::class.java)
        profileViewModel = ProfileViewModelFactory(applicationContext).create(ProfileViewModel::class.java)
        locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)

        // Inicjalizacja ActivityResultLauncher dla żądania uprawnień
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                // Obsługa braku udzielonych uprawnień
                Log.e("Location", "Location permission denied")
            }
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        loadingToAnonymousChatViewModel = ViewModelProvider(this).get(LoadingToAnonymousChatViewModel::class.java)

        if (viewModel.isReady.value) {
            val startDestination = when {
                viewModel.isFirstRun() -> Navigation.Destinations.FIRST_LAUNCH
                viewModel.isLoggedIn() -> Navigation.Destinations.MAIN_MENU+"/1"
                else -> Navigation.Destinations.START
            }
            setContent {
                VrescieComposeTheme(settingsViewModel = settingsViewModel) {
                    Surface(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val navController = rememberNavController()
                        ProvideContext(context = applicationContext) {
                            AppNavigation(
                                navController,
                                startDestination,
                                googleAuthClient,
                                lifecycleScope,
                                applicationContext = applicationContext,
                                registrationViewModel= registrationViewModel,
                                loginViewModel = loginViewModel,
                                configurationProfileViewModel = configurationProfileViewModel,
                                profileViewModel = profileViewModel,
                                locationViewModel = locationViewModel,
                                requestPermissionLauncher = requestPermissionLauncher,
                                conversationViewModel = conversationViewModel,
                                userChatPrefsViewModel = userChatPrefsViewModel,
                                settingsViewModel = settingsViewModel,
                                loadingToAnonymousChatViewModel = loadingToAnonymousChatViewModel
                            )
                        }
                    }
                }
            }
            // Ustawienie isFirstRun na false dopiero po ustawieniu setContent na FirstLaunch
            if (startDestination == Navigation.Destinations.FIRST_LAUNCH) {
                applicationContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("isFirstRun", false)
                    .apply()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        conversationViewModel.stopMonitoringNetworkConnection(this)
    }
}

class MainViewModelFactory(private val sharedPreferences: SharedPreferences) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


@Database(entities = [UserChatPrefs::class, ConversationEntity::class, MessageEntity::class], version = 6)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userChatPrefsDao(): UserChatPrefsDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}


class UserChatPrefsViewModelFactory(private val userChatPrefsDao: UserChatPrefsDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserChatPrefsViewModel::class.java)) {
            return UserChatPrefsViewModel(userChatPrefsDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ConversationViewModelFactory(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConversationViewModel::class.java)) {
            return ConversationViewModel(messageDao, conversationDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SettingsViewModelFactory(
    private val repository: SettingsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ProfileViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(appContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}