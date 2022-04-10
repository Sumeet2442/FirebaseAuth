package com.example.firebaseauth.ui.login

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.firebaseauth.R
import com.example.firebaseauth.databinding.FragmentGoogelAuthBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class GoogleAuthFragment : Fragment() {
    private val REQ_ONE_TAP = 20
    private var _binding: FragmentGoogelAuthBinding? = null
    private val binding get() = _binding!!
    private lateinit var oneTapClient: SignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var signInRequest: BeginSignInRequest


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGoogelAuthBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        oneTapClient = Identity.getSignInClient(requireActivity())
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.your_web_client_))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            .setAutoSelectEnabled(true)
            .build()
        binding.btnSignIn.setOnClickListener {
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(requireActivity()) { result ->
                    try {
                        startIntentSenderForResult(
                            result.pendingIntent.intentSender, REQ_ONE_TAP,
                            null, 0, 0, 0, null)
                    } catch (e: IntentSender.SendIntentException) {
                        Snackbar.make(binding.root,e.message.toString(),Snackbar.LENGTH_INDEFINITE).show()
                    }
                }
                .addOnFailureListener(requireActivity()) { e ->
                    Snackbar.make(binding.root,e.message.toString(),Snackbar.LENGTH_INDEFINITE).show()
                }
        }
    }

    override fun onStart() {
        super.onStart()
        var currentUser = auth.getCurrentUser()
        updateUI(currentUser);
    }

    private fun updateUI(currentUser: FirebaseUser?) {
       currentUser?.displayName?.let {
           Snackbar.make(binding.root,it,Snackbar.LENGTH_INDEFINITE).show()
       }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val googleCredential = oneTapClient.getSignInCredentialFromIntent(data)
        val idToken = googleCredential.googleIdToken
        when {
            idToken != null -> {
                // Got an ID token from Google. Use it to authenticate
                // with Firebase.
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            val user = auth.currentUser
                            updateUI(user)
                        } else {
                            // If sign in fails, display a message to the user.
                            Snackbar.make(binding.root,task.exception?.message.toString(),Snackbar.LENGTH_INDEFINITE).show()
                            updateUI(null)
                        }
                    }
            }
            else -> {
                // Shouldn't happen.
                Snackbar.make(binding.root,"no token found",Snackbar.LENGTH_INDEFINITE).show()
            }
        }
    }
}