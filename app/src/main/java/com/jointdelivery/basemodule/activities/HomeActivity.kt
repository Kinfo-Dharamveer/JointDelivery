package com.jointdelivery.basemodule.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jointdelivery.MyApplication
import com.jointdelivery.R
import com.jointdelivery.appviewmodule.NotificationsActivity
import com.jointdelivery.auth.AuthManager
import com.jointdelivery.fragments.homeFragments.*
import com.jointdelivery.interfaces.SettingClickListener
import com.jointdelivery.utilities.CommonUtil
import com.jointdelivery.utilities.ConnectionManager
import com.polyak.iconswitch.IconSwitch
import com.sendbird.android.SendBird
import com.sendbird.android.SendBirdException
import kotlinx.android.synthetic.main.home_activity_layout.*
import kotlinx.android.synthetic.main.tool_bar_layout.*
import javax.inject.Inject


open class HomeActivity : AppCompatActivity() {
    lateinit var settingClickListener: SettingClickListener

    var ismapView = false
    @Inject
    lateinit var authmanager: AuthManager;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity_layout)
        (application as MyApplication).getAppComponent()?.inject(this)

        back_button.setOnClickListener { onBackPressed() }
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        toolbar_title.text = (resources.getString(R.string.home_title))
        setFragment(YourRoutesFragment(), true)



        /* toggle_map.visibility = View.VISIBLE
         toggle_list.visibility = View.VISIBLE

         toggle_map.setOnClickListener {
             include_toolbar.visibility = View.VISIBLE
             search_view.visibility = View.GONE
             profile_setting.visibility = View.GONE
             toggle_map.visibility = View.VISIBLE
             toolbar_title.text = (resources.getString(R.string.home_title))
             setFragment(HomeFragment(), true)


         }
         toggle_list.setOnClickListener {
             include_toolbar.visibility = View.VISIBLE
             search_view.visibility = View.GONE
             profile_setting.visibility = View.GONE
             toggle_map.visibility = View.VISIBLE
             toolbar_title.text = (resources.getString(R.string.home_title))
             setFragment(YourRoutesFragment(), true)
         }*/

        icon_switch.setBackgroundColor(resources.getColor(R.color.green_dark))
        icon_switch.setThumbColorLeft(resources.getColor(R.color.colorIcons))
        icon_switch.setThumbColorRight(resources.getColor(R.color.colorIcons))
        icon_switch.setActiveTintIconLeft(resources.getColor(R.color.colorAccent))
        icon_switch.setInactiveTintIconLeft(resources.getColor(R.color.colorIcons))
        icon_switch.setActiveTintIconRight(resources.getColor(R.color.colorAccent))
        icon_switch.setInactiveTintIconRight(resources.getColor(R.color.colorIcons))




        icon_switch.setCheckedChangeListener { current ->
            if (current === IconSwitch.Checked.LEFT) { // Map Clicked
                include_toolbar.visibility = View.VISIBLE
                search_view.visibility = View.GONE
                profile_setting.visibility = View.GONE
                // toggle_map.visibility = View.VISIBLE
                toolbar_title.text = (resources.getString(R.string.home_title))
                setFragment(YourRoutesFragment(), true)

            } else { // List
                include_toolbar.visibility = View.VISIBLE
                search_view.visibility = View.GONE
                profile_setting.visibility = View.GONE
                // toggle_map.visibility = View.VISIBLE
                toolbar_title.text = (resources.getString(R.string.home_title))
                setFragment(HomeFragment(), true)

            }
        }

        profile_setting.setOnClickListener {
            settingClickListener.onSettingClick()
        }
    }

    fun hideIconSwitch(){

      icon_switch.visibility = View.INVISIBLE

    }

    fun showIconSwitch(){

        icon_switch.visibility = View.VISIBLE

    }

    private val mOnNavigationItemSelectedListener = object : BottomNavigationView.OnNavigationItemSelectedListener {

        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            back_button.visibility = View.VISIBLE
            when (item.itemId) {
                R.id.action_assigned -> {

                    if (icon_switch.getChecked()==IconSwitch.Checked.LEFT){
                        openYourRoutesFrag()

                    } else{

                        include_toolbar.visibility = View.VISIBLE
                        search_view.visibility = View.GONE
                        profile_setting.visibility = View.GONE
                        showIconSwitch()
                        //toggle_map.visibility = View.VISIBLE
                        toolbar_title.text = (resources.getString(R.string.assigned_title))
                        // toggle_list.visibility = View.VISIBLE
                        setFragment(HomeFragment(), true)
                    }

                    return true
                }
//                R.id.action_assigned -> {
//                    include_toolbar.visibility = View.VISIBLE
//                    search_view.visibility = View.GONE
//                    profile_setting.visibility = View.GONE
//                    toolbar_title.text = resources.getString(R.string.assigned_title)
//                    toggle_map.visibility = View.GONE
//
//                    toggle_list.visibility = View.GONE
//                    setFragment(HomeFragment(), true)
//                    return true
//                }
                R.id.action_completed -> {


                        profile_setting.visibility = View.GONE
                        search_view.visibility = View.GONE
                        showIconSwitch()

                        //toggle_map.visibility = View.GONE
                        // toggle_list.visibility = View.GONE
                        toolbar_title.text = resources.getString(R.string.complteted_title)
                        setFragment(CompletedFragment(), true)

                    return true
                }
                R.id.action_messages -> {


                        profile_setting.visibility = View.GONE
                        search_view.visibility = View.GONE
                        showIconSwitch()

                        toolbar_title.text = resources.getString(R.string.messages)
                        // toggle_map.visibility = View.GONE
                        // toggle_list.visibility = View.GONE
                        setFragment(MessagesFragmenrt(), true)




                    return true
                }
                R.id.action_profile
                -> {
                    profile_setting.visibility = View.VISIBLE
                    search_view.visibility = View.GONE
                    back_button.visibility = View.GONE
                    toolbar_title.text = resources.getString(R.string.profile)
                    hideIconSwitch()
                   // toggle_map.visibility = View.GONE
                   // toggle_list.visibility = View.GONE
                    toolbar_title.visibility = View.VISIBLE
                    setFragment(ProfileFragment(), true)
                    return true
                }
            }
            return false
        }
    }


    fun openYourRoutesFrag(){
        include_toolbar.visibility = View.VISIBLE
        search_view.visibility = View.GONE
        profile_setting.visibility = View.GONE
        // toggle_map.visibility = View.VISIBLE
        toolbar_title.text = (resources.getString(R.string.home_title))
        setFragment(YourRoutesFragment(), true)
    }


    fun setListener(listener: SettingClickListener) {
        this.settingClickListener = listener
    }

    private fun setFragment(fragment: Fragment, addtoBackStack: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container_home_activity, fragment)
        transaction.commit()
    }


    override fun onBackPressed() {

        AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("Closing Application")
            .setMessage("Are you sure you want to close this activity?")
            .setPositiveButton("Yes") { dialog, which ->
                if (supportFragmentManager.getBackStackEntryCount() > 0) {
                    supportFragmentManager.popBackStack();
                } else {
                    super.onBackPressed();
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        val item = menu.findItem(R.id.action_search)
        search_view.setMenuItem(item)

        return true
    }
}