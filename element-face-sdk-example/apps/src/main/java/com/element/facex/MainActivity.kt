package com.element.facex

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.element.camera.ElementFaceAuthActivity
import com.element.camera.ElementFaceEnrollActivity
import com.element.camera.ProviderUtil
import com.element.camera.UserInfo
import com.element.common.PermissionUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            val userDataFormFragment = UserDataFormFragment()
            userDataFormFragment.setMainActivity(this@MainActivity)
            userDataFormFragment.show(supportFragmentManager, null)

            val list = ProviderUtil.getUsers(baseContext, packageName, "")
            if (list.isNotEmpty()) {
                toastMessage(getString(R.string.msg_already_enrolled, list[0].fullName))
            }
        }

        userName.setOnClickListener {
            val list = ProviderUtil.getUsers(baseContext, packageName, "")
            if (list.isEmpty()) {
                showMessage(getString(R.string.enroll_first))
            } else {
                startAuth(list[0].userId)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        PermissionUtils.verifyPermissions(
            this,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val list = ProviderUtil.getUsers(baseContext, packageName, "")
        if (list.isEmpty()) {
            userName.visibility = GONE
        } else {
            userName.visibility = VISIBLE
            userName.text = list[0].fullName
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ENROLL_REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                showMessage(getString(R.string.enroll_completed))
            } else {
                showMessage(getString(R.string.enroll_cancelled))
            }
        } else if (requestCode == AUTH_REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val userId = data?.getStringExtra(ElementFaceAuthActivity.EXTRA_ELEMENT_USER_ID)
                val userInfo = ProviderUtil.getUser(baseContext, packageName, userId!!)
                val results = data.getStringExtra(ElementFaceAuthActivity.EXTRA_RESULTS)
                val message: String
                message = when (results) {
                    ElementFaceAuthActivity.USER_VERIFIED -> getString(
                        R.string.msg_verified,
                        userInfo.fullName
                    )
                    ElementFaceAuthActivity.USER_FAKE -> getString(R.string.msg_fake)
                    else -> getString(R.string.msg_not_verified)
                }
                showMessage(message)
            } else {
                showMessage(getString(R.string.auth_cancelled))
            }
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(
            findViewById(R.id.constraintLayout),
            message, Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun toastMessage(message: String) {
        Toast.makeText(
            baseContext,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    protected fun startEnroll(userId: String) {
        val intent = Intent(this@MainActivity, ElementFaceEnrollActivity::class.java)
        intent.putExtra(ElementFaceEnrollActivity.EXTRA_ELEMENT_USER_ID, userId)
        intent.putExtra(ElementFaceEnrollActivity.EXTRA_LIVENESS_DETECTION, true)
        intent.putExtra(ElementFaceEnrollActivity.EXTRA_TUTORIAL, true)
        startActivityForResult(intent, ENROLL_REQ_CODE)
    }

    protected fun startAuth(userId: String) {
        val intent = Intent(this@MainActivity, ElementFaceAuthActivity::class.java)
        intent.putExtra(ElementFaceAuthActivity.EXTRA_ELEMENT_USER_ID, userId)
        intent.putExtra(ElementFaceEnrollActivity.EXTRA_LIVENESS_DETECTION, true)
        intent.putExtra(ElementFaceEnrollActivity.EXTRA_TUTORIAL, true)
        startActivityForResult(intent, AUTH_REQ_CODE)
    }

    companion object {
        @JvmStatic
        val ENROLL_REQ_CODE = 12800

        @JvmStatic
        val AUTH_REQ_CODE = 12801
    }
}
