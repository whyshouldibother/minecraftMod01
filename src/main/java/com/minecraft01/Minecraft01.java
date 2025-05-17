package com.minecraft01;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import com.mojang.brigadier.arguments.StringArgumentType;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import net.minecraft.util.math.BlockPos;
//player tracking
import net.minecraft.world.biome.Biome;

import com.google.gson.JsonObject;

//https libraries for webhook
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;


public class Minecraft01 implements ModInitializer {

	public static final String MOD_ID = "minecraft01";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ConfigHandler.loadConfig();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerCommands(dispatcher);
		});
		LOGGER.info("minecraft01 Initialized");
	}

	public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
				literal("announce")
						.then(argument("message", StringArgumentType.greedyString())
								.executes(context -> {
									String message = StringArgumentType.getString(context, "message");
									return executeAnnounce(context.getSource(), message);
								})));

		dispatcher.register(
				literal("alert").executes(context -> {
					return executeAlert(context.getSource(), "");
				})
						.then(argument("message", StringArgumentType.greedyString())
								.executes(context -> {
									String message = StringArgumentType.getString(context, "message");
									return executeAlert(context.getSource(), message);
								})));
		dispatcher.register(
				literal("mark").executes(context -> {
					return executeMark(context.getSource(), "");
				})
						.then(argument("locationName", StringArgumentType.word())
								.executes(context -> {
									String locationName = StringArgumentType.getString(context, "locationName");
									return executeMark(context.getSource(), locationName);
								})));
		dispatcher.register(literal("ping").executes(context -> executePing(context.getSource())));
	}

	public int executeAlert(ServerCommandSource source, String message) {
		try {
			String webhook = ConfigHandler.config.alertWebHook;
			URL url = URI.create(webhook).toURL();
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);

			// create the payload
			var player = source.getPlayer();
			BlockPos position = player.getBlockPos();
			String username = player.getName().getString();
			String avatar = "https://minotar.net/avatar/" + username;
			JsonObject payload = new JsonObject();
			payload.addProperty("username", "ALERT");
			payload.addProperty("avatar_url", "https://www.onlygfx.com/wp-content/uploads/2020/05/alert-stamp-1.png");
			JsonArray embeds = new JsonArray();
			JsonObject embed = new JsonObject();
			JsonObject author = new JsonObject();
			author.addProperty("name", username);
			author.addProperty("icon_url", avatar);
			embed.add("author", author);
			embed.addProperty("title", "" + position.getX() + "," + position.getY() + "," + position.getZ());
			embed.addProperty("description", message);
			embed.addProperty("color", 15548997);
			embeds.add(embed);
			payload.add("embeds", embeds);
			String jsonPayload = new Gson().toJson(payload);

			// Launche Payload
			try (OutputStream os = connection.getOutputStream()) {
				byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
				os.write(input);
			}

			int responseCode = connection.getResponseCode();
			if (responseCode != 204) { // Discord returns 204 No Content on success
				LOGGER.error("Discord webhook failed: " + responseCode);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return 1;
	}

	public int executeAnnounce(ServerCommandSource source, String message) {
		try {
			String webhook = ConfigHandler.config.annoucementWebHook;
			URL url = URI.create(webhook).toURL();
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);

			// create the payload
			var player = source.getPlayer();
			String username = player.getName().getString();
			String avatar = "https://minotar.net/avatar/" + username;
			JsonObject payload = new JsonObject();
			payload.addProperty("username", username);
			payload.addProperty("avatar_url", avatar);
			payload.addProperty("content", message);
			String jsonPayload = new Gson().toJson(payload);

			// Launche Payload
			try (OutputStream os = connection.getOutputStream()) {
				byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
				os.write(input);
			}

			int responseCode = connection.getResponseCode();
			if (responseCode != 204) { // Discord returns 204 No Content on success
				System.err.println("Discord webhook failed: " + responseCode);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return 1;
	}

	public int executeMark(ServerCommandSource source, String locationName) {
		try {
			String webhook = ConfigHandler.config.cartographerWebHook;
			URL url = URI.create(webhook).toURL();
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);

			// create the payload
			var player = source.getPlayer();
			String username = player.getName().getString();
			String avatar = "https://minotar.net/avatar/" + username;
			BlockPos position = player.getBlockPos();
			RegistryEntry<Biome> biomeEntry = player.getWorld().getBiome(position);
			if (locationName == "") {
				String biomeId = biomeEntry.getKey()
						.map(key -> key.getValue().toString())
						.orElse("unknown");
				locationName = biomeId;
			}
			JsonObject payload = new JsonObject();
			payload.addProperty("username", "Cartographer");
			payload.addProperty("avatar_url",
					"https://static.wikia.nocookie.net/minecraft_gamepedia/images/6/66/Plains_Cartographer.png/revision/latest/scale-to-width-down/70?cb=20200310022433");
			JsonArray embeds = new JsonArray();
			JsonObject embed = new JsonObject();
			JsonObject author = new JsonObject();
			author.addProperty("name", username);
			author.addProperty("icon_url", avatar);
			embed.add("author", author);
			embed.addProperty("title", locationName);
			embed.addProperty("description", "" + position.getX() + "," + position.getY() + "," + position.getZ());
			embed.addProperty("color", 5763719);
			embeds.add(embed);
			payload.add("embeds", embeds);
			String jsonPayload = new Gson().toJson(payload);

			// Launch Payload
			try (OutputStream os = connection.getOutputStream()) {
				byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
				os.write(input);
			}

			int responseCode = connection.getResponseCode();
			if (responseCode != 204) { // Discord returns 204 No Content on success
				System.err.println("Discord webhook failed: " + responseCode);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}

		return 1;
	}

	public int executePing(ServerCommandSource source) {
		source.sendFeedback(() -> Text.literal("Pong"), false);
		return 1;
	}
}