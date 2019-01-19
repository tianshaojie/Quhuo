package cn.skyui.module.support;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.chenenyu.router.annotation.Route;

import cn.skyui.module.support.fragment.HomeFragment;

/**
 * @author tianshaojie
 * @date 2019/1/15
 */
@Route("/support/main")
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAB_INDEX_KEY = "tabIndex";
    private static final String[] FRAGMENT_TAGS = new String[]{
            "HomeFragment", "FavoriteFragment", "MessageFragment", "MemberFragment"
    };

    private int tabIndex = 0;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        showFragment(0);
    }

    private void showFragment(int index) {
        // 隐藏旧Tab
        if (tabIndex != index) {
            Fragment prevFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAGS[tabIndex]);
            if (prevFragment != null) {
                fragmentManager.beginTransaction().hide(prevFragment).commit();
            }
        }
        // 显示新Tab
        Fragment fragment = fragmentManager.findFragmentByTag(FRAGMENT_TAGS[index]);
        if (fragment != null && fragment.isAdded()) {
            fragmentManager.beginTransaction().show(fragment).commit();
        } else {
            fragment = instantFragment(index);
            fragmentManager.beginTransaction().add(R.id.fragment_container, fragment, FRAGMENT_TAGS[index]).commit();
        }
        tabIndex = index;
    }

    private Fragment instantFragment(int currIndex) {
        switch (currIndex) {
            case 0:
                return new HomeFragment();
//            case 1:
//                return FavoriteFragment.newInstance();
//            case 2:
//                return MessageFragment.newInstance();
//            case 3:
//                return MemberFragment.newInstance();
            default:
                return new HomeFragment();
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
