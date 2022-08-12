package savestate.monsters.exordium;

import com.google.gson.JsonObject;
import savestate.fastobjects.AnimationStateFast;
import savestate.monsters.Monster;
import savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.GremlinTsundere;

import static savestate.SaveStateMod.shouldGoFast;

public class GremlinTsundereState extends MonsterState {
    public GremlinTsundereState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.GREMLIN_TSUNDERE.ordinal();
    }

    public GremlinTsundereState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.GREMLIN_TSUNDERE.ordinal();
    }

    public GremlinTsundereState(JsonObject monsterJson) {
        super(monsterJson);

        monsterTypeNumber = Monster.GREMLIN_TSUNDERE.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        GremlinTsundere result = new GremlinTsundere(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = GremlinTsundere.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 61)
        public static SpireReturn GremlinTsundere(GremlinTsundere _instance, float x, float y) {
            if (shouldGoFast) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
