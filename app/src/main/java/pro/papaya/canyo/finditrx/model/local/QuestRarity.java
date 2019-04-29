package pro.papaya.canyo.finditrx.model.local;

import pro.papaya.canyo.finditrx.R;

import static pro.papaya.canyo.finditrx.utils.Constants.BOTTOM_RARITY_BORDER;
import static pro.papaya.canyo.finditrx.utils.Constants.RARITY_PRICE_STEP;

public enum QuestRarity {
  COMMON(
      BOTTOM_RARITY_BORDER,
      R.color.common_quest_color,
      "Common"),
  UNCOMMON(
      BOTTOM_RARITY_BORDER + RARITY_PRICE_STEP,
      R.color.uncommon_quest_color,
      "Uncommon"),
  RARE(BOTTOM_RARITY_BORDER + RARITY_PRICE_STEP * 2,
      R.color.rare_quest_color,
      "Rare"),
  MYTHIC(BOTTOM_RARITY_BORDER + RARITY_PRICE_STEP * 3,
      R.color.mythic_quest_color,
      "Mythic"),
  LEGENDARY(BOTTOM_RARITY_BORDER + RARITY_PRICE_STEP * 4,
      R.color.legendary_quest_color,
      "Legendary"
  );

  private int price;
  private int color;
  private String rarity;

  QuestRarity(int price, int color, String rarity) {
    this.price = price;
    this.color = color;
    this.rarity = rarity;
  }

  public static QuestRarity getRarity(int price) {
    if (price > 0 && price < UNCOMMON.getPrice()) {
      return COMMON;
    } else if (price > COMMON.getPrice() && price < RARE.getPrice()) {
      return UNCOMMON;
    } else if (price > UNCOMMON.getPrice() && price < MYTHIC.getPrice()) {
      return RARE;
    } else if (price > RARE.getPrice() && price < LEGENDARY.getPrice()) {
      return MYTHIC;
    } else {
      return LEGENDARY;
    }
  }

  public int getPrice() {
    return price;
  }

  public int getColor() {
    return color;
  }

  public String getRarityString() {
    return rarity;
  }
}