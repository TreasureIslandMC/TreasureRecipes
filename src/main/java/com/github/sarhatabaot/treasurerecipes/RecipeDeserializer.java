package com.github.sarhatabaot.treasurerecipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.Arrays;
import java.util.Map;

public class RecipeDeserializer {

	public ShapedRecipe toShapedRecipe(final JsonElement json){
		final String group = json.getAsJsonObject().get("group").getAsString();
		final String[] pattern = getAsStringArray(json.getAsJsonObject().get("pattern").getAsJsonArray());
		final String item = json.getAsJsonObject().getAsJsonObject("result").get("item").getAsString();
		int count = json.getAsJsonObject().getAsJsonObject("result").get("count").getAsInt();

		final Material material = Material.matchMaterial(item);
		Validate.notNull(material,"Material cannot be null.");
		Validate.notNull(group,"Group cannot be null.");
		Validate.notNull(pattern,"Pattern cannot be null");
		final ItemStack result = new ItemStack(material,count);

		ShapedRecipe recipe = new ShapedRecipe(TreasureRecipes.easyKey(item),result);
		recipe.setGroup(group);
		recipe.shape(pattern);

		for(Map.Entry<String, JsonElement> entrySet: json.getAsJsonObject().get("key").getAsJsonObject().entrySet()){
			char charJson = entrySet.getKey().charAt(0);
			String keyItem = entrySet.getValue().getAsJsonObject().get("item").getAsString();
			recipe.setIngredient(charJson,new ItemStack(Material.matchMaterial(keyItem)));
		}
		return recipe;
	}

	public ShapelessRecipe toShapelessRecipe(final JsonElement json){
		final String resultItem = json.getAsJsonObject().get("result").getAsJsonObject().get("item").getAsString();
		final int resultCount = json.getAsJsonObject().get("result").getAsJsonObject().get("count").getAsInt();
		final String group = json.getAsJsonObject().get("group").getAsString();

		Validate.notNull(resultItem,"Result item cannot be null.");
		Validate.notNull(group,"Group cannot be null.");
		final Material resultMaterial = Material.matchMaterial(resultItem);

		Validate.notNull(resultMaterial,"Material cannot be null.");
		ShapelessRecipe shapelessRecipe = new ShapelessRecipe(TreasureRecipes.easyKey(resultItem),new ItemStack(resultMaterial,resultCount));
		shapelessRecipe.setGroup(group);

		for(JsonElement jsonElement: json.getAsJsonObject().getAsJsonArray("ingredients")){
			final Material materialItem = Material.matchMaterial(jsonElement.getAsJsonObject().get("item").getAsString());

			Validate.notNull(materialItem,"Material cannot be null.");

			shapelessRecipe.addIngredient(1,materialItem);
		}

		return shapelessRecipe;
	}

	public String[] getAsStringArray(final JsonArray array){
		String[] strings = new String[array.size()];
		for(int i=0;i<array.size();i++){
			strings[i] = array.get(i).getAsString();
		}
		TreasureRecipes.debug("Pattern=%s,Size=%d", Arrays.toString(strings),array.size());
		return strings;
	}
}
