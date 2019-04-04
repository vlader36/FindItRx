package pro.papaya.canyo.finditrx.viewmodel;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

import java.util.List;

import androidx.lifecycle.ViewModel;
import pro.papaya.canyo.finditrx.firebase.FireBaseItemsManager;
import pro.papaya.canyo.finditrx.firebase.FireBaseProfileManager;
import pro.papaya.canyo.finditrx.model.firebase.QuestModel;

public class QuestsViewModel extends ViewModel {
  public DocumentReference getUserReference() {
    return FireBaseProfileManager.getInstance().getUserReference();
  }

  public CollectionReference getQuestsReference() {
    return FireBaseProfileManager.getInstance().getQuestsReference();
  }

  public Query getItemCollectionModel() {
    return FireBaseItemsManager.getInstance().getItemsCollectionQuery();
  }

  public void setTimestamp(long timestamp) {
    FireBaseProfileManager.getInstance().setTimestamp(timestamp);
  }

  //Returns true if quests requested
  public boolean requestQuests(List<QuestModel> availableQuests, Long timestamp, int oldQuestsCount) {
    return FireBaseItemsManager.getInstance().requestQuests(availableQuests, timestamp, oldQuestsCount);
  }
}
