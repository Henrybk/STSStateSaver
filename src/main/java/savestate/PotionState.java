package savestate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.potions.*;

import java.util.HashMap;
import java.util.HashSet;

public class PotionState {
    public final String potionId;
    private final int slot;

    public PotionState(AbstractPotion potion) {
        this.potionId = potion.ID;
        this.slot = potion.slot;
    }

    public PotionState(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.potionId = parsed.get("id").getAsString();
        this.slot = parsed.get("slot").getAsInt();
    }

    public PotionState(JsonObject jsonObject) {
        this.potionId = jsonObject.get("id").getAsString();
        this.slot = jsonObject.get("slot").getAsInt();
    }

    public String encode() {
        return jsonEncode().toString();
    }

    public JsonObject jsonEncode() {
        JsonObject potionJson = new JsonObject();

        potionJson.addProperty("id", potionId);
        potionJson.addProperty("slot", slot);

        return potionJson;
    }

    public AbstractPotion loadPotion() {
        AbstractPotion result;
        if (potionId.equals("Potion Slot")) {
            result = new PotionSlot(slot);
        } else {
            result = PotionHelper.getPotion(potionId).makeCopy();
        }

        result.slot = slot;

        return result;
    }

    public static HashSet<String> UNPLAYABLE_POTIONS = new HashSet<String>() {{
        add(GamblersBrew.POTION_ID);
        add(LiquidMemories.POTION_ID);
        add(SmokeBomb.POTION_ID);
        add(Elixir.POTION_ID);
        add(CultistPotion.POTION_ID);
        add(StancePotion.POTION_ID);
    }};
}
