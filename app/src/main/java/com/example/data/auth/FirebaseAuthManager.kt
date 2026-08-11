package com.example.data.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseAuthManager(private val context: android.content.Context? = null) {

    private val auth: FirebaseAuth? by lazy {
        try {
            if (context != null && com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
                try {
                    com.google.firebase.FirebaseApp.initializeApp(context)
                } catch (e: Exception) {
                    Log.i("FirebaseAuthManager", "FirebaseApp initializeApp note: ${e.message}")
                }
            }
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.i("FirebaseAuthManager", "Firebase Auth status note: ${e.message}")
            null
        }
    }

    val currentUser: FirebaseUser?
        get() = auth?.currentUser

    fun getAuthStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        val currentAuth = auth
        if (currentAuth != null) {
            currentAuth.addAuthStateListener(listener)
            trySend(currentAuth.currentUser)
        } else {
            trySend(null)
        }
        awaitClose {
            currentAuth?.removeAuthStateListener(listener)
        }
    }

    fun signInWithEmail(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        val currentAuth = auth
        if (currentAuth == null) {
            onResult(false, "Firebase Auth not initialized in this environment.")
            return
        }
        currentAuth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Sign in failed")
                }
            }
    }

    fun registerWithEmail(
        displayName: String,
        email: String,
        pass: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val currentAuth = auth
        if (currentAuth == null) {
            onResult(false, "Firebase Auth not initialized in this environment.")
            return
        }
        currentAuth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = currentAuth.currentUser
                    if (user != null && displayName.isNotBlank()) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName)
                            .build()
                        user.updateProfile(profileUpdates).addOnCompleteListener {
                            onResult(true, null)
                        }
                    } else {
                        onResult(true, null)
                    }
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Registration failed")
                }
            }
    }

    fun signInAnonymously(onResult: (Boolean, String?) -> Unit) {
        val currentAuth = auth
        if (currentAuth == null) {
            onResult(false, "Firebase Auth not initialized in this environment.")
            return
        }
        currentAuth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Anonymous sign in failed")
                }
            }
    }

    fun sendPasswordReset(email: String, onResult: (Boolean, String?) -> Unit) {
        val currentAuth = auth
        if (currentAuth == null) {
            onResult(false, "Firebase Auth not initialized in this environment.")
            return
        }
        currentAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Password reset email failed")
                }
            }
    }

    fun updateUserProfile(
        displayName: String,
        email: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = auth?.currentUser
        if (user == null) {
            onResult(false, "No active Firebase user session.")
            return
        }

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (email.isNotBlank() && email != user.email) {
                    user.updateEmail(email).addOnCompleteListener { emailTask ->
                        if (emailTask.isSuccessful) {
                            onResult(true, null)
                        } else {
                            onResult(true, "Profile updated, but email update required re-authentication: ${emailTask.exception?.localizedMessage}")
                        }
                    }
                } else {
                    onResult(true, null)
                }
            } else {
                onResult(false, task.exception?.localizedMessage ?: "Profile update failed")
            }
        }
    }

    fun sendEmailVerification(onResult: (Boolean, String?) -> Unit) {
        val user = auth?.currentUser
        if (user == null) {
            onResult(false, "No active user")
            return
        }
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(true, null)
            } else {
                onResult(false, task.exception?.localizedMessage ?: "Verification email failed")
            }
        }
    }

    fun signOut() {
        auth?.signOut()
    }
}
