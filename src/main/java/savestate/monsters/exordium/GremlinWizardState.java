package savestate.monsters.exordium;

import basemod.ReflectionHacks;
import savestate.fastobjects.AnimationStateFast;
import savestate.monsters.Monster;
import savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.GremlinWizard;

import static savestate.SaveStateMod.shouldGoFast;

public class GremlinWizardState extends MonsterState {
    private final int currentCharge;

    public GremlinWizardState(AbstractMonster monster) {
        super(monster);

        currentCharge = ReflectionHacks
                .getPrivate(monster, GremlinWizard.class, "currentCharge");

        monsterTypeNumber = Monster.GREMLIN_WIZARD.ordinal();
    }

    public GremlinWizardState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.currentCharge = parsed.get("current_charge").getAsInt();

        monsterTypeNumber = Monster.GREMLIN_WIZARD.ordinal();
    }

    public GremlinWizardState(JsonObject monsterJson) {
        super(monsterJson);

        this.currentCharge = monsterJson.get("current_charge").getAsInt();

        monsterTypeNumber = Monster.GREMLIN_WIZARD.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        GremlinWizard monster = new GremlinWizard(offsetX, offsetY);
        populateSharedFields(monster);

        ReflectionHacks
                .setPrivate(monster, GremlinWizard.class, "currentCharge", currentCharge);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("current_charge", currentCharge);

        return monsterStateJson.toString();
    }

    @Override
    public JsonObject jsonEncode() {
        JsonObject result = super.jsonEncode();

        result.addProperty("current_charge", currentCharge);

        return result;
    }

    @SpirePatch(
            clz = GremlinWizard.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 55)
        public static SpireReturn GremlinWizard(GremlinWizard _instance, float x, float y) {
            if (shouldGoFast) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
