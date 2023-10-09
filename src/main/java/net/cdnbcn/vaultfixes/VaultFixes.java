package net.cdnbcn.vaultfixes;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod("vaultfixes")
public class VaultFixes {

    private static final Logger LOGGER = LogUtils.getLogger();


    /**
     *  a publicly accessible method to get the logger (for use within a mixin).
     *
     * @return VaultFixes Logger
     */
    @SuppressWarnings("unused")
    public static Logger getLogger() { return LOGGER; }

    public VaultFixes() {
        MinecraftForge.EVENT_BUS.register(this);
    }
}
