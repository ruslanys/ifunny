package me.ruslanys.ifunny.grab.event

import me.ruslanys.ifunny.channel.Channel
import me.ruslanys.ifunny.channel.MemeInfo

class MemeIndexRequest(channel: Channel, val info: MemeInfo) : GrabEvent(channel) {
    override fun toString(): String {
        return "MemeIndexRequest(channel=${channel.getName()}, info=$info)"
    }
}
