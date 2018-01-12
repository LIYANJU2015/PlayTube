package org.schabi.newpipe.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import org.schabi.newpipe.R;

import java.io.File;
import java.net.URLDecoder;
import java.util.regex.Pattern;

public class FilenameUtils {

    /**
     * #143 #44 #42 #22: make sure that the filename does not contain illegal chars.
     * @param context the context to retrieve strings and preferences from
     * @param title the title to create a filename from
     * @return the filename
     */
    public static String createFilename(Context context, String title) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String key = context.getString(R.string.settings_file_charset_key);
        final String value = sharedPreferences.getString(key, context.getString(R.string.default_file_charset_value));
        Pattern pattern = Pattern.compile(value);

        final String replacementChar = sharedPreferences.getString(context.getString(R.string.settings_file_replacement_character_key), "_");
        return createFilename(title, pattern, replacementChar);
    }

    public static String sizeFormatNum2String(long size) {
        String s = "";
        if(size>1024*1024)
            s=String.format("%.2f", (double)size/(1024*1024))+"M";
        else
            s=String.format("%.2f", (double)size/(1024))+"KB";
        return s;
    }

    public static String parseRefererSource(String referer) {
        try {
            String newreferer = URLDecoder.decode(referer, "UTF-8");
            String target = Constants.SOURCE;
            newreferer = newreferer.substring(newreferer.indexOf(target) + target.length(), newreferer.length());
            String source = newreferer.substring(0, newreferer.indexOf("&"));
            return source;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String parseRefererCampaignid(String referer) {
        try {
            String newreferer = URLDecoder.decode(referer, "UTF-8");
            String target = "campaignid=";
            newreferer = newreferer.substring(newreferer.indexOf(target) + target.length(), newreferer.length());
            String source = newreferer.substring(0, newreferer.indexOf("&"));
            return source;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String parseRefererCampaign(String referer) {
        try {
            String newreferer = URLDecoder.decode(referer, "UTF-8");
            String target = Constants.CAMPAIGN;
            newreferer = newreferer.substring(newreferer.indexOf(target) + target.length(), newreferer.length());
            String source = newreferer.substring(0, newreferer.indexOf("&"));
            return source;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String convertDuration(String duration) {
        duration = duration.substring(2);  // del. PT-symbols
        String H, M, S;
        // Get Hours:
        int indOfH = duration.indexOf("H");  // position of H-symbol
        if (indOfH > -1) {  // there is H-symbol
            H = duration.substring(0,indOfH);      // take number for hours
            duration = duration.substring(indOfH); // del. hours
            duration = duration.replace("H","");   // del. H-symbol
        } else {
            H = "";
        }
        // Get Minutes:
        int indOfM = duration.indexOf("M");  // position of M-symbol
        if (indOfM > -1) {  // there is M-symbol
            M = duration.substring(0,indOfM);      // take number for minutes
            duration = duration.substring(indOfM); // del. minutes
            duration = duration.replace("M","");   // del. M-symbol
            // If there was H-symbol and less than 10 minutes
            // then add left "0" to the minutes
            if (H.length() > 0 && M.length() == 1) {
                M = "0" + M;
            }
        } else {
            // If there was H-symbol then set "00" for the minutes
            // otherwise set "0"
            if (H.length() > 0) {
                M = "00";
            } else {
                M = "0";
            }
        }
        // Get Seconds:
        int indOfS = duration.indexOf("S");  // position of S-symbol
        if (indOfS > -1) {  // there is S-symbol
            S = duration.substring(0,indOfS);      // take number for seconds
            duration = duration.substring(indOfS); // del. seconds
            duration = duration.replace("S","");   // del. S-symbol
            if (S.length() == 1) {
                S = "0" + S;
            }
        } else {
            S = "00";
        }
        if (H.length() > 0) {
            return H + ":" +  M + ":" + S;
        } else {
            return M + ":" + S;
        }
    }

    private static final String HTTP_CACHE_DIR = "http";

    public static File getHttpCacheDir(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return new File(context.getExternalCacheDir(), HTTP_CACHE_DIR);
        }
        return new File(context.getCacheDir(), HTTP_CACHE_DIR);
    }

    /**
     * Create a valid filename
     * @param title the title to create a filename from
     * @param invalidCharacters patter matching invalid characters
     * @param replacementChar the replacement
     * @return the filename
     */
    private static String createFilename(String title, Pattern invalidCharacters, String replacementChar) {
        return title.replaceAll(invalidCharacters.pattern(), replacementChar);
    }
}