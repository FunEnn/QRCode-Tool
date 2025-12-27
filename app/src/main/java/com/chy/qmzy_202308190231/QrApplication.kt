package com.chy.qmzy_202308190231

import android.app.Application
import com.chy.qmzy_202308190231.di.AppContainer

/**
 * Main application class for the QR application.
 * This class extends the base Application class and serves as the entry point
 * for the application. It initializes and provides access to the app container.
 */
class QrApplication : Application() {
    // Lazy initialization of the application container
    // This container holds all the dependencies and components needed by the app
    lateinit var appContainer: AppContainer
        private set  // Private setter ensures the container can only be set from within this class

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * This is where we initialize our app container.
     */
    override fun onCreate() {
        super.onCreate()  // Call the superclass's implementation first
        appContainer = AppContainer()  // Initialize the app container with all dependencies
    }
}
