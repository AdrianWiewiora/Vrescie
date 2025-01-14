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
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.vresciecompose.authentication.GoogleAuthentication
import com.example.vresciecompose.data.ConversationDao
import com.example.vresciecompose.data.ConversationEntity
import com.example.vresciecompose.data.MessageDao
import com.example.vresciecompose.data.MessageEntity
import com.example.vresciecompose.data.SettingsRepository
import com.example.vresciecompose.data.UserChatPrefs
import com.example.vresciecompose.data.UserChatPrefsDao
import com.example.vresciecompose.data.dataStore
import com.example.vresciecompose.ui.theme.VrescieComposeTheme
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
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    lateinit var database: AppDatabase
    private lateinit var userChatPrefsViewModel: UserChatPrefsViewModel
    private lateinit var registrationViewModel: RegistrationViewModel
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var conversationViewModel: ConversationViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var loadingToAnonymousChatViewModel: LoadingToAnonymousChatViewModel
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(getSharedPreferences("MyPrefs", Context.MODE_PRIVATE))
    }
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    val googleAuthClient by lazy {
        GoogleAuthentication(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeDatabase()
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                !viewModel.isReady.value
            }
        }

        initializeFirebase()
        initializeRepositories()
        initializeViewModels()
        initializePermissionLaunchers()
        askNotificationPermission()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.startDestination.collect { startDestination ->
                    if (startDestination != null) {
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
                                            registrationViewModel = registrationViewModel,
                                            loginViewModel = loginViewModel,
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
                        if (startDestination == Navigation.Destinations.FIRST_LAUNCH) {
                            viewModel.markFirstRunCompleted()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        conversationViewModel.stopMonitoringNetworkConnection(this)
    }

    private fun initializeFirebase() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let { MyFirebaseMessagingService().saveTokenToFirebase(it) }
            }
        }
        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
    }

    private fun initializeDatabase() {
        database = Room.databaseBuilder(
            applicationContext, AppDatabase::class.java, "my-database"
        ).build()
    }

    private fun initializeRepositories() {
        val dataStore = applicationContext.dataStore
        val settingsRepository = SettingsRepository(dataStore)
        settingsViewModel = ViewModelProvider(
            this, SettingsViewModelFactory(settingsRepository)
        )[SettingsViewModel::class.java]
    }

    private fun initializeViewModels() {
        val userChatPrefsDao = database.userChatPrefsDao()
        userChatPrefsViewModel = ViewModelProvider(
            this, UserChatPrefsViewModelFactory(userChatPrefsDao)
        ).get(UserChatPrefsViewModel::class.java)

        val messageDao = database.messageDao()
        val conversationDao = database.conversationDao()
        conversationViewModel = ViewModelProvider(
            this, ConversationViewModelFactory(messageDao, conversationDao)
        ).get(ConversationViewModel::class.java)
        conversationViewModel.monitorNetworkConnection(this)

        registrationViewModel = ViewModelProvider(this).get(RegistrationViewModel::class.java)
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        profileViewModel = ProfileViewModelFactory(applicationContext).create(ProfileViewModel::class.java)
        locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        loadingToAnonymousChatViewModel = ViewModelProvider(this).get(LoadingToAnonymousChatViewModel::class.java)
    }

    private fun initializePermissionLaunchers() {
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {isGranted ->
            if (isGranted) {
                Log.d("Permissions", "Permission granted!")
            } else {
                Log.e("Permissions", "Permission denied.")
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
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