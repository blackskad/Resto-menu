package be.ugent.zeus.resto.client.data.services;

import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import be.ugent.zeus.resto.client.data.Building;
import be.ugent.zeus.resto.client.data.caches.BuildingCache;
import java.util.List;
import org.json.JSONArray;

/**
 *
 * @author Thomas Meire
 */
public class RestoService extends HTTPIntentService {

  private static final String RESTO_URL = "http://zeus.ugent.be/~blackskad/resto/api/0.1/list.json";

  private BuildingCache cache;

  public RestoService() {
    super("RestoService");
  }

  @Override
  public void onCreate() {
    super.onCreate();
    // get an instance of the menu cache
    cache = BuildingCache.getInstance(this);
  }

  private void sync() {
    Log.i("[RestoFetcherThread]", "Fetching resto's from " + RESTO_URL);

    try {
      JSONArray data = new JSONArray(fetch(RESTO_URL));
      Building[] restos = parseJsonArray(data, Building.class);
      for (Building r : restos) {
        cache.put(r.name, r);
      }
    } catch (Exception e) {
      Log.e("[RestoService]", "An exception occured while parsing the json response!");
    }
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    final ResultReceiver receiver = intent.getParcelableExtra(RESULT_RECEIVER_EXTRA);
    if (receiver != null) {
      receiver.send(STATUS_STARTED, Bundle.EMPTY);
    }

    // get the menu from the local cache
    List<Building> restos = cache.getAll();

    // if not in the cache, sync it from the rest service
    if (restos == null || restos.isEmpty()) {
      sync();
    }

    // send the result to the receiver
    if (receiver != null) {
      final Bundle bundle = new Bundle();
      receiver.send(STATUS_FINISHED, bundle);
    }
  }
}
