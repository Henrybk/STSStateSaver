package savestate.powers.powerstates.common;

import basemod.ReflectionHacks;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.RitualPower;
import savestate.powers.PowerState;

public class RitualPowerState extends PowerState {
    private final boolean skipFirst;

    public RitualPowerState(AbstractPower power) {
        super(power);

        this.skipFirst = ReflectionHacks
                .getPrivate(power, RitualPower.class, "skipFirst");
    }

    public RitualPowerState(String jsonString) {
        super(jsonString);

        // TODO don't reparse somehow
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.skipFirst = parsed.get("skip_first").getAsBoolean();
    }

    public RitualPowerState(JsonObject powerJson) {
        super(powerJson);

        this.skipFirst = powerJson.get("skip_first").getAsBoolean();
    }

    @Override
    public String encode() {
        JsonObject parsed = new JsonParser().parse(super.encode()).getAsJsonObject();

        parsed.addProperty("skip_first", skipFirst);

        return parsed.toString();
    }

    @Override
    public JsonObject jsonEncode() {
        JsonObject result = super.jsonEncode();

        result.addProperty("skip_first", skipFirst);

        return result;
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        RitualPower result = new RitualPower(targetAndSource, amount, false);
        ReflectionHacks
                .setPrivate(result, RitualPower.class, "skipFirst", skipFirst);
        return result;
    }
}
