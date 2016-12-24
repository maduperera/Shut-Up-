package com.wizardsofm.voice;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.wizardsofm.deskclock.alarms.AlarmActivity;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import static android.widget.Toast.makeText;

/**
 * Created by madushaperera on 12/15/16.
 */


// This is a Singleton
public class SpeechRecognizerManager implements
        RecognitionListener {


    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";
    private static final String FORECAST_SEARCH = "forecast";
    private static final String DIGITS_SEARCH = "digits";
    private static final String PHONE_SEARCH = "phones";
    private static final String MENU_SEARCH = "menu";

    // Keyword we are looking for snooze
    private static final String KEYPHRASE =  "alarm stop";//"alarm snooze"; //"go away alarm"; //"shut up alarm"; //"stop it alarm"; //"shut it up"; //"switch it off";  //"alarm stop";


    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;


    private Context mContext;
    private static SpeechRecognizerManager instance = null;

    protected SpeechRecognizerManager(Context context) {
        this.mContext = context;

        initPockerSphinx();
    }


    public static SpeechRecognizerManager getInstance(Context context){
        if(instance == null) {
            instance = new SpeechRecognizerManager(context);
        }else{
            instance.mContext = context;
            instance.initPockerSphinx();
        }

        return instance;
    }


    public static void shutDownTheListener(){
        if(instance != null){
            instance.recognizer.cancel();
            instance.recognizer.shutdown();
        }
    }


    public void initPockerSphinx() {
        runRecognizerSetup();
    }

    private void runRecognizerSetup() {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(mContext);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    // failed to init
                } else {
                    switchSearch(KWS_SEARCH);
                }
            }
        }.execute();
    }


    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        if (text.equals(KEYPHRASE))
            switchSearch(MENU_SEARCH);
        else if (text.equals(DIGITS_SEARCH))
            switchSearch(DIGITS_SEARCH);
        else if (text.equals(PHONE_SEARCH))
            switchSearch(PHONE_SEARCH);
        else if (text.equals(FORECAST_SEARCH))
            switchSearch(FORECAST_SEARCH);

    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {

        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            makeText(mContext, text, Toast.LENGTH_SHORT).show();

            if(!text.isEmpty() && text.contains(KEYPHRASE)){
                AlarmActivity alarmActivity = (AlarmActivity) mContext;
                alarmActivity.snooze();
//                recognizer.stop();
//                shutDownTheListener();
            }


            //TODO: mustchange switchsearch to relisten
//            switchSearch(KWS_SEARCH);
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().equals(KWS_SEARCH))
            switchSearch(KWS_SEARCH);
    }

    private void switchSearch(String searchName) {
        recognizer.stop();

        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
//        if (searchName.equals(KWS_SEARCH))
            recognizer.startListening(searchName);
//        else
//            recognizer.startListening(searchName, 10000);

    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
//                .setKeywordThreshold(1e-25f)
                .getRecognizer();
        recognizer.addListener(this);

        /** In your application you might not need to add all those searches.
         * They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

        // Create grammar-based search for selection between demos
        File menuGrammar = new File(assetsDir, "menu.gram");
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);

        // Create grammar-based search for digit recognition
        File digitsGrammar = new File(assetsDir, "digits.gram");
        recognizer.addGrammarSearch(DIGITS_SEARCH, digitsGrammar);

        // Create language model search
        File languageModel = new File(assetsDir, "weather.dmp");
        recognizer.addNgramSearch(FORECAST_SEARCH, languageModel);

        // Phonetic search
        File phoneticModel = new File(assetsDir, "en-phone.dmp");
        recognizer.addAllphoneSearch(PHONE_SEARCH, phoneticModel);
    }

    @Override
    public void onError(Exception error) {
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }
}