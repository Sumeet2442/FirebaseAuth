package com.example.firebaseauth.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebaseauth.R
import com.example.firebaseauth.adapters.MessageAdapter
import com.example.firebaseauth.databinding.FragmentHomeBinding
import com.example.firebaseauth.models.Message
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth:FirebaseAuth
    private lateinit var db:FirebaseFirestore
    private lateinit var msgList: ArrayList<Message>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        auth = Firebase.auth
        db = Firebase.firestore
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        msgList = arrayListOf()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = MessageAdapter(R.layout.message_cards,msgList)
        binding.recyclerView.adapter = adapter
        loadMessages()
    }

    private fun loadMessages(){
        db.collection("messages").get()
            .addOnSuccessListener { data ->
                data.forEach {
                   msgList.add(it.toObject<Message>())
                }
                binding.recyclerView.adapter?.notifyDataSetChanged()
             }
            .addOnFailureListener {
                Snackbar.make(binding.root,it.message.toString(),Snackbar.LENGTH_INDEFINITE).show()
            }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}