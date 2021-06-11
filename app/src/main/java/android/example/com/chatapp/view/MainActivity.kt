package android.example.com.chatapp.view

import android.Manifest
import android.content.pm.PackageManager
import android.example.com.chatapp.R
import android.example.com.chatapp.util.CHAT_FRAGMENT
import android.example.com.chatapp.util.CHAT_FRAGMENT_CODE
import android.example.com.chatapp.util.PERMISSION_CAMERA
import android.example.com.chatapp.util.PERMISSION_STORAGE
import android.example.com.chatapp.view.menu.ProfileFragment
import android.example.com.chatapp.view.pagerFragments.CameraFragment
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomsheet.BottomSheetBehavior

class MainActivity : FragmentActivity() {

    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        navController = navHostFragment.navController

    }



    fun checkGalleryPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Media permission")
                    .setMessage("This app requires access to your media files")
                    .setPositiveButton("Ask me") { dialog, which ->
                        requestGalleryPermission()
                    }
                    .setNegativeButton("No") { dialog, which ->
                        notifyFragment(false)
                    }.show()
            } else {
                requestGalleryPermission()
            }
        } else {
            notifyFragment(true)
        }
    }
    private fun requestGalleryPermission() {

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSION_STORAGE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    notifyFragment(true)
                }
            }
            PERMISSION_STORAGE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                notifyFragment(true)

            }
        }
    }

    private fun notifyFragment(permissionGranted: Boolean) {
        val navHost = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        navHost?.let { navFragment ->
            navFragment.childFragmentManager.primaryNavigationFragment?.let { fragment ->
                if (fragment is ProfileFragment) {
                    fragment.onPermissionResult(permissionGranted)
                }
            }
        }
    }

}
