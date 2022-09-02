package ray.mintcat.redpacket.impl

import org.bukkit.entity.Player
import ray.mintcat.redpacket.RedPacket
import ray.mintcat.redpacket.Vault
import ray.mintcat.redpacket.error
import ray.mintcat.redpacket.info
import java.util.*

class RedPacketData(
    val name: String,
    val sender: String,
    var money: Double,
    val number: Int,
    val key: String,
    val getter: MutableList<UUID> = mutableListOf()
) {
    fun run(player: Player) {
        if (getter.contains(player.uniqueId)) {
            player.error("这个红包你已经领取过了留给别人吧!")
            return
        }
        if (getter.size >= number || money <= 0) {
            player.error("红包被抢光啦!")
            return
        }
        val ran = (1..money.toInt()).random()
        getter.add(player.uniqueId)
        if (getter.size >= number) {
            Vault.addMoney(player, money)
            player.info("你抢到了 ${money} 游戏币")
            money = 0.0
            RedPacket.packs.remove(this)
            return
        }
        if (money >= ran) {
            money -= ran
            Vault.addMoney(player, ran.toDouble())
            player.info("你抢到了 ${ran} 游戏币")
        } else {
            Vault.addMoney(player, money)
            player.info("你抢到了 ${money} 游戏币")
            money = 0.0
            RedPacket.packs.remove(this)
        }


    }
}