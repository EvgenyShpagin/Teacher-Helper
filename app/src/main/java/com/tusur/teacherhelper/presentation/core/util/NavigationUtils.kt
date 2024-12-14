package com.tusur.teacherhelper.presentation.core.util

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.forEach
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.FloatingWindow
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import java.lang.ref.WeakReference

/**
 * Sets up to the fragment's Toolbar AppBarConfiguration of MainActivity
 * to use its DrawerLayout
 */
fun Fragment.setupTopLevelAppBarConfiguration(toolbar: Toolbar) {
    val navController = findNavController()
    val appBarConfigurationProvider = requireActivity() as? AppBarConfigurationProvider ?: return
    toolbar.setupWithNavController(
        navController = navController,
        configuration = appBarConfigurationProvider.appBarConfiguration
    )
}

/**
 * Sets up a NavigationView for use with a NavController.
 * This will call onNavDestinationSelected when a menu item is selected.
 *
 * The selected item in the NavigationView will automatically be updated when the destination changes.
 *
 * (Custom implementation allowing to avoid lagging
 * when closing DrawerLayout and navigating simultaneously)
 */
fun NavigationView.setupWithNavController(navController: NavController) {
    val parent = parent as? DrawerLayout ?: return

    setNavigationItemSelectedListener { item ->
        parent.doOnDrawerClose { onNavDestinationSelected(item, navController) }
        parent.close()
        true
    }
    val weakReference = WeakReference(this)
    navController.addOnDestinationChangedListener(
        object : NavController.OnDestinationChangedListener {
            override fun onDestinationChanged(
                controller: NavController,
                destination: NavDestination,
                arguments: Bundle?
            ) {
                val view = weakReference.get()
                if (view == null) {
                    navController.removeOnDestinationChangedListener(this)
                    return
                }

                if (destination is FloatingWindow) {
                    return
                }

                updateMenu(menu, destination.id)
            }

            private fun updateMenu(menu: Menu, destinationId: Int) {
                menu.forEach { item ->
                    if (item.hasSubMenu()) {
                        updateMenu(item.subMenu!!, destinationId)
                    } else {
                        item.isChecked = destinationId == item.itemId
                    }
                }
            }
        }
    )
}

private fun DrawerLayout.doOnDrawerClose(action: () -> Unit) {
    addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
        override fun onDrawerClosed(drawerView: View) {
            removeDrawerListener(this)
            action.invoke()
        }
    })
}