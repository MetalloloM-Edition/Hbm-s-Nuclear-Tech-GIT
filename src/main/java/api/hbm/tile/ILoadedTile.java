package api.hbm.tile;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import net.minecraft.nbt.NBTTagCompound;

import java.io.IOException;

public interface ILoadedTile {
	
	public boolean isLoaded();

    String getConfigName();

    void readIfPresent(JsonObject obj);

    void writeConfig(JsonWriter writer) throws IOException;

    void packExtra(NBTTagCompound data);

    boolean extraCondition(int convert);

    void postConvert(int convert);
}
