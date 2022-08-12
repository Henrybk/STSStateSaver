package savestate.orbs;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.google.gson.JsonObject;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.cards.blue.Blizzard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.Frost;
import savestate.SaveStateMod;

import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.actionManager;

public class FrostOrbState extends OrbState {
    public FrostOrbState(AbstractOrb orb) {
        super(orb);
    }

    public FrostOrbState(String jsonString) {
        super(jsonString);
    }

    public FrostOrbState(JsonObject orbJson) {
        super(orbJson);
    }

    @Override
    public AbstractOrb loadOrb() {
        Frost result = new Frost();
        result.evokeAmount = this.evokeAmount;
        result.passiveAmount = this.passiveAmount;
        return result;
    }

    @SpirePatch(
            clz = Blizzard.class,
            method = "use",
            paramtypez = {AbstractPlayer.class, AbstractMonster.class}
    )
    public static class NoFXOnUsePatch {
        @SpirePrefixPatch
        public static SpireReturn silentUse(Blizzard blizzard, AbstractPlayer p, AbstractMonster m) {
            if (SaveStateMod.shouldGoFast) {
                int frostCount = (int) actionManager.orbsChanneledThisCombat
                        .stream()
                        .filter(orb -> orb instanceof Frost)
                        .count();

                blizzard.baseDamage = frostCount * blizzard.magicNumber;
                blizzard.calculateCardDamage(null);

                actionManager
                        .addToBottom(new DamageAllEnemiesAction(p, blizzard.multiDamage, blizzard.damageTypeForTurn, AbstractGameAction.AttackEffect.BLUNT_HEAVY, true));

                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
