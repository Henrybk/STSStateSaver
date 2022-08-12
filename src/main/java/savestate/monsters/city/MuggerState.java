package savestate.monsters.city;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.Mugger;
import savestate.fastobjects.AnimationStateFast;
import savestate.monsters.Monster;
import savestate.monsters.MonsterState;

import static savestate.SaveStateMod.shouldGoFast;

public class MuggerState extends MonsterState {
    private final int slashCount;
    private final int stolenGold;

    public MuggerState(AbstractMonster monster) {
        super(monster);

        slashCount = ReflectionHacks
                .getPrivate(monster, Mugger.class, "slashCount");
        stolenGold = ReflectionHacks
                .getPrivate(monster, Mugger.class, "stolenGold");

        monsterTypeNumber = Monster.MUGGER.ordinal();
    }

    public MuggerState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.slashCount = parsed.get("slash_count").getAsInt();
        this.stolenGold = parsed.get("stolen_gold").getAsInt();

        monsterTypeNumber = Monster.MUGGER.ordinal();
    }

    public MuggerState(JsonObject monsterJson) {
        super(monsterJson);

        this.slashCount = monsterJson.get("slash_count").getAsInt();
        this.stolenGold = monsterJson.get("stolen_gold").getAsInt();

        monsterTypeNumber = Monster.MUGGER.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Mugger monster = new Mugger(offsetX, offsetY);
        populateSharedFields(monster);

        ReflectionHacks
                .setPrivate(monster, Mugger.class, "slashCount", slashCount);
        ReflectionHacks
                .setPrivate(monster, Mugger.class, "stolenGold", stolenGold);
        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("slash_count", slashCount);
        monsterStateJson.addProperty("stolen_gold", stolenGold);

        return monsterStateJson.toString();
    }

    @Override
    public JsonObject jsonEncode() {
        JsonObject result = super.jsonEncode();

        result.addProperty("slash_count", slashCount);
        result.addProperty("stolen_gold", stolenGold);

        return result;
    }

    @SpirePatch(
            clz = Mugger.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 70)
        public static SpireReturn Mugger(Mugger _instance, float x, float y) {
            if (shouldGoFast) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
