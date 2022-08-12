package savestate.powers.powerstates;

import basemod.ReflectionHacks;
import savestate.powers.PowerState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.TimeMazePower;

public class TimeMazePowerState extends PowerState {
    private final int maxAmount;

    public TimeMazePowerState(AbstractPower power) {
        super(power);
        maxAmount = ReflectionHacks.getPrivate(power, TimeMazePower.class, "maxAmount");
    }

    public TimeMazePowerState(String jsonString) {
        super(jsonString);

        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        maxAmount = parsed.get("max_amount").getAsInt();
    }

    public TimeMazePowerState(JsonObject powerJson) {
        super(powerJson);

        maxAmount = powerJson.get("max_amount").getAsInt();
    }

    @Override
    public String encode() {
        JsonObject parsed = new JsonParser().parse(super.encode()).getAsJsonObject();

        parsed.addProperty("max_amount", maxAmount);

        return parsed.toString();
    }

    @Override
    public JsonObject jsonEncode() {
        JsonObject result = super.jsonEncode();

        result.addProperty("max_amount", maxAmount);

        return result;
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        TimeMazePower timeMazePower = new TimeMazePower(targetAndSource, maxAmount);
        timeMazePower.amount = this.amount;
        return timeMazePower;
    }
}
