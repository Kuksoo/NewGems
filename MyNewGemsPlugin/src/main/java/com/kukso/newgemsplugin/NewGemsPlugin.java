package kukso.newgemsplugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.comphenix.protocol.wrappers.WrappedStack;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.StonecuttingRecipe;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class NewGemsPlugin extends JavaPlugin {

    private static final Logger PLUGIN_LOGGER = Logger.getLogger("NewGemsPlugin");

    public static NamespacedKey CUT_DIAMOND_KEY;
    public static NamespacedKey CUT_EMERALD_KEY;
    private static ItemStack cutDiamond; // Ограненный Алмаз
    private static ItemStack cutEmerald; // Ограненный Изумруд

    @Override
    public void onEnable() {
        PLUGIN_LOGGER.info("NewGemsPlugin включен! Начинаем инициализацию...");

        try {
            CUT_DIAMOND_KEY = new NamespacedKey(this, "cut_diamond");
            CUT_EMERALD_KEY = new NamespacedKey(this, "cut_emerald");

            createCustomItems();
            registerRecipes();
            setupProtocolLib();

            PLUGIN_LOGGER.info("NewGemsPlugin инициализация завершена.");

        } catch (Exception e) {
            PLUGIN_LOGGER.log(Level.SEVERE, "Неожиданная ошибка при инициализации плагина NewGemsPlugin", e);
        }
    }

    @Override
    public void onDisable() {
        PLUGIN_LOGGER.info("NewGemsPlugin выключен.");
    }

    private void createCustomItems() {
        try {
            cutDiamond = new ItemStack(Material.DIAMOND, 1);
            ItemMeta cutDiamondMeta = cutDiamond.getItemMeta();
            if (cutDiamondMeta != null) {
                cutDiamondMeta.setDisplayName("§bОграненный алмаз");
                List<String> diamondLore = new ArrayList<>();
                diamondLore.add("§7Сияющий алмаз.");
                diamondLore.add("§8(Из необработанной руды)");
                cutDiamondMeta.setLore(diamondLore);
                cutDiamondMeta.getPersistentDataContainer().set(CUT_DIAMOND_KEY, PersistentDataType.STRING, "cut_diamond");
                cutDiamond.setItemMeta(cutDiamondMeta);
            } else {
                PLUGIN_LOGGER.warning("Не удалось создать метаданные для Ограненного алмаза!");
            }
        } catch (Exception e) {
            PLUGIN_LOGGER.log(Level.SEVERE, "Ошибка при создания Ограненного алмаза", e);
        }

        try {
            cutEmerald = new ItemStack(Material.EMERALD, 1);
            ItemMeta cutEmeraldMeta = cutEmerald.getItemMeta();
            if (cutEmeraldMeta != null) {
                cutEmeraldMeta.setDisplayName("§2Ограненный изумруд");
                List<String> emeraldLore = new ArrayList<>();
                emeraldLore.add("§7Идеально ограненный изумруд.");
                emeraldLore.add("§8(Из необработанной руды)");
                cutEmeraldMeta.setLore(emeraldLore);
                cutEmeraldMeta.getPersistentDataContainer().set(CUT_EMERALD_KEY, PersistentDataType.STRING, "cut_emerald");
                cutEmerald.setItemMeta(cutEmeraldMeta);
            } else {
                PLUGIN_LOGGER.warning("Не удалось создать метаданные для Ограненного изумруда!");
            }
        } catch (Exception e) {
            PLUGIN_LOGGER.log(Level.SEVERE, "Ошибка при создания Ограненного изумруда", e);
        }

        PLUGIN_LOGGER.info("Кастомные предметы созданы.");
    }

    private void registerRecipes() {
        try {
            NamespacedKey diamondKey = new NamespacedKey(this, "cut_diamond_recipe");
            StonecuttingRecipe diamondRecipe = new StonecuttingRecipe(diamondKey, cutDiamond, new MaterialChoice(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE));
            getServer().addRecipe(diamondRecipe);
            PLUGIN_LOGGER.info("Рецепт Ограненного Алмаза из руды для камнереза зарегистрирован.");
        } catch (Exception e) {
            PLUGIN_LOGGER.log(Level.SEVERE, "Ошибка при регистрации рецепта Ограненного Алмаза из руды в камнерезе", e);
        }

        try {
            NamespacedKey emeraldKey = new NamespacedKey(this, "cut_emerald_recipe");
            StonecuttingRecipe emeraldRecipe = new StonecuttingRecipe(emeraldKey, cutEmerald, new MaterialChoice(Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE));
            getServer().addRecipe(emeraldRecipe);
            PLUGIN_LOGGER.info("Рецепт Ограненного Изумруда из руды для камнереза зарегистрирован.");
        } catch (Exception e) {
            PLUGIN_LOGGER.log(Level.SEVERE, "Ошибка при регистрации рецепта Ограненного Изумруда из руды в камнерезе", e);
        }

        PLUGIN_LOGGER.info("Рецепты зарегистрированы.");
    }

    private void setupProtocolLib() {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL,
                PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
                    ItemStack item = packet.getItemModifier().read(0);
                    ItemStack modifiedItem = modifyItemStack(item);
                    if (modifiedItem != null) {
                        packet.getItemModifier().write(0, modifiedItem);
                    }
                } else if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
                    List<ItemStack> itemList = packet.getItemListModifier().read(0);
                    List<ItemStack> modifiedList = new ArrayList<>();
                    for (ItemStack item : itemList) {
                        ItemStack modifiedItem = modifyItemStack(item);
                        modifiedList.add(modifiedItem != null ? modifiedItem : item);
                    }
                    packet.getItemListModifier().write(0, modifiedList);
                }
            }
        });

        PLUGIN_LOGGER.info("ProtocolLib настроен для обработки кастомных ID.");
    }

    private ItemStack modifyItemStack(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return item;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        if (item.getType() == Material.DIAMOND && meta.getPersistentDataContainer().has(CUT_DIAMOND_KEY, PersistentDataType.STRING)) {
            WrappedStack wrappedStack = WrappedStack.fromItemStack(item);
            wrappedStack.setItemKey(new MinecraftKey("newgems", "cut_diamond"));
            return wrappedStack.toItemStack();
        }

        if (item.getType() == Material.EMERALD && meta.getPersistentDataContainer().has(CUT_EMERALD_KEY, PersistentDataType.STRING)) {
            WrappedStack wrappedStack = WrappedStack.fromItemStack(item);
            wrappedStack.setItemKey(new MinecraftKey("newgems", "cut_emerald"));
            return wrappedStack.toItemStack();
        }

        return item;
    }

    public static ItemStack getCutDiamond() {
        return cutDiamond != null ? cutDiamond.clone() : null;
    }

    public static ItemStack getCutEmerald() {
        return cutEmerald != null ? cutEmerald.clone() : null;
    }
}
