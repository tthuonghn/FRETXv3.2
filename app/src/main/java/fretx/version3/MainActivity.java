package fretx.version3;



import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.facebook.login.LoginManager;

import java.io.File;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends ActionBarActivity
{

    ViewPager pager;

    private String titles[] = new String[]{ "    play  ",
                                            "    Learn    ",
                                            "    Chords   ",
                                            " Tuner   "};

    SlidingTabLayout slidingTabLayout;

    public TextView m_tvConnectionState;
    public Drawable mFbProfilePic;

    Button btn;

    private LeftNavAdapter adapter;

    private int mCurrentPosition = 0;
    private int mPreviousPosition = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_back);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), titles);
        pager = (ViewPager) findViewById(R.id.viewpager);
        pager.setAdapter(viewPagerAdapter);

        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setViewPager(pager);
        slidingTabLayout.setBackgroundColor(Color.argb(255, 240, 240, 240));
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                mPreviousPosition = mCurrentPosition;
                mCurrentPosition = position;
                if (mPreviousPosition == 1)
                    changeFragments(position);
                Util.stopViaData();
                return Color.BLUE;

            }
        });



        m_tvConnectionState = (TextView) findViewById(R.id.tvConnectionState);
        m_tvConnectionState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Config.bBlueToothActive == false) {
                    Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                    startActivity(intent);
                } else {
                    try {
                        Util.stopViaData();
                        BluetoothClass.mmSocket.close();
                        Config.bBlueToothActive = false;
                        showConnectionState();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        adapter = new LeftNavAdapter(this, getResources().getStringArray(
                R.array.arr_left_nav_list));

        final DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        final ListView navList = (ListView) findViewById(R.id.drawer);
        View header = getLayoutInflater().inflate(R.layout.left_nav_header_one,
                null);
        navList.addHeaderView(header);
        navList.setAdapter(adapter);
        navList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
                                    long arg3)
            {
                drawer.closeDrawers();
                if (pos != 0)
                    launchFragment(pos - 1);
                else
                    launchFragment(-2);

            }
        });
        btn = (Button)findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(navList);
            }
        });

        TextView userName = (TextView) findViewById(R.id.tvYourName);
        userName.setText(Config.strUserName);
        CircleImageView profilePic = (CircleImageView) findViewById(R.id.imageCommonFriends);
        new DownloadUserProfilePic().execute(profilePic);

    }
    public void launchFragment(int pos)
    {

        String title = null;
        if (pos == -1)
        {
            title = "Your Match";
            Toast.makeText(getApplicationContext(),title, Toast.LENGTH_LONG).show();
        }
        else if (pos == -2)
        {
            title = "Profile";
            Toast.makeText(getApplicationContext(),title, Toast.LENGTH_LONG).show();
        }
        else if (pos == 0)
        {
            title = "Home";
            Toast.makeText(getApplicationContext(),title, Toast.LENGTH_LONG).show();
        }
        else if (pos == 1)
        {
            title = "Find Match";
            Intent intent = new Intent(MainActivity.this, PresentationActivity.class);
            startActivity(intent);
            finish();
        }
        else if (pos == 2)
        {
            title = "Chat with Nikita";
            Intent intent  = new Intent(MainActivity.this, CommentMainActivity.class);
            startActivity(intent);
        }
        else if (pos == 3)
        {
            title = "Video Chat with Nikita";
            LoginManager.getInstance().logOut();
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
        }
        else if (pos == 4)
        {
            title = "Liked You";
            Toast.makeText(getApplicationContext(),title, Toast.LENGTH_LONG).show();
        }
        else if (pos == 5)
        {
            title = "Favorites";
            Toast.makeText(getApplicationContext(),title, Toast.LENGTH_LONG).show();
        }
        else if (pos == 6)
        {
            title = "Visitores";
            Toast.makeText(getApplicationContext(),title, Toast.LENGTH_LONG).show();
        }


        if (adapter != null && pos >= 0)
            adapter.setSelection(pos);
    }
    public void showConnectionState(){
        if (Config.bBlueToothActive == true){
            m_tvConnectionState.setText(R.string.connect);
            m_tvConnectionState.setBackgroundColor(Color.GREEN);
        }else{
            m_tvConnectionState.setText(R.string.disconnect);
            m_tvConnectionState.setBackgroundColor(Color.RED);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showConnectionState();
    }

    @Override
    public void onBackPressed() {
        if (mCurrentPosition == 0){
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.play_container, new PlayFragmentSearchList());
            fragmentTransaction.commit();
        }else if (mCurrentPosition == 1){
            FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.learn_container, new LearnFragmentButton());
            fragmentTransaction.commit();
        }

    }
    public void changeFragments(int position){
        if (position == 2 || position == 0){
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.learn_container, new LearnFragmentButton());
            fragmentTransaction.commit();
        }

    }

    private class DownloadUserProfilePic extends AsyncTask<CircleImageView, Void, Drawable> {
        CircleImageView profilePicView = null;
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Drawable doInBackground(CircleImageView... inputs) {
            this.profilePicView = inputs[0];
            //mFbProfilePic = Util.LoadImageFromWeb(Config.strUserProfilePic);
            return Util.LoadImageFromWeb(Config.strUserProfilePic);
        }

        @Override
        protected void onPostExecute(Drawable result) {
            profilePicView.setImageDrawable(result);
            adapter.notifyDataSetChanged();
        }
    }
}
