package pro.papaya.canyo.finditrx.firebase;


import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import pro.papaya.canyo.finditrx.model.firebase.ItemModel;

public class FireBaseItemsManager {
  private static final String TABLE_LABELS = "labels";
  private static final String TABLE_LABELS_ID_FIELD = "id";

  private static final FirebaseFirestore database = FirebaseFirestore.getInstance();

  public static Observable<List<ItemModel>> getObservableItemsCollection() {
    return new Observable<List<ItemModel>>() {
      @Override
      protected void subscribeActual(Observer<? super List<ItemModel>> observer) {
        database.collection(TABLE_LABELS).orderBy(TABLE_LABELS_ID_FIELD)
            .addSnapshotListener((queryDocumentSnapshots, e) -> {
              if (queryDocumentSnapshots != null) {
                observer.onNext(queryDocumentSnapshots.toObjects(ItemModel.class));
              } else if (e != null) {
                observer.onError(e);
              }
            });
      }
    };
  }
}
