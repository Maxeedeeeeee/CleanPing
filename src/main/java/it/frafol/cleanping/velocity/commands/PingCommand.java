package it.frafol.cleanping.velocity.commands;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import it.frafol.cleanping.velocity.enums.VelocityConfig;
import it.frafol.cleanping.velocity.enums.VelocityMessages;
import it.frafol.cleanping.velocity.enums.VelocityRedis;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Optional;
import java.util.UUID;

public class PingCommand implements SimpleCommand {

	private final ProxyServer proxyServer;

	public PingCommand(ProxyServer server) {
		this.proxyServer = server;
	}

	@Override
	public void execute(SimpleCommand.Invocation invocation) {

		final CommandSource source = invocation.source();

		if(!(source instanceof Player)) {
			source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.ONLY_PLAYERS.color()
					.replace("%prefix%", VelocityMessages.PREFIX.color())));
			return;
		}

		final Player player = (Player) source;

		if (invocation.arguments().length == 0) {

			final long ping = player.getPing();

			if (source.hasPermission(VelocityConfig.PING_PERMISSION.get(String.class))) {

				if (ping < VelocityConfig.MEDIUM_MS.get(Integer.class)) {

					source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.PING.color()
							.replace("%prefix%", VelocityMessages.PREFIX.color())
							.replace("%ping%", VelocityConfig.LOW_MS_COLOR.color() + player.getPing())));

				} else if (ping > VelocityConfig.MEDIUM_MS.get(Integer.class)
						&& ping < VelocityConfig.HIGH_MS.get(Integer.class)) {
					source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.PING.color()
							.replace("%prefix%", VelocityMessages.PREFIX.color())
							.replace("%ping%", VelocityConfig.MEDIUM_MS_COLOR.color() + player.getPing())));

				} else {
					source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.PING.color()
							.replace("%prefix%", VelocityMessages.PREFIX.color())
							.replace("%ping%", VelocityConfig.HIGH_MS_COLOR.color() + player.getPing())));
				}

			} else {
				source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NO_PERMISSION.color()
						.replace("%prefix%", VelocityMessages.PREFIX.color())));
			}

		} else if (invocation.arguments().length == 1) {

			if (!(VelocityRedis.REDIS.get(Boolean.class) || proxyServer.getPluginManager().isLoaded("redisbungee"))
					|| proxyServer.getPlayer(invocation.arguments()[0]).isPresent()) {

				if (!source.hasPermission(VelocityConfig.PING_OTHERS_PERMISSION.get(String.class))) {
					source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NO_PERMISSION.color()
							.replace("%prefix%", VelocityMessages.PREFIX.color())));
					return;
				}

				final Optional<Player> target = proxyServer.getPlayer(invocation.arguments()[0]);

				if (!target.isPresent()) {

					source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_ONLINE.color()
							.replace("%prefix%", VelocityMessages.PREFIX.color())
							.replace("%user%", (invocation.arguments()[0]))));

					return;

				}

				if (!(VelocityConfig.OTHERS_PING_OPTION.get(Boolean.class))) {

					source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.USAGE.color()
							.replace("%prefix%", VelocityMessages.PREFIX.color())));

					return;
				}

				final long ping = target.get().getPing();

				if (!(VelocityConfig.DYNAMIC_PING.get(Boolean.class))) {

					source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.OTHERS_PING.color()
							.replace("%prefix%", VelocityMessages.PREFIX.color())
							.replace("%user%", (invocation.arguments()[0]))
							.replace("%ping%", "" + target.get().getPing())));

					return;

				}

				if (ping < VelocityConfig.MEDIUM_MS.get(Integer.class)) {

					source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.OTHERS_PING.color()
							.replace("%prefix%", VelocityMessages.PREFIX.color())
							.replace("%user%", (invocation.arguments()[0]))
							.replace("%ping%", VelocityConfig.LOW_MS_COLOR.color() + target.get().getPing())));

				} else if (ping > VelocityConfig.MEDIUM_MS.get(Integer.class)
						&& ping < VelocityConfig.HIGH_MS.get(Integer.class)) {

					source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.OTHERS_PING.color()
							.replace("%prefix%", VelocityMessages.PREFIX.color())
							.replace("%user%", (invocation.arguments()[0]))
							.replace("%ping%", VelocityConfig.MEDIUM_MS_COLOR.color() + target.get().getPing())));

				} else {

					source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.OTHERS_PING.color()
							.replace("%prefix%", VelocityMessages.PREFIX.color())
							.replace("%user%", (invocation.arguments()[0]))
							.replace("%ping%", VelocityConfig.HIGH_MS_COLOR.color() + target.get().getPing())));

				}

			} else {

				if (!source.hasPermission(VelocityConfig.PING_OTHERS_PERMISSION.get(String.class))) {
					source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NO_PERMISSION.color()
							.replace("%prefix%", VelocityMessages.PREFIX.color())));
					return;
				}

				final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

				final String target = invocation.arguments()[0];

				if (redisBungeeAPI.getUuidFromName(target) == null) {
					return;
				}

				final UUID uuid = redisBungeeAPI.getUuidFromName(target);

				if (!redisBungeeAPI.isPlayerOnline(uuid)) {

					source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.NOT_ONLINE.color()
							.replace("%prefix%", VelocityMessages.PREFIX.color())
							.replace("%user%", (invocation.arguments()[0]))));

					return;

				}

				final String send_message = target + ";" + uuid + ";" + redisBungeeAPI.getProxy(uuid) + ";" + player.getUniqueId();
				redisBungeeAPI.sendChannelMessage("CleanPing-Request", send_message);

			}

		} else {

			source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize(VelocityMessages.USAGE.color()
					.replace("%prefix%", VelocityMessages.PREFIX.color())));

		}
	}
}