package pro.papaya.canyo.finditrx.model.firebase;

public class UserQuestsModel extends QuestModel {
  private int reward;

  public UserQuestsModel() {
  }

  public UserQuestsModel(String identifier, String label, int reward) {
    super(identifier, label);
    this.reward = reward;
  }

  public static UserQuestsModel from(QuestModel model, int reward) {
    return new UserQuestsModel(
        model.identifier,
        model.label,
        reward
    );
  }

  public int getReward() {
    return reward;
  }

  public void setReward(int reward) {
    this.reward = reward;
  }
}
