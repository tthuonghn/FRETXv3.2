package fretx.version3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.facebook.AccessToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Misho on 2/21/2016.
 */
public final class Util {
    private Util() {
    }

    // We only need one instance of the clients and credentials provider
    private static AmazonS3Client sS3Client;
    private static CognitoCachingCredentialsProvider sCredProvider;
    private static TransferUtility sTransferUtility;

    /**
     * Gets an instance of CognitoCachingCredentialsProvider which is
     * constructed using the given Context.
     *
     * @param context An Context instance.
     * @return A default credential provider.
     */
    private static CognitoCachingCredentialsProvider getCredProvider(Context context) {
        if (sCredProvider == null) {
            sCredProvider = new CognitoCachingCredentialsProvider(
                    context.getApplicationContext(),
                    Constants.COGNITO_POOL_ID,
                    Regions.EU_WEST_1);
            Map<String, String> logins = new HashMap<String, String>();
            logins.put("graph.facebook.com", AccessToken.getCurrentAccessToken().getToken());
            sCredProvider.setLogins(logins);
            sCredProvider.refresh();
        }
        return sCredProvider;
    }

    /**
     * Gets an instance of a S3 client which is constructed using the given
     * Context.
     *
     * @param context An Context instance.
     * @return A default S3 client.
     */
    public static AmazonS3Client getS3Client(Context context) {
        if (sS3Client == null) {
            sS3Client = new AmazonS3Client(getCredProvider(context.getApplicationContext()));
        }
        return sS3Client;
    }

    /**
     * Gets an instance of the TransferUtility which is constructed using the
     * given Context
     *
     * @param context
     * @return a TransferUtility instance
     */
    public static TransferUtility getTransferUtility(Context context) {
        if (sTransferUtility == null) {
            sTransferUtility = new TransferUtility(getS3Client(context.getApplicationContext()),
                    context.getApplicationContext());
        }

        return sTransferUtility;
    }

    /**
     * Download file from AWS
     *
     *
     * @param context, file
     * @return void
     */
    public static TransferObserver downloadFile(Context context, String bucketName, String fileName) {
        File file = new File(context.getFilesDir().toString() + "/" + fileName);
        TransferUtility transferUtility = Util.getTransferUtility(context);
        // Initiate the download
        TransferObserver observer = transferUtility.download(bucketName, fileName, file);
        return observer;
    }

    public static String checkS3Access(Context context){
        File hwaccessFile = new File(context.getFilesDir().toString()+ "/" + Constants.HW_BUCKET_MAPPING_FILE);
        File userInfoFile = new File(context.getFilesDir().toString()+ "/" + Constants.USER_INFO_FILE);
        String accessFolder = "";
        String macAddress = Constants.NO_BT_DEVICE;
        if(userInfoFile.isFile()){
            try {
                BufferedReader br = new BufferedReader(new FileReader(userInfoFile));
                if((macAddress = br.readLine()) != null){
                    macAddress = macAddress.substring((macAddress.indexOf(",")+1), macAddress.length());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(hwaccessFile.isFile()){
            try {
                BufferedReader br = new BufferedReader(new FileReader(hwaccessFile));

                while((accessFolder = br.readLine()) != null){
                    if(macAddress.equals(accessFolder.substring(0, accessFolder.indexOf(",")))){
                        accessFolder = accessFolder.substring((accessFolder.indexOf(",")+1), accessFolder.length());
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return accessFolder;
    }

    /*
     * Fills in the map with information in the observer so that it can be used
     * with a SimpleAdapter to populate the UI
     */
    public static void fillMap(Map<String, Object> map, TransferObserver observer, boolean isChecked) {
        int progress = (int) ((double) observer.getBytesTransferred() * 100 / observer
                .getBytesTotal());
        map.put("id", observer.getId());
        map.put("checked", isChecked);
        map.put("fileName", observer.getAbsoluteFilePath());
        map.put("progress", progress);
        map.put("bytes",
                getBytesString(observer.getBytesTransferred()) + "/"
                        + getBytesString(observer.getBytesTotal()));
        map.put("state", observer.getState());
        map.put("percentage", progress + "%");
    }

    /**
     * Converts number of bytes into proper scale.
     *
     * @param bytes number of bytes to be converted.
     * @return A string that represents the bytes in a proper scale.
     */
    public static String getBytesString(long bytes) {
        String[] quantifiers = new String[] {
                "KB", "MB", "GB", "TB"
        };
        double speedNum = bytes;
        for (int i = 0;; i++) {
            if (i >= quantifiers.length) {
                return "";
            }
            speedNum /= 1024;
            if (speedNum < 512) {
                return String.format("%.2f", speedNum) + " " + quantifiers[i];
            }
        }
    }

    public static SongItem setSongItem(String songName, String songUrl, String sontText, Drawable drawable){
        SongItem itemData = new SongItem();
        itemData.songName = songName;
        itemData.songURl = songUrl;
        itemData.songTxt = sontText;
        itemData.image = drawable;

        return itemData;
    }

    public static Drawable LoadImageFromWeb(String url){
        try{
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        }catch (Exception e) {
            System.out.println("Exc="+e);
            return null;
        }
    }


    ///Read the text file from resource(Raw) and divide by end line mark('\n")
    public static String readRawTextFile(Context ctx, String txtFile) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(ctx.getFilesDir().toString()+ "/" + txtFile));
            StringBuilder text = new StringBuilder();

            if(inputStream != null) {
                InputStreamReader inputreader = new InputStreamReader(inputStream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;

                try {
                    while ((line = buffreader.readLine()) != null) {
                        text.append(line);
                        text.append('\n');
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return text.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static void setDefaultValues(Boolean[] bArray)
    {
        for (int i = 0; i < bArray.length; i ++){
            bArray[i] = false;
        }
    }
    public static byte[] str2array(String string){
        String strSub = string.replaceAll("[{}]", "");
        String[] parts = strSub.split(",");
        byte[] array = new byte[parts.length];
        for (int i = 0; i < parts.length; i ++)
        {
            array[i] = Byte.valueOf(parts[i]);
        }
        return array;
    }
    public static void startViaData(byte[] array) {
        BluetoothClass.mHandler.obtainMessage(BluetoothClass.ARDUINO, array).sendToTarget();
    }

    public static void stopViaData() {
        byte[] array = new byte[]{0};
        BluetoothClass.mHandler.obtainMessage(BluetoothClass.ARDUINO, array).sendToTarget();
    }

}
