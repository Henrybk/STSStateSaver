package savestate.monsters.city;

import com.google.gson.JsonObject;
import savestate.fastobjects.AnimationStateFast;
import savestate.monsters.Monster;
import savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.BanditLeader;

import static savestate.SaveStateMod.shouldGoFast;

public class BanditLeaderState extends MonsterState {
    public BanditLeaderState(AbstractMonster monster) {
        super(monster);

        monsterTypeNumber = Monster.BANDIT_LEADER.ordinal();
    }

    public BanditLeaderState(String jsonString) {
        super(jsonString);

        monsterTypeNumber = Monster.BANDIT_LEADER.ordinal();
    }

    public BanditLeaderState(JsonObject monsterJson) {
        super(monsterJson);

        monsterTypeNumber = Monster.BANDIT_LEADER.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        BanditLeader result = new BanditLeader(offsetX, offsetY);
        populateSharedFields(result);
        return result;
    }

    @SpirePatch(
            clz = BanditLeader.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 66)
        public static SpireReturn BanditLeader(BanditLeader _instance, float x, float y) {
            if (shouldGoFast) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
