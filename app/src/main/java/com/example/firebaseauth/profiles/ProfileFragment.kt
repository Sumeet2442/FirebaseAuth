package com.example.firebaseauth.profiles

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.firebaseauth.R
import com.example.firebaseauth.databinding.FragmentProfileBinding
import com.example.firebaseauth.models.Profile
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    val REQUEST_IMAGE_GET = 1
    private lateinit var storage: FirebaseStorage
    private var photoUri:Uri ?= null
    private var imgUrl:String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater,container,false)
        val root: View = binding.root
        auth = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSubmit.setOnClickListener {
            updateProfile()
        }
        binding.gifProfile.setOnClickListener {
            val dialog = SelectorDialogFragment()
            dialog.show(childFragmentManager,"choose")
            selectImage()
        }
    }

    private fun uploadImage(){
        val imageName = "dp/"+binding.edtName.text.toString()
        val imgRef = storage.reference.child(imageName+"'s DP")
        val user = auth.currentUser
        var gen = when (binding.rdGrp.checkedRadioButtonId) {
            R.id.rdBtnMale -> "Male"
            R.id.rdBtnFemale -> "Female"
            else -> "null"
        }
        photoUri?.let { imgRef.putFile(it)
            .addOnSuccessListener { task->
                imgRef.downloadUrl.addOnSuccessListener { task ->
                    imgUrl = task.toString()
                    user?.let { it1 -> db.collection("User").document(it1.uid).
                    set(Profile(uid = user.uid,
                        name = binding.edtName.text.toString(),
                        email = binding.edtMail.text.toString(),
                        msg = binding.edtMail.text.toString(),
                        dob = binding.editDate.text.toString(),
                        gender = gen,
                        bio = binding.editBio.text.toString(),
                        img = imgUrl.toString()))
                        }
                }
                Toast.makeText(context,"Image Uploaded",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Snackbar.make(binding.root,it.message.toString(),Snackbar.LENGTH_INDEFINITE).show()
            }
            .addOnProgressListener {
                val uploadProg = it.bytesTransferred/it.totalByteCount
                Log.d("progress value","${uploadProg}%")
            }
        }
    }

    private fun updateProfile() {
        uploadImage()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun selectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_GET) {
            if(resultCode ==Activity.RESULT_OK){
                val thumbnail: Bitmap? = data?.getParcelableExtra("data")
                photoUri = data?.data
                Glide.with(requireActivity()).load(photoUri).into(binding.gifProfile)
            }
            else{
                Toast.makeText(context,"Select an Image",Toast.LENGTH_LONG).show()
            }
        }
    }
    class SelectorDialogFragment: DialogFragment(){

    }
}