package savestate.monsters.exordium;

import com.google.gson.JsonObject;
import savestate.fastobjects.AnimationStateFast;
import savestate.monsters.Monster;
import savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.GremlinFat;

import static savestate.SaveStateMod.shouldGoFast;

public class GremlinFatState extends MonsterState {
    public GremlinFatState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.GREMLIN_FAT.ordinal();
    }

    public GremlinFatState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.GREMLIN_FAT.ordinal();
    }

    public GremlinFatState(JsonObject monsterJson) {
        super(monsterJson);

        monsterTypeNumber = Monster.GREMLIN_FAT.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        GremlinFat result = new GremlinFat(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = GremlinFat.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 56)
        public static SpireReturn GremlinFat(GremlinFat _instance, float x, float y) {
            if (shouldGoFast) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
