package com.github.shadowsocks

import android.os.{Message, Handler, Bundle}
import android.app.{ProgressDialog, AlertDialog, Activity}
import android.content.{Intent, DialogInterface}
import com.github.shadowsocks.database.{ProfileManager, Profile}
import com.github.shadowsocks.utils.{Parser, Action}
import android.preference.PreferenceManager
import android.view.WindowManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable

class ParserActivity extends Activity {
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    showAsPopup(this)
    val data = getIntent.getData.toString
    new AlertDialog.Builder(this)
      .setTitle(R.string.add_profile_dialog)
      .setCancelable(false)
      .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
      override def onClick(dialog: DialogInterface, id: Int) {
        Parser.parse(data) match {
          case Some(profile) => addProfile(profile)
          case _ => // ignore
        }
        dialog.dismiss()
      }
    })
      .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
      override def onClick(dialog: DialogInterface, id: Int) {
        dialog.dismiss()
        finish()
      }
    })
      .setMessage(data)
      .create()
      .show()
  }

  def showAsPopup(activity: Activity) {
    activity.getWindow.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
      WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    val params = activity.getWindow.getAttributes
    params.alpha = 1.0f
    params.dimAmount = 0.5f
    activity.getWindow.setAttributes(params.asInstanceOf[android.view.WindowManager.LayoutParams])
    activity.getWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT))
  }

  def addProfile(profile: Profile) {

    val h = showProgress(getString(R.string.loading))

    h.postDelayed(new Runnable {
      def run() {
        val profileManager =
          new ProfileManager(PreferenceManager.getDefaultSharedPreferences(getBaseContext),
            getApplication.asInstanceOf[ShadowsocksApplication].dbHelper)
        profileManager.createOrUpdateProfile(profile)
        profileManager.reload(profile.id)
        h.sendEmptyMessage(0)
      }
    }, 600)
  }

  private def showProgress(msg: String): Handler = {
    val progressDialog = ProgressDialog.show(this, "", msg, true, false)
    new Handler {
      override def handleMessage(msg: Message) {
        progressDialog.dismiss()
        finish()
      }
    }
  }

}
