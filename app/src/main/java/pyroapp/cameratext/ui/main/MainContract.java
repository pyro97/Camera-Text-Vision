package pyroapp.cameratext.ui.main;

import com.google.android.gms.ads.InterstitialAd;

public interface MainContract {
    void initializeBanner();
    InterstitialAd initializeInterstitial();
}
