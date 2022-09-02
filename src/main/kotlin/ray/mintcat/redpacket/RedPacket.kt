package ray.mintcat.redpacket

import io.lumine.xikage.mythicmobs.MythicMobs
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import ray.mintcat.redpacket.impl.RedPacketData
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Plugin
import taboolib.common.platform.command.command
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.adaptPlayer
import taboolib.module.chat.TellrawJson
import taboolib.module.chat.colored
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import taboolib.module.nms.ItemTagData
import taboolib.module.nms.getItemTag
import taboolib.platform.util.isAir

object RedPacket : Plugin() {

    val packs = ArrayList<RedPacketData>()

    @Config
    lateinit var config: ConfigFile
        private set

    @Awake(LifeCycle.ENABLE)
    fun run() {
        //redpacket %player_name% 10000
        command("redpacket") {
            dynamic(commit = "target") {
                dynamic(commit = "info") {
                    dynamic(commit = "money") {
                        execute<CommandSender> { sender, context, argument ->
                            val target = context.argument(-2)
                            val key = config.getStringList("keys").random().replace("%name%", target)
                            val data = RedPacketData(
                                context.argument(-1),
                                target,
                                context.argument(0).toDoubleOrNull() ?: 0.0,
                                (Bukkit.getOnlinePlayers().size / 2) + 1,
                                key
                            )
                            packs.add(data)
                            Bukkit.getOnlinePlayers().forEach {
                                TellrawJson()
                                    .append("&6[&4红包&6] &d&l${data.sender} &e发送了${data.number}个红包 &c${data.name}".colored())
                                    .sendTo(adaptPlayer(it))
                                TellrawJson()
                                    .append("&6[&4红包&6] &6&l>>>点击开抢<<< 一起瓜分 ${data.money}游戏币！".colored())
                                    .suggestCommand(data.key)
                                    .sendTo(adaptPlayer(it))
                            }

                        }
                    }
                }
            }
        }
        command("redpacketadmin") {
            dynamic(commit = "target") {
                dynamic(commit = "info") {
                    dynamic(commit = "money") {
                        dynamic {
                            execute<CommandSender> { sender, context, argument ->
                                val target = context.argument(-3)
                                val key = config.getStringList("keys").random().replace("%name%", target)
                                val data = RedPacketData(
                                    context.argument(-2),
                                    target,
                                    context.argument(-1).toDoubleOrNull() ?: 0.0,
                                    context.argument(0).toIntOrNull() ?: 0,
                                    key
                                )
                                packs.add(data)
                                Bukkit.getOnlinePlayers().forEach {
                                    TellrawJson()
                                        .append("&6[&4红包&6] &d&l${data.sender} &e发送了${data.number}个红包 &c${data.name}".colored())
                                        .sendTo(adaptPlayer(it))
                                    TellrawJson()
                                        .append("&6[&4红包&6] &6&l>>>点击开抢<<< 一起瓜分 ${data.money}游戏币！".colored())
                                        .suggestCommand(data.key)
                                        .sendTo(adaptPlayer(it))
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun int(event: PlayerInteractEvent) {
        val items = event.item ?: return
        if (items.isAir){
            return
        }
        val mid = items.getString("mmi", "null")
        val mmi = MythicMobs.inst().itemManager.getItem(mid) ?: return
        if (!mmi.isPresent) {
            return
        }
        val name = mmi.get().config.getString("RedPacket.name", "null")
        if (name == "null") {
            return
        }
        if (Bukkit.getOnlinePlayers().size < 5) {
            event.player.error("最少要有 5 个人才可以发红包！")
            return
        }
        val money = mmi.get().config.getDouble("RedPacket.money")
        items.amount = items.amount - 1
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "redpacket ${event.player.name} ${name} ${money}")
    }

    @SubscribeEvent
    fun onTalk(event: AsyncPlayerChatEvent) {
        val pack = packs.firstOrNull { it.key == event.message && !it.getter.contains(event.player.uniqueId) } ?: return
        pack.run(event.player)
    }

    fun ItemStack.getString(key: String, def: String = "null"): String {
        if (key.contains(".")) {
            return this.getItemTag().getDeepOrElse(key, ItemTagData(def)).asString()
        }
        return this.getItemTag().getOrElse(key, ItemTagData(def)).asString()
    }


}