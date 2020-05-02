package com.github.sarhatabaot.treasurerecipes;

import com.google.gson.*;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public final class TreasureRecipes extends JavaPlugin {
	private static JavaPlugin instance;

	private final RecipeDeserializer recipeDeserializer = new RecipeDeserializer();

	@Override
	public void onEnable() {
		setInstance(this);
		saveDefaultConfig();
		createDefaultDir();

		try {
			loadRecipes();
		} catch (IOException e) {
			getLogger().severe(e.getMessage());
			getLogger().severe(e.getCause().getMessage());
		}
	}


	public static void setInstance(final JavaPlugin instance) {
		TreasureRecipes.instance = instance;
	}

	@Override
	public void onDisable() {
		setInstance(null);
	}

	private void createDefaultDir() {
		final File recipes = new File(getDataFolder() + "/recipes");
		if (!recipes.isDirectory())
			recipes.mkdir();
	}

	private void loadRecipes() throws IOException {
		long startTime = System.currentTimeMillis();
		final File recipes = new File(getDataFolder() + "/recipes");
		Validate.isTrue(recipes.isDirectory(), "Recipes directory doesn't exist.");

		JsonParser jsonParser = new JsonParser();
		int recipeCount = 0;
		int failedCount = 0;
		for (File recipeFile : recipes.listFiles()) {
			JsonElement element = jsonParser.parse(new FileReader(recipeFile));
			Recipe recipe;

			try {
				recipe = load(element);
			} catch (NullPointerException e){
				getLogger().info(String.format("Could not load %s skipping..",recipeFile.getName()));
				failedCount++;
				continue;
			}
			Bukkit.addRecipe(recipe);
			recipeCount++;
		}
		getLogger().info(String.format("Loaded %d/%d recipes in %d ms.",recipeCount,recipeCount+failedCount,System.currentTimeMillis()-startTime));
		if(failedCount > 0)
			getLogger().warning(String.format("Failed to load %d recipes", failedCount));
	}

	public Recipe load(final JsonElement json) {
		JsonObject jsonObject = json.getAsJsonObject();
		final String type = jsonObject.get("type").getAsString();
		if (type.equals("crafting_shapeless"))
			return recipeDeserializer.toShapelessRecipe(json);
		return recipeDeserializer.toShapedRecipe(json);
	}

	private static JavaPlugin getInstance() {
		return instance;
	}

	public static NamespacedKey easyKey(@NotNull final String name) {
		Validate.notNull(name,"Name cannot be null.");
		String filtered = name;
		if (filtered.startsWith(NamespacedKey.MINECRAFT + ":")) {
			filtered = filtered.substring((NamespacedKey.MINECRAFT + ":").length());
		}
		return new NamespacedKey(getInstance(), filtered);
	}

	public static void debug(@NotNull String format, Object... args){
		if(Config.DEBUG)
			getInstance().getSLF4JLogger().info(String.format(format, args));
	}

	public static class Config{
		private static final FileConfiguration configuration = getInstance().getConfig();
		public static final boolean DEBUG = configuration.getBoolean("debug",false);
	}
}
