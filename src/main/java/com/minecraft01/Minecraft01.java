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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
//https libraries for webhook
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Vector;
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

	private static String escapeJson(String str) {
		return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
	}

	public int executeAlert(ServerCommandSource source, String message) {
		try {
			String webhook = "https://discord.com/api/webhooks/1373191908658249788/pS7KahQOnCgTXU0lsWYVlHcfukd4iwoCZwRjrxztzZuYMY9A7ngB-af8mbP2QRPl_Xtm";
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
			author.addProperty("name",username);
			author.addProperty("icon_url", avatar);
			embed.add("author",author);
			embed.addProperty("title", position.getX() + position.getY() + position.getZ());
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
			String webhook = "https://discord.com/api/webhooks/1373191908658249788/pS7KahQOnCgTXU0lsWYVlHcfukd4iwoCZwRjrxztzZuYMY9A7ngB-af8mbP2QRPl_Xtm";
			URL url = URI.create(webhook).toURL();
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);

			// create the payload
			var player = source.getPlayer();
			String username = player.getName().getString();
			String avatar = "https://minotar.net/avatar/" + username;
			String jsonPayload = String.format("{\"username\":\"%s\",\"avatar_url\":\"%s\",\"content\":\"%s\"}",
					username, avatar, escapeJson(message));
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
			String webhook = "https://discord.com/api/webhooks/1373220129038536735/tul7CxBKRF9hYIoFXICzpeYXuYylT1oGjpX2ClzYodU9_ZeO_-Zuq4Cog951hGt_YGQO";
			URL url = URI.create(webhook).toURL();
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);

			// create the payload
			var player = source.getPlayer();
			BlockPos position = player.getBlockPos();
			RegistryEntry<Biome> biomeEntry = player.getWorld().getBiome(position);
			if (locationName == "") {
				String biomeId = biomeEntry.getKey()
						.map(key -> key.getValue().toString())
						.orElse("unknown");
				locationName = biomeId;
			}
			String jsonPayload = String.format("{\"content\":\"%s\"}", escapeJson(position + ">" + locationName));

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