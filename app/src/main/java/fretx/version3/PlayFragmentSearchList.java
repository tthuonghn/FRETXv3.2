package fretx.version3;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SearchView;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;


public class PlayFragmentSearchList extends Fragment implements SearchView.OnQueryTextListener {

    private MainActivity mActivity;

    private View rootView = null;

    public SearchView svNews = null;

    public GridView lvListNews = null;
    public ArrayList<SongItem> mainData;

    public PlayFragmentSearchList(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mActivity = (MainActivity) getActivity();

        rootView = inflater.inflate(R.layout.play_fragment_search_list, container, false);

        svNews = (SearchView)rootView.findViewById(R.id.svSongs);
        svNews.setOnQueryTextListener(this);

        //initData();
        mainData = new ArrayList<SongItem>();

        lvListNews = (GridView)rootView.findViewById(R.id.lvSongList);

        lvListNews.setAdapter(new CustomGridViewAdapter(mActivity, R.layout.play_fragment_search_list_row_item, mainData));

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    public void initData(){
        mainData = new ArrayList<SongItem>();
        String accessFolder = Util.checkS3Access(mActivity);

        new GetFileListTask().execute(accessFolder);
        /*mainData.add(Util.setSongItem("The Beatles - Come Together",        "eTNitq77Utg",  R.raw.one, homeIcon));
        mainData.add(Util.setSongItem("The Beatles - Here Comes The Sun",   "Y6GNEEi7x4c",  R.raw.two,homeIcon));
        mainData.add(Util.setSongItem("Oasis - Wonderwall",                 "SLZ7uzFIMoY",  R.raw.three,homeIcon));
        mainData.add(Util.setSongItem("Led Zeppelin - Immigrant Song", "TlmrQfSTmiY", R.raw.four,homeIcon));*/
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        lvListNews.setVisibility(View.VISIBLE);
        if (!query.equals(null)){
            ArrayList<SongItem> arrResultTemp = new ArrayList<SongItem>();
            for (int i = 0; i < mainData.size(); i ++){
                if(mainData.get(i).songName.toLowerCase().contains(query.toLowerCase())){
                    arrResultTemp.add(mainData.get(i));
                }
                lvListNews.setAdapter(new CustomGridViewAdapter(mActivity,R.layout.play_fragment_search_list_row_item, arrResultTemp));
            }
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        lvListNews.setVisibility(View.VISIBLE);
        if (newText.equals(null)){
            lvListNews.setAdapter(new CustomGridViewAdapter(mActivity,R.layout.play_fragment_search_list_row_item, mainData));
        }else{
            ArrayList<SongItem> arrResultTemp = new ArrayList<SongItem>();
            for (int i = 0; i < mainData.size(); i ++){
                if(mainData.get(i).songName.toLowerCase().contains(newText.toLowerCase())) {
                    arrResultTemp.add(mainData.get(i));
                }
                lvListNews.setAdapter(new CustomGridViewAdapter(mActivity,R.layout.play_fragment_search_list_row_item, arrResultTemp));
            }
        }
        return false;
    }

    private class GetFileListTask extends AsyncTask<String, Void, Void> {
        // The list of objects we find in the S3 bucket
        private List<S3ObjectSummary> s3ObjList;
        // A dialog to let the user know we are retrieving the files
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(mActivity,
                    getString(R.string.refreshing),
                    getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(String... inputs) {
            // Queries files in the bucket from S3.
            AmazonS3Client s3 = Util.getS3Client(mActivity);
            s3ObjList = s3.listObjects(inputs[0]).getObjectSummaries();
            mainData.clear();
            int count = 0;
            for (S3ObjectSummary summary : s3ObjList) {
                if(count >= 10) {
                    break;
                }
                if(!(new File(mActivity.getFilesDir().toString() + "/" + summary.getKey()).isFile())) {
                    Util.downloadFile(mActivity, inputs[0], summary.getKey());
                }
                String keySplit[] = summary.getKey().split("\\.");
                Drawable drawable = Util.LoadImageFromWeb("http://img.youtube.com/vi/" + keySplit[1] + "/0.jpg");
                mainData.add(Util.setSongItem(keySplit[0], keySplit[1], summary.getKey(), drawable));
                count++;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
            lvListNews.setAdapter(new CustomGridViewAdapter(mActivity, R.layout.play_fragment_search_list_row_item, mainData));
        }
    }
}